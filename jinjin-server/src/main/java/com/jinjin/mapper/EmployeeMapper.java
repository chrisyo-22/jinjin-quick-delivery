package com.jinjin.mapper;

import com.github.pagehelper.Page;
import com.jinjin.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * Add new employee
     * @param employee
     */
    void insert(Employee employee);


    /**
     * Retrieves a paginated list of employees based on the provided name.
     *
     * @param name the name of the employee(s) to search for
     * @return a paginated list of employees matching the provided name
     */
    Page<Employee> list(String name);
}
