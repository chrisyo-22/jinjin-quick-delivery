package com.jinjin.controller.user;

import com.jinjin.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

// 为防止冲突，设置bean的自定义名字，亦作区分
@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@Tag( name = "Shop management Related Interfaces")
public class ShopController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static final String key = "SHOP_STATUS";

    /**
     * 查询店铺营业状态
     * @return com.sky.result.Result<java.lang.Integer>
     * @author paxi
     * @data 2023/9/3
     **/
    @GetMapping("/status")
    @Operation(summary = "Query shop operating status")
    public Result<Integer> getStatus() {
        log.info("User querying shop operating status...");

        String value = redisTemplate.opsForValue().get(key);
        Integer shopStatus = value != null ? Integer.valueOf(value) : 0;
        log.info("Shop operating status found: {}", shopStatus == 1 ? "Open" : "Closed");

        return Result.success(shopStatus);
    }
}