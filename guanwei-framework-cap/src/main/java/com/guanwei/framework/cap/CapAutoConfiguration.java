package com.guanwei.framework.cap;

import com.guanwei.framework.cap.impl.CapPublisherImpl;
import com.guanwei.framework.cap.impl.CapSubscriberImpl;
import com.guanwei.framework.cap.impl.CapTransactionManagerImpl;
import com.guanwei.framework.cap.processor.CapSubscribeProcessor;
import com.guanwei.framework.cap.processor.CapSubscriberProcessor;
import com.guanwei.framework.cap.queue.CapQueueManager;
import com.guanwei.framework.cap.queue.MessageQueue;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * CAP 组件自动配置类
 * 负责自动配置 CAP 组件的所有 Bean
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CapProperties.class)
@ComponentScan(basePackages = "com.guanwei.framework.cap")
@ConditionalOnProperty(prefix = "cap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CapAutoConfiguration {

    /**
     * 配置 CAP 发布者
     */
    @Bean
    @ConditionalOnMissingBean(CapPublisher.class)
    public CapPublisher capPublisher(MessageQueue messageQueue, MessageStorage messageStorage,
            CapProperties capProperties, CapTransactionManager transactionManager) {
        log.info("Creating CAP Publisher bean");
        return new CapPublisherImpl(messageQueue, messageStorage, capProperties, transactionManager);
    }

    /**
     * 配置 CAP 订阅者
     */
    @Bean
    @ConditionalOnMissingBean(CapSubscriber.class)
    public CapSubscriber capSubscriber(MessageStorage messageStorage, MessageQueue messageQueue,
            CapProperties capProperties) {
        log.info("Creating CAP Subscriber bean");
        return new CapSubscriberImpl(messageStorage, messageQueue, capProperties);
    }

    /**
     * 配置 CAP 事务管理器
     */
    @Bean
    @ConditionalOnMissingBean(CapTransactionManager.class)
    public CapTransactionManager capTransactionManager() {
        log.info("Creating CAP Transaction Manager bean");
        return new CapTransactionManagerImpl();
    }

    /**
     * 配置 CAP 订阅者处理器
     */
    @Bean
    @ConditionalOnMissingBean(CapSubscriberProcessor.class)
    public CapSubscriberProcessor capSubscriberProcessor(CapSubscriber capSubscriber) {
        log.info("Creating CAP Subscriber Processor bean");
        return new CapSubscriberProcessor(capSubscriber);
    }

    /**
     * 配置 CAP 订阅处理器
     */
    @Bean
    @ConditionalOnMissingBean(CapSubscribeProcessor.class)
    public CapSubscribeProcessor capSubscribeProcessor() {
        log.info("Creating CAP Subscribe Processor bean");
        return new CapSubscribeProcessor();
    }

    /**
     * 配置消息队列（默认使用内存队列）
     */
    @Bean
    @ConditionalOnMissingBean(name = "messageQueue")
    @ConditionalOnProperty(prefix = "cap.message-queue", name = "type", havingValue = "memory", matchIfMissing = true)
    public MessageQueue memoryMessageQueue() {
        log.info("Creating Memory Message Queue bean");
        return new com.guanwei.framework.cap.queue.MemoryMessageQueue();
    }

    /**
     * 配置 RabbitMQ 消息队列
     */
    @Bean
    @ConditionalOnMissingBean(name = "messageQueue")
    @ConditionalOnProperty(prefix = "cap.message-queue", name = "type", havingValue = "rabbitmq", matchIfMissing = false)
    public MessageQueue rabbitMQMessageQueue(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate,
            ConnectionFactory connectionFactory, CapQueueManager capQueueManager,
            @Value("${cap.message-queue.exchange-name:cap.exchange}") String exchangeName,
            @Value("${cap.message-queue.queue-prefix:cap_}") String queuePrefix) {
        log.info("Creating RabbitMQ Message Queue bean");
        return new com.guanwei.framework.cap.queue.RabbitMQMessageQueue(amqpAdmin, rabbitTemplate,
                connectionFactory, capQueueManager,
                exchangeName, queuePrefix);
    }

    /**
     * 配置消息存储（默认使用内存存储）
     */
    @Bean
    @ConditionalOnMissingBean(name = "messageStorage")
    @ConditionalOnProperty(prefix = "cap.storage", name = "type", havingValue = "memory", matchIfMissing = true)
    public MessageStorage memoryMessageStorage() {
        log.info("Creating Memory Message Storage bean");
        return new com.guanwei.framework.cap.storage.MemoryMessageStorage();
    }

    /**
     * 配置 Redis 消息存储
     */
    @Bean
    @ConditionalOnMissingBean(name = "messageStorage")
    @ConditionalOnProperty(prefix = "cap.storage", name = "type", havingValue = "redis", matchIfMissing = false)
    public MessageStorage redisMessageStorage() {
        log.info("Creating Redis Message Storage bean");
        return new com.guanwei.framework.cap.storage.RedisMessageStorage();
    }

    /**
     * 配置 CAP 队列管理器（仅在使用 RabbitMQ 时）
     */
    @Bean
    @ConditionalOnMissingBean(CapQueueManager.class)
    @ConditionalOnProperty(prefix = "cap.message-queue", name = "type", havingValue = "rabbitmq", matchIfMissing = false)
    public CapQueueManager capQueueManager(RabbitAdmin rabbitAdmin,
            @Value("${cap.message-queue.exchange-name:cap.exchange}") String exchangeName,
            @Value("${cap.message-queue.exchange-type:topic}") String exchangeType,
            @Value("${cap.default-group:default}") String defaultGroup) {
        log.info("Creating CAP Queue Manager bean with exchange: {} (type: {})", exchangeName, exchangeType);
        return new CapQueueManager(rabbitAdmin, exchangeName, exchangeType, defaultGroup);
    }
}