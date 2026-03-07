package com.jinjin;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.jinjin.mapper") //specify the package of mapper(we can remove the @Mapper annotation now)
@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@EnableCaching
@EnableScheduling
@Slf4j
public class JinJinApplication {


    public static void main(String[] args) {
        SpringApplication.run(JinJinApplication.class, args);
        log.info("server started");
    }

}



