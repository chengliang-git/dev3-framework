package com.guanwei.framework.cap;

import com.guanwei.framework.cap.queue.MessageQueue;
import com.guanwei.framework.cap.queue.MemoryMessageQueue;
import com.guanwei.framework.cap.queue.RabbitMQMessageQueue;
import com.guanwei.framework.cap.storage.MessageStorage;
import com.guanwei.framework.cap.storage.MemoryMessageStorage;
import com.guanwei.framework.cap.impl.CapSubscriberImpl;
import com.guanwei.framework.cap.impl.CapPublisherImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CAP 自动配置类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(CapProperties.class)
@ConditionalOnProperty(prefix = "cap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CapConfig {

    /**
     * 消息队列 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageQueue messageQueue(CapProperties capProperties) {
        String queueType = capProperties.getMessageQueue().getType();
        if ("rabbitmq".equalsIgnoreCase(queueType)) {
            return new RabbitMQMessageQueue();
        } else {
            return new MemoryMessageQueue();
        }
    }

    /**
     * 消息存储 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageStorage messageStorage() {
        return new MemoryMessageStorage();
    }

    /**
     * CAP 订阅者实现 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CapSubscriber capSubscriber() {
        return new CapSubscriberImpl();
    }

    /**
     * CAP 发布者实现 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CapPublisher capPublisher() {
        return new CapPublisherImpl();
    }
}