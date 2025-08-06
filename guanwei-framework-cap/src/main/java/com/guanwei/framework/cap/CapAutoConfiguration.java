package com.guanwei.framework.cap;

import com.guanwei.framework.cap.impl.CapPublisherImpl;
import com.guanwei.framework.cap.impl.CapSubscriberImpl;
import com.guanwei.framework.cap.impl.CapTransactionManagerImpl;
import com.guanwei.framework.cap.processor.*;
import com.guanwei.framework.cap.queue.CapQueueManager;
import com.guanwei.framework.cap.queue.MessageQueue;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

/**
 * CAP 自动配置类
 * 参考 .NET Core CAP 的自动配置机制
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CapProperties.class)
@ConditionalOnProperty(prefix = "cap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CapAutoConfiguration {

    private MessageRetryProcessor messageRetryProcessor;
    private MessageCollectorProcessor messageCollectorProcessor;
    private DefaultMessageDispatcher messageDispatcher;

    /**
     * 配置消息存储
     */
    @Bean
    public MessageStorage messageStorage(CapProperties properties) {
        // 根据配置选择存储类型
        String storageType = properties.getStorage().getType();
        switch (storageType.toLowerCase()) {
            case "memory":
                return new com.guanwei.framework.cap.storage.MemoryMessageStorage();
            case "redis":
                return new com.guanwei.framework.cap.storage.RedisMessageStorage();
            case "oracle":
                return new com.guanwei.framework.cap.storage.OracleMessageStorage();
            default:
                log.warn("Unknown storage type: {}, using memory storage", storageType);
                return new com.guanwei.framework.cap.storage.MemoryMessageStorage();
        }
    }

    /**
     * 配置队列管理器
     */
    @Bean
    public CapQueueManager capQueueManager(CapProperties properties, ConnectionFactory connectionFactory) {
        return new CapQueueManager(
                new RabbitAdmin(connectionFactory),
                properties.getMessageQueue().getRabbitmq().getExchangeName(),
                properties.getMessageQueue().getRabbitmq().getExchangeType(),
                properties.getDefaultGroupName());
    }

    /**
     * 配置消息队列
     */
    @Bean
    public MessageQueue messageQueue(CapProperties properties,
            AmqpAdmin amqpAdmin,
            RabbitTemplate rabbitTemplate,
            ConnectionFactory connectionFactory,
            CapQueueManager capQueueManager) {
        // 根据配置选择队列类型
        String queueType = properties.getMessageQueue().getType();
        switch (queueType.toLowerCase()) {
            case "memory":
                return new com.guanwei.framework.cap.queue.MemoryMessageQueue();
            case "rabbitmq":
                return new com.guanwei.framework.cap.queue.RabbitMQMessageQueue(
                        amqpAdmin,
                        rabbitTemplate,
                        connectionFactory,
                        capQueueManager,
                        properties.getMessageQueue().getRabbitmq().getExchangeName(),
                        properties.getMessageQueue().getRabbitmq().getQueuePrefix());
            default:
                log.warn("Unknown queue type: {}, using memory queue", queueType);
                return new com.guanwei.framework.cap.queue.MemoryMessageQueue();
        }
    }

    /**
     * 配置订阅处理器
     */
    @Bean
    public CapSubscriberProcessor capSubscriberProcessor(CapSubscriber capSubscriber) {
        return new CapSubscriberProcessor(capSubscriber);
    }

    /**
     * 配置订阅执行器
     */
    @Bean
    public SubscribeExecutor subscribeExecutor(CapProperties properties, MessageStorage messageStorage) {
        return new DefaultSubscribeExecutor(properties, messageStorage);
    }

    /**
     * 配置消息发送器
     */
    @Bean
    public MessageSender messageSender(CapProperties properties, MessageQueue messageQueue) {
        return new DefaultMessageSender(properties, messageQueue);
    }

    /**
     * 配置消息分发器
     */
    @Bean
    public MessageDispatcher messageDispatcher(CapProperties properties,
            MessageStorage messageStorage,
            MessageQueue messageQueue,
            SubscribeExecutor subscribeExecutor,
            MessageSender messageSender) {
        this.messageDispatcher = new DefaultMessageDispatcher(properties, messageStorage, messageQueue,
                subscribeExecutor, messageSender);
        return this.messageDispatcher;
    }

    /**
     * 配置消息重试处理器
     */
    @Bean
    public MessageRetryProcessor messageRetryProcessor(CapProperties properties,
            MessageStorage messageStorage,
            MessageDispatcher messageDispatcher) {
        this.messageRetryProcessor = new MessageRetryProcessor(properties, messageStorage, messageDispatcher);
        return this.messageRetryProcessor;
    }

    /**
     * 配置消息清理处理器
     */
    @Bean
    public MessageCollectorProcessor messageCollectorProcessor(CapProperties properties,
            MessageStorage messageStorage) {
        this.messageCollectorProcessor = new MessageCollectorProcessor(properties, messageStorage);
        return this.messageCollectorProcessor;
    }

    /**
     * 配置CAP发布器
     */
    @Bean
    public CapPublisher capPublisher(CapProperties properties,
            MessageStorage messageStorage,
            MessageQueue messageQueue,
            CapTransactionManager capTransactionManager) {
        return new CapPublisherImpl(messageQueue, messageStorage, properties, capTransactionManager);
    }

    /**
     * 配置CAP订阅器
     */
    @Bean
    public CapSubscriber capSubscriber(CapProperties properties,
            MessageStorage messageStorage,
            MessageQueue messageQueue,
            CapQueueManager capQueueManager) {
        return new CapSubscriberImpl(messageStorage, messageQueue, properties, capQueueManager);
    }

    /**
     * 配置CAP事务管理器
     */
    @Bean
    public CapTransactionManager capTransactionManager() {
        return new CapTransactionManagerImpl();
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void destroy() {
        log.info("Shutting down CAP components...");

        if (messageRetryProcessor != null) {
            messageRetryProcessor.shutdown();
        }

        if (messageCollectorProcessor != null) {
            messageCollectorProcessor.shutdown();
        }

        if (messageDispatcher != null) {
            messageDispatcher.stop();
        }

        log.info("CAP components shutdown completed");
    }
}