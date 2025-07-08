package com.enterprise.framework.cap;

import com.enterprise.framework.cap.impl.CapPublisherImpl;
import com.enterprise.framework.cap.impl.CapSubscriberImpl;
import com.enterprise.framework.cap.queue.MessageQueue;
import com.enterprise.framework.cap.storage.MessageStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * CAP 自动配置类
 * 参考 .NET Core CAP 组件的配置方式
 */
@Configuration
@EnableConfigurationProperties(CapProperties.class)
@ConditionalOnProperty(prefix = "cap", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
        CapPublisherImpl.class,
        CapSubscriberImpl.class
})
public class CapConfig {

    /**
     * 配置 CAP 发布者
     */
    @Bean
    public CapPublisher capPublisher(CapPublisherImpl publisherImpl) {
        return publisherImpl;
    }

    /**
     * 配置 CAP 订阅者
     */
    @Bean
    public CapSubscriber capSubscriber(CapSubscriberImpl subscriberImpl) {
        return subscriberImpl;
    }

    /**
     * 配置消息存储（默认使用内存存储）
     */
    @Bean
    @ConditionalOnProperty(prefix = "cap.storage", name = "type", havingValue = "memory", matchIfMissing = true)
    public MessageStorage messageStorage() {
        return new com.enterprise.framework.cap.storage.MemoryMessageStorage();
    }

    /**
     * 配置消息队列（默认使用内存队列）
     */
    @Bean
    @ConditionalOnProperty(prefix = "cap.message-queue", name = "type", havingValue = "memory", matchIfMissing = true)
    public MessageQueue messageQueue() {
        return new com.enterprise.framework.cap.queue.MemoryMessageQueue();
    }
}