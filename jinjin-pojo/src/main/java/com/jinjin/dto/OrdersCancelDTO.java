package com.jinjin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "订单取消数据传输对象")
public class OrdersCancelDTO implements Serializable {

    @Schema(description = "订单ID")
    private Long id;
    
    @Schema(description = "订单取消原因")
    private String cancelReason;

}
