package com.jinjin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "订单确认数据传输对象")
public class OrdersConfirmDTO implements Serializable {

    @Schema(description = "订单ID")
    private Long id;
    
    @Schema(description = "订单状态 1待付款 2待接单 3 已接单 4 派送中 5 已完成 6 已取消 7 退款")
    private Integer status;

}
