package com.jinjin.service;


import com.jinjin.dto.DishDTO;

public interface DishService {
    void startOrStop(Integer status, Long id);

    void addDish(DishDTO dishDTO);
}
