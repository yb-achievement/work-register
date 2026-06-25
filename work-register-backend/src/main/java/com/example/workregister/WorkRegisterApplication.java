package com.example.workregister;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 个人工作登记后端应用入口。
 */
@MapperScan("com.example.workregister.mapper")
@SpringBootApplication
public class WorkRegisterApplication {

    /**
     * 启动 Spring Boot 应用。
     */
    public static void main(String[] args) {
        SpringApplication.run(WorkRegisterApplication.class, args);
    }
}
