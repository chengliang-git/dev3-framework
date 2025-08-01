package com.guanwei.tles.casetransfer.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 案件转存项目 RabbitMQ 配置类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {

    @Value("${cap.message-queue.exchange-name:cap.exchange}")
    private String capExchangeName;

    @Value("${cap.default-group:case-transfer-group}")
    private String capDefaultGroup;

    /**
     * CAP 交换机
     */
    @Bean
    public DirectExchange capExchange() {
        return new DirectExchange(capExchangeName, true, false);
    }

    /**
     * CAP 默认队列
     */
    @Bean
    public Queue capDefaultQueue() {
        return new Queue(capDefaultGroup, true, false, false);
    }

    /**
     * 绑定 CAP 默认队列到交换机
     */
    @Bean
    public Binding capDefaultBinding() {
        return BindingBuilder.bind(capDefaultQueue())
                .to(capExchange())
                .with(capExchangeName + "." + capDefaultGroup);
    }

    /**
     * 案件新增队列
     */
    @Bean
    public Queue caseFilingQueue() {
        return new Queue("case-filing-queue", true, false, false);
    }

    /**
     * 案件修改队列
     */
    @Bean
    public Queue caseUpdatedQueue() {
        return new Queue("case-updated-queue", true, false, false);
    }

    /**
     * 案件删除队列
     */
    @Bean
    public Queue caseDeletedQueue() {
        return new Queue("case-deleted-queue", true, false, false);
    }

    /**
     * 绑定案件相关队列到交换机
     */
    @Bean
    public Binding caseFilingBinding() {
        return BindingBuilder.bind(caseFilingQueue())
                .to(capExchange())
                .with("tles.case.filing");
    }

    @Bean
    public Binding caseUpdatedBinding() {
        return BindingBuilder.bind(caseUpdatedQueue())
                .to(capExchange())
                .with("case.updated");
    }

    @Bean
    public Binding caseDeletedBinding() {
        return BindingBuilder.bind(caseDeletedQueue())
                .to(capExchange())
                .with("case.deleted");
    }
} 