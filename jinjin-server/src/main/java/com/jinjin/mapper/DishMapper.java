package com.jinjin.mapper;

import com.github.pagehelper.Page;
import com.jinjin.anno.AutoFill;
import com.jinjin.dto.DishPageQueryDTO;
import com.jinjin.entity.Dish;
import com.jinjin.enumeration.OperationType;
import com.jinjin.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);


    //    @Options(useGeneratedKeys = true, keyProperty = "id") use only here or the xml, do not use together
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> list(DishPageQueryDTO pageQueryDTO);

    Dish selectById(Long id);

    void deleteBatch(List<Long> ids);

    /**
     * update dish info
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);
}
