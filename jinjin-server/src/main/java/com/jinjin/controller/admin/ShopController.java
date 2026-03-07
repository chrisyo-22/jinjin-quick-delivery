package com.jinjin.controller.admin;

import com.jinjin.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Tag(name = "Admin shop related interfaces")
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PutMapping("/{status}")
    @Operation(summary = "Set shop operating status")
    public Result<String> setStatus(@PathVariable Integer status) {
        log.info("Setting shop status to {}", status);
        redisTemplate.opsForValue().set(KEY, String.valueOf(status));
        return Result.success();
    }

    @GetMapping("/status")
    @Operation(summary = "Get shop operating status")
    public Result<Integer> getStatus() {
        String value = redisTemplate.opsForValue().get(KEY);
        Integer status = value == null ? 0 : Integer.valueOf(value);
        return Result.success(status);
    }
}
