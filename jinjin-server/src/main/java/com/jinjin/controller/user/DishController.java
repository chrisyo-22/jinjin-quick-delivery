package com.jinjin.controller.user;

import com.jinjin.constant.StatusConstant;
import com.jinjin.entity.Dish;
import com.jinjin.result.Result;
import com.jinjin.service.DishService;
import com.jinjin.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Tag(name = "User dish browse interfaces")
public class DishController {

    @Autowired
    private DishService dishService;

    @GetMapping("/list")
    @Operation(summary = "List dishes by category")
    @Cacheable(cacheNames = "dishCache", key = "#categoryId")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("Listing dishes for category {}", categoryId);
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        return Result.success(dishService.listWithFlavor(dish));
    }
}
