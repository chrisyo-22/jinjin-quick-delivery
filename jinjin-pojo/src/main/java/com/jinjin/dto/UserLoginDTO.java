package com.jinjin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
@Schema(description = "C端用户登录数据传输对象")
public class UserLoginDTO implements Serializable {

    @Schema(description = "微信授权码")
    private String code;

}
