package com.jinjin;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
public class SkyApplication {

    @Autowired
    private Environment env;


    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }

    // 项目启动后自动执行，打印数据源配置
    @PostConstruct
    public void printDatasourceConfig() {
        System.out.println("=== 数据源配置加载结果 ===");
        System.out.println("driver-class-name: " + env.getProperty("sky.datasource.driver-class-name"));
        System.out.println("url: " + env.getProperty("spring.datasource.druid.url"));
        System.out.println("username: " + env.getProperty("sky.datasource.username"));
    }
}



