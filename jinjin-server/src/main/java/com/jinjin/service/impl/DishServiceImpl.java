package com.jinjin.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jinjin.constant.StatusConstant;
import com.jinjin.dto.DishDTO;
import com.jinjin.dto.DishPageQueryDTO;
import com.jinjin.entity.Dish;
import com.jinjin.entity.DishFlavor;
import com.jinjin.entity.Setmeal;
import com.jinjin.exception.DeletionNotAllowedException;
import com.jinjin.mapper.*;
import com.jinjin.result.PageResult;
import com.jinjin.service.DishService;
import com.jinjin.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
//            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
//            if (setmealIds != null && setmealIds.size() > 0) {
//                for (Long setmealId : setmealIds) {
//                    Setmeal setmeal = Setmeal.builder()
//                            .id(setmealId)
//                            .status(StatusConstant.DISABLE)
//                            .build();
//                    setmealMapper.update(setmeal);
//                }
//            }
        }
    }

    @Override
    public void addDish(DishDTO dishDTO) {
        //1. Construct dish information
        Dish dish = new Dish();
        //copy properties
        BeanUtils.copyProperties(dishDTO,dish);
        //invoke mapper and save
        dishMapper.insert(dish);

        //2. construct dish flavours
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        dishFlavors.forEach(flavor->{
            flavor.setDishId(dish.getId());
        });

        dishFlavorMapper.insertBatch(dishFlavors);

    }

    @Override
    public PageResult page(DishPageQueryDTO pageQueryDTO) {
        //set page params:
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());


        //invoke mapper
        Page<DishVO> page = dishMapper.list(pageQueryDTO);

        //encapsulate as PageResult
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        //1. check dish is active, active is not allowed to delete
        ids.forEach(id->{
            Dish dish = dishMapper.selectById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException("Deletion not allowed, dish is active:");
            }
        });

        //2. check if dish is used in setmeal, setmeal is not allowed to delete
        Integer count= setmealDishMapper.countByDishId(ids);
        if(count > 0){
            throw new DeletionNotAllowedException("Deletion not allowed, dish is used in set meal:");
        }

        //3.delete from dish table

        dishMapper.deleteBatch(ids);
        //4.delete from dish_flavor table
        dishFlavorMapper.deleteBatch(ids);
    }

    @Override
    public DishVO getById(Long dishId) {

        DishVO dishV0 = new DishVO();
        //1.query dish basic info on dish id and copy to vo object
        Dish dish = dishMapper.selectById(dishId);
        BeanUtils.copyProperties(dish,dishV0);

        //2. query flavor info based on dish id
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(dishId);
        dishV0.setFlavors(flavors);

        //3. construct vo object and return
        return dishV0;
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        //1. Modify dish basic info(dish)
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //2. Modify dish flavor info(dish flavor)
        //because flavor involves update, delete or addition, it's more complicated, we should DELETE
        //all old data and INSERT new data.
        dishFlavorMapper.deleteByDish(dishDTO.getId());

        List<DishFlavor> dishFlavors = dishDTO.getFlavors();

        if(dishFlavors == null || dishFlavors.isEmpty()){
            return;
        }
        dishFlavors.forEach(flavor->{
            flavor.setDishId(dishDTO.getId());
        });

        dishFlavorMapper.insertBatch(dishDTO.getFlavors());

    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.listByCondition(dish);
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish item : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(item, dishVO);
            dishVO.setFlavors(dishFlavorMapper.selectByDishId(item.getId()));
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

}
