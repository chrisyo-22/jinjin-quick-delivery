package com.jinjin.service;


import com.jinjin.dto.DishDTO;
import com.jinjin.dto.DishPageQueryDTO;
import com.jinjin.result.PageResult;
import com.jinjin.vo.DishVO;

import java.util.List;

public interface DishService {
    void startOrStop(Integer status, Long id);

    void addDish(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO pageQueryDTO);

    void delete(List<Long> ids);

    DishVO getById(Long dishId);
}
