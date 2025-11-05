package com.jinjin.service.impl;

import com.jinjin.constant.MessageConstant;
import com.jinjin.constant.PasswordConstant;
import com.jinjin.constant.StatusConstant;
import com.jinjin.dto.EmployeeDTO;
import com.jinjin.dto.EmployeeLoginDTO;
import com.jinjin.entity.Employee;
import com.jinjin.exception.AccountLockedException;
import com.jinjin.exception.AccountNotFoundException;
import com.jinjin.exception.PasswordErrorException;
import com.jinjin.mapper.EmployeeMapper;
import com.jinjin.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void addEmp(EmployeeDTO employeeDTO) {

        Employee employee = new Employee();
        //copy properties
        BeanUtils.copyProperties(employeeDTO, employee);
        //1. Fill missing properties
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //fix for now
        employee.setCreateUser(10L);
        employee.setUpdateUser(10L);

        //2. invoke mapper add method, save this object into employee table
        employeeMapper.insert(employee);


    }

}
