package com.enterprise.framework.cap.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.enterprise.framework.cap.CapMessage;
import com.enterprise.framework.cap.CapProperties;
import com.enterprise.framework.cap.CapPublisher;
import com.enterprise.framework.cap.queue.MessageQueue;
import com.enterprise.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * CAP 发布者实现类
 * 负责消息的发布、存储和队列发送
 */
@Slf4j
@Component
public class CapPublisherImpl implements CapPublisher {

    @Autowired
    private MessageStorage messageStorage;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private CapProperties capProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        try {
            CapMessage message = buildMessage(name, content, group);

            // 存储消息
            if (!messageStorage.store(message)) {
                throw new RuntimeException("Failed to store message: " + message.getId());
            }

            // 发送到队列
            String queueName = buildQueueName(name, group);
            if (!messageQueue.send(queueName, message)) {
                throw new RuntimeException("Failed to send message to queue: " + queueName);
            }

            log.info("Published message: {} -> {}", message.getId(), queueName);
            return message.getId();
        } catch (Exception e) {
            log.error("Failed to publish message: name={}, group={}", name, group, e);
            throw new RuntimeException("Failed to publish message", e);
        }
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
        try {
            CapMessage message = buildMessage(name, content, group);
            message.setExpiresAt(LocalDateTime.now().plusSeconds(delaySeconds));

            // 存储消息
            if (!messageStorage.store(message)) {
                throw new RuntimeException("Failed to store delay message: " + message.getId());
            }

            // 发送延迟消息到队列
            String queueName = buildQueueName(name, group);
            if (!messageQueue.sendDelay(queueName, message, delaySeconds)) {
                throw new RuntimeException("Failed to send delay message to queue: " + queueName);
            }

            log.info("Published delay message: {} -> {} (delay: {}s)", message.getId(), queueName, delaySeconds);
            return message.getId();
        } catch (Exception e) {
            log.error("Failed to publish delay message: name={}, group={}, delay={}", name, group, delaySeconds, e);
            throw new RuntimeException("Failed to publish delay message", e);
        }
    }

    @Override
    @Transactional
    public String publishTransactional(String name, Object content) {
        return publishTransactional(name, content, capProperties.getDefaultGroup());
    }

    @Override
    @Transactional
    public String publishTransactional(String name, Object content, String group) {
        try {
            CapMessage message = buildMessage(name, content, group);

            // 在事务中存储消息
            if (!messageStorage.store(message)) {
                throw new RuntimeException("Failed to store transactional message: " + message.getId());
            }

            // 注意：队列发送在事务提交后异步进行，这里只是记录
            log.info("Stored transactional message: {} (will be sent after transaction commit)", message.getId());
            return message.getId();
        } catch (Exception e) {
            log.error("Failed to publish transactional message: name={}, group={}", name, group, e);
            throw new RuntimeException("Failed to publish transactional message", e);
        }
    }

    /**
     * 构建消息对象
     */
    private CapMessage buildMessage(String name, Object content, String group) {
        try {
            return CapMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .name(name)
                    .content(objectMapper.writeValueAsString(content))
                    .group(group)
                    .status(CapMessage.MessageStatus.PENDING)
                    .retries(0)
                    .maxRetries(capProperties.getRetry().getMaxRetries())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message content", e);
        }
    }

    /**
     * 构建队列名称
     */
    private String buildQueueName(String name, String group) {
        return capProperties.getMessageQueue().getQueuePrefix() + name + "_" + group;
    }
}