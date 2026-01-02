package com.jinjin.controller;

import com.jinjin.dto.DishDTO;
import com.jinjin.dto.DishPageQueryDTO;
import com.jinjin.result.PageResult;
import com.jinjin.result.Result;
import com.jinjin.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.jinjin.service.DishService;

import java.util.List;

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
    @Operation(summary = "Add a dish")
    @PostMapping
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("add dish: {}", dishDTO);
        dishService.addDish(dishDTO);
        return Result.success();
    }

    /**
     * Query dishes
     */
    @Operation(summary = "Query dishes")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO pageQueryDTO){
        log.info("pageQueryDTO: {}", pageQueryDTO);
        PageResult pageResult = dishService.page(pageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * Delete dish
     */
    @Operation(summary = "Delete Dish")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("delete dish: {}", ids);
        dishService.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get dish by id")
    public Result getById(@PathVariable Long id){
        log.info("get dish by id: {}", id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }
}
