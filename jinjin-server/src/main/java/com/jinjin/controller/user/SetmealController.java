package com.jinjin.controller.user;

import com.jinjin.constant.StatusConstant;
import com.jinjin.entity.Setmeal;
import com.jinjin.result.Result;
import com.jinjin.service.SetmealService;
import com.jinjin.vo.DishItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Tag(name = "User setmeal browse interfaces")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/list")
    @Operation(summary = "List setmeals by category")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    public Result<List<Setmeal>> list(Long categoryId) {
        log.info("Listing setmeals for category {}", categoryId);
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);
        return Result.success(setmealService.list(setmeal));
    }

    @GetMapping("/dish/{id}")
    @Operation(summary = "Get dish items by setmeal id")
    public Result<List<DishItemVO>> getDishItemById(@PathVariable Long id) {
        return Result.success(setmealService.getDishItemById(id));
    }
}
