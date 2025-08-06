package com.guanwei.framework.config;

import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * CAP框架会自动创建和管理所有必要的交换机和队列，无需手动配置
 * 
 * @author Enterprise Framework
 */
@Configuration
public class RabbitMQConfig {
    // CAP框架会自动创建和管理所有必要的交换机和队列
    // 业务代码无需手动配置这些基础设施组件
}