package com.enterprise.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 企业级Spring Boot框架主启动类
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class FrameworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrameworkApplication.class, args);
        System.out.println("==================================================");
        System.out.println("企业级Spring Boot框架启动成功！");
        System.out.println("API文档地址: http://localhost:8080/doc.html");
        System.out.println("==================================================");
    }
}