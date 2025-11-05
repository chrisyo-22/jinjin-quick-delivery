package com.jinjin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据概览查询数据传输对象")
public class DataOverViewQueryDTO implements Serializable {

    @Schema(description = "开始时间")
    private LocalDateTime begin;

    @Schema(description = "结束时间")
    private LocalDateTime end;

}
