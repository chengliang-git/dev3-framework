package com.guanwei.yourproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 * 
 * @author Your Name
 * @since 1.0.0
 */
@SpringBootApplication
public class YourProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourProjectApplication.class, args);
        System.out.println("==================================================");
        System.out.println("项目启动成功！");
        System.out.println("API文档地址: http://localhost:8080/doc.html");
        System.out.println("==================================================");
    }
} 