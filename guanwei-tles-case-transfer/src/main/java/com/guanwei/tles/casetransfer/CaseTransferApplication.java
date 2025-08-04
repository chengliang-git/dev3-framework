package com.guanwei.tles.casetransfer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 案件数据转存服务启动类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.guanwei.tles.casetransfer.mapper.oracle")
public class CaseTransferApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseTransferApplication.class, args);
    }
}