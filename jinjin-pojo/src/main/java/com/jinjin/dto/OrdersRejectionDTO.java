package com.jinjin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "订单拒绝数据传输对象")
public class OrdersRejectionDTO implements Serializable {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单拒绝原因")
    private String rejectionReason;

}
