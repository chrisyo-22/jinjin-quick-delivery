package com.jinjin.service;

import com.jinjin.dto.EmployeeDTO;
import com.jinjin.dto.EmployeeLoginDTO;
import com.jinjin.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void addEmp(EmployeeDTO employeeDTO);
}
