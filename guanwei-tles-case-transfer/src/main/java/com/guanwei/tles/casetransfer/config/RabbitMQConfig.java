package com.guanwei.tles.casetransfer.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 案件转存项目 RabbitMQ 配置类
 * 参考 GitHub CAP 源码的队列命名规则：routeKey + "." + groupName
 * CAP框架会自动创建和绑定队列，这里只需要配置基础组件
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
     * CAP框架会自动创建这个交换机
     */
    @Bean
    public TopicExchange capExchange() {
        return new TopicExchange(capExchangeName, true, false);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("cap.dead.letter", true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("cap.dead.letter.queue", true, false, false);
    }

    /**
     * 绑定死信队列
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("cap.dead.letter");
    }

    /**
     * 案件新增队列 Bean
     * 用于 @RabbitListener 注解引用
     * 队列名称：tles.case.filing.case-transfer-group
     */
    @Bean
    public Queue caseFilingQueue() {
        return new Queue("tles.case.filing." + capDefaultGroup, true, false, false);
    }

    /**
     * 案件违法信息录入队列 Bean
     * 用于 @RabbitListener 注解引用
     * 队列名称：tles.case.case-illegal.case-transfer-group
     */
    @Bean
    public Queue caseIllegalQueue() {
        return new Queue("tles.case.case-illegal." + capDefaultGroup, true, false, false);
    }

    /**
     * 案件修改队列 Bean
     * 用于 @RabbitListener 注解引用
     * 队列名称：case.updated.case-transfer-group
     */
    @Bean
    public Queue caseUpdatedQueue() {
        return new Queue("case.updated." + capDefaultGroup, true, false, false);
    }

    /**
     * 案件删除队列 Bean
     * 用于 @RabbitListener 注解引用
     * 队列名称：case.deleted.case-transfer-group
     */
    @Bean
    public Queue caseDeletedQueue() {
        return new Queue("case.deleted." + capDefaultGroup, true, false, false);
    }

    /**
     * 绑定案件新增队列到交换机
     * 路由键：tles.case.filing
     */
    @Bean
    public Binding caseFilingBinding() {
        return BindingBuilder.bind(caseFilingQueue())
                .to(capExchange())
                .with("tles.case.filing");
    }

    /**
     * 绑定案件违法信息录入队列到交换机
     * 路由键：tles.case.case-illegal
     */
    @Bean
    public Binding caseIllegalBinding() {
        return BindingBuilder.bind(caseIllegalQueue())
                .to(capExchange())
                .with("tles.case.case-illegal");
    }
}