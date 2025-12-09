package com.jinjin.aspect;

import com.jinjin.anno.AutoFill;
import com.jinjin.constant.AutoFillConstant;
import com.jinjin.context.BaseContext;
import com.jinjin.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;


/**
 * Common properties auto fill aspect
 */
@Component
@Aspect
@Slf4j
public class AutoFillAspect {
    @Before("@annotation(com.jinjin.anno.AutoFill)")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException {
        //1. get annotation value from target method
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AutoFill autofill = method.getAnnotation(AutoFill.class);
        OperationType operationType = autofill.value();

        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        //dont convert them just yet, they could be different types
        Object entity = args[0]; //Employee or some other instance object

        //2. check what's the value(update or insert)
        try{
            if(operationType == OperationType.INSERT){
                //reflection
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setCreateTime.invoke(entity, LocalDateTime.now());
                setUpdateTime.invoke(entity, LocalDateTime.now());
                setCreateUser.invoke(entity, BaseContext.getCurrentId());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());

            }
            //Update
            else if(operationType == OperationType.UPDATE){
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, LocalDateTime.now());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

    }
}
