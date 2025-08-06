package com.guanwei.tles.casetransfer.config;

import org.springframework.context.annotation.Configuration;

/**
 * 案件转存项目 RabbitMQ 配置类
 * CAP框架会自动创建和管理所有必要的交换机和队列，无需手动配置
 * 参考 .NET Core CAP 组件的设计理念
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {
    // CAP框架会自动创建和管理所有必要的交换机和队列
    // 业务代码无需手动配置这些基础设施组件
}