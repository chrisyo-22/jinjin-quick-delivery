package com.jinjin.controller;

import com.jinjin.dto.DishDTO;
import com.jinjin.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.jinjin.service.DishService;

@RestController
@RequestMapping("/admin/dish")
@Tag(name = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @Operation(summary ="菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status,id);
        return Result.success();
    }


    /**
     * Add a dish
     */
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("add dish: {}", dishDTO);
        dishService.addDish(dishDTO);
        return Result.success();
    }
}
