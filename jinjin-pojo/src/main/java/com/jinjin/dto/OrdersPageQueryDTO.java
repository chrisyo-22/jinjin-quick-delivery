package com.jinjin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "订单分页查询数据传输对象")
public class OrdersPageQueryDTO implements Serializable {

    @Schema(description = "页码")
    private int page;

    @Schema(description = "每页记录数")
    private int pageSize;

    @Schema(description = "订单号")
    private String number;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "用户ID")
    private Long userId;

}
