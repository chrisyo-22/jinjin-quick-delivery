package com.jinjin.service;

import com.github.pagehelper.Page;
import com.jinjin.dto.EmployeeDTO;
import com.jinjin.dto.EmployeeLoginDTO;
import com.jinjin.dto.EmployeePageQueryDTO;
import com.jinjin.entity.Employee;
import com.jinjin.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void addEmp(EmployeeDTO employeeDTO);


    PageResult page(EmployeePageQueryDTO dto);
}
