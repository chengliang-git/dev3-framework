package com.guanwei.framework.cap.impl;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapPublisher;
import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.queue.MessageQueue;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * CAP 发布者实现类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class CapPublisherImpl implements CapPublisher {

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private MessageStorage messageStorage;

    @Autowired
    private CapProperties capProperties;

    @Override
    public String publish(String name, Object content) {
        return publish(name, content, capProperties.getDefaultGroup());
    }

    @Override
    public CompletableFuture<String> publishAsync(String name, Object content) {
        return CompletableFuture.supplyAsync(() -> publish(name, content));
    }

    @Override
    public String publish(String name, Object content, String group) {
        String messageId = UUID.randomUUID().toString();
        
        CapMessage message = CapMessage.builder()
                .id(messageId)
                .name(name)
                .content(content.toString())
                .group(group)
                .status(CapMessage.MessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retries(0)
                .maxRetries(capProperties.getRetry().getMaxRetries())
                .build();

        // 保存消息到存储
        messageStorage.store(message);

        // 发送到队列
        String queueName = buildQueueName(name, group);
        messageQueue.send(queueName, message);

        log.info("Published message: {} to queue: {}", messageId, queueName);
        return messageId;
    }

    @Override
    public CompletableFuture<String> publishAsync(String name, Object content, String group) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, group));
    }

    @Override
    public String publishDelay(String name, Object content, long delaySeconds) {
        return publishDelay(name, content, capProperties.getDefaultGroup(), delaySeconds);
    }

    @Override
    public String publishDelay(String name, Object content, String group, long delaySeconds) {
        String messageId = UUID.randomUUID().toString();
        
        CapMessage message = CapMessage.builder()
                .id(messageId)
                .name(name)
                .content(content.toString())
                .group(group)
                .status(CapMessage.MessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retries(0)
                .maxRetries(capProperties.getRetry().getMaxRetries())
                .expiresAt(LocalDateTime.now().plusSeconds(delaySeconds))
                .build();

        // 保存消息到存储
        messageStorage.store(message);

        // 延迟消息暂时不发送到队列，由调度器处理
        log.info("Published delay message: {} to group: {} with delay: {}s", messageId, group, delaySeconds);
        return messageId;
    }

    @Override
    public String publishTransactional(String name, Object content) {
        return publishTransactional(name, content, capProperties.getDefaultGroup());
    }

    @Override
    public String publishTransactional(String name, Object content, String group) {
        // 事务性消息的处理逻辑
        // 在实际应用中，这里需要与数据库事务集成
        log.warn("Transactional message publishing not fully implemented yet");
        return publish(name, content, group);
    }

    /**
     * 构建队列名称
     */
    private String buildQueueName(String name, String group) {
        return capProperties.getMessageQueue().getQueuePrefix() + name + "_" + group;
    }
} 