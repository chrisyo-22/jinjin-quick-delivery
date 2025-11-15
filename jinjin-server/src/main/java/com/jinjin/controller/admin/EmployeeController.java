package com.jinjin.controller.admin;

import com.jinjin.constant.JwtClaimsConstant;
import com.jinjin.dto.EmployeeDTO;
import com.jinjin.dto.EmployeeLoginDTO;
import com.jinjin.dto.EmployeePageQueryDTO;
import com.jinjin.entity.Employee;
import com.jinjin.properties.JwtProperties;
import com.jinjin.result.PageResult;
import com.jinjin.result.Result;
import com.jinjin.service.EmployeeService;
import com.jinjin.utils.JwtUtil;
import com.jinjin.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }


    /**
     * Query employee by page
     * @param dto
     * @return
     */
    @Operation(summary = "Query Employee By Page")
    @GetMapping("/page")
    public Result<PageResult> page(@ParameterObject EmployeePageQueryDTO dto) {
        log.info("EmployeePageQueryDTO: {}", dto);
        log.info("Received parameters - page: {}, pageSize: {}, name: {}", dto.getPage(), dto.getPageSize(), dto.getName());
        PageResult pageResult = employeeService.page(dto);
        log.info("pageResult: {}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 退出
     *
     * @return
     */
    @Operation(summary = "Add new Employee")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * Adds a new employee and retrieves their login details.
     *
     * @return a Result object containing the EmployeeLoginVO with employee login details, such as ID, username, name, and token.
     */
    @PutMapping
    public Result<EmployeeLoginVO> addEmp(@RequestBody EmployeeDTO employeeDTO){
        log.info("adding new employee: {}", employeeDTO);
        employeeService.addEmp(employeeDTO);
        return Result.success();
    }

}
