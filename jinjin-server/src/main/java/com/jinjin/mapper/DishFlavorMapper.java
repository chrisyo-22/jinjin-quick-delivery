package com.jinjin.mapper;


import com.jinjin.entity.DishFlavor;

import java.util.List;

public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> dishFlavors);
}
