package com.jinjin.handler;

import com.jinjin.constant.MessageConstant;
import com.jinjin.exception.BaseException;
import com.jinjin.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }


    /**
     * Used to capture SQL Error
     *
     * @param ex error obj being passed in
     * @return error in a Result object
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result doSQLException(SQLIntegrityConstraintViolationException ex) {
        log.error("Error msg: {}", ex.getMessage());
        String msg = ex.getMessage();
        if (msg.contains("Duplicate")) {
            String[] split = msg.split(" ");
            String msgError = split[2]; //get which key is duplicate
            return Result.error(msgError + " " + MessageConstant.ALREADY_EXISTS);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}
