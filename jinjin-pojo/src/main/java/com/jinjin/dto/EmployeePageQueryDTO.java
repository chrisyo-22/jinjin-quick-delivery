package com.jinjin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "员工分页查询数据传输对象")
public class EmployeePageQueryDTO implements Serializable {

    @Schema(description = "员工姓名")
    private String name;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页显示记录数")
    private Integer pageSize;

}
