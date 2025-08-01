package com.guanwei.framework.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 
 * @author Enterprise Framework
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 框架交换机名称
     */
    public static final String FRAMEWORK_EXCHANGE = "framework.exchange";

    /**
     * 框架队列名称
     */
    public static final String FRAMEWORK_QUEUE = "framework.queue";

    /**
     * 框架路由键
     */
    public static final String FRAMEWORK_ROUTING_KEY = "framework.routing.key";

    /**
     * 创建框架交换机
     */
    @Bean
    public DirectExchange frameworkExchange() {
        return new DirectExchange(FRAMEWORK_EXCHANGE, true, false);
    }

    /**
     * 创建框架队列
     */
    @Bean
    public Queue frameworkQueue() {
        return new Queue(FRAMEWORK_QUEUE, true, false, false);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding frameworkBinding() {
        return BindingBuilder.bind(frameworkQueue())
                .to(frameworkExchange())
                .with(FRAMEWORK_ROUTING_KEY);
    }
}