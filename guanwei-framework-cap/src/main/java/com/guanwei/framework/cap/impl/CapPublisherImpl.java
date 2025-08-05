package com.guanwei.framework.cap.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapMessageStatus;
import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.CapPublisher;
import com.guanwei.framework.cap.CapTransaction;
import com.guanwei.framework.cap.CapTransactionManager;
import com.guanwei.framework.cap.queue.MessageQueue;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * CAP 发布者实现类
 * 负责消息的发布和事务性消息处理
 * 参考 GitHub CAP 源码的消息发布机制
 */
@Slf4j
public class CapPublisherImpl implements CapPublisher {

    private final MessageQueue messageQueue;
    private final MessageStorage messageStorage;
    private final CapProperties capProperties;
    private final CapTransactionManager transactionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CapPublisherImpl(MessageQueue messageQueue, MessageStorage messageStorage, 
                           CapProperties capProperties, CapTransactionManager transactionManager) {
        this.messageQueue = messageQueue;
        this.messageStorage = messageStorage;
        this.capProperties = capProperties;
        this.transactionManager = transactionManager;
    }

    @Override
    public String publish(String name, Object content) {
        return publish(name, content, capProperties != null ? capProperties.getDefaultGroupName() : "default");
    }

    @Override
    public String publish(String name, Object content, String group) {
        return publishInternal(name, content, null, group, null, false);
    }

    @Override
    public String publish(String name, Object content, String callbackName, String group) {
        return publishInternal(name, content, callbackName, group, null, false);
    }

    @Override
    public String publish(String name, Object content, Map<String, String> headers) {
        return publishInternal(name, content, null, capProperties != null ? capProperties.getDefaultGroupName() : "default",
                headers, false);
    }

    @Override
    public String publish(String name, Object content, Map<String, String> headers, String group) {
        return publishInternal(name, content, null, group, headers, false);
    }

    @Override
    public CompletableFuture<String> publishAsync(String name, Object content) {
        return CompletableFuture.supplyAsync(() -> publish(name, content));
    }

    @Override
    public CompletableFuture<String> publishAsync(String name, Object content, String group) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, group));
    }

    @Override
    public CompletableFuture<String> publishAsync(String name, Object content, String callbackName, String group) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, callbackName, group));
    }

    @Override
    public CompletableFuture<String> publishAsync(String name, Object content, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, headers));
    }

    @Override
    public CompletableFuture<String> publishAsync(String name, Object content, Map<String, String> headers,
            String group) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, headers, group));
    }

    @Override
    public String publishDelay(String name, Object content, long delaySeconds) {
        return publishDelay(name, content, capProperties != null ? capProperties.getDefaultGroupName() : "default",
                delaySeconds);
    }

    @Override
    public String publishDelay(String name, Object content, String group, long delaySeconds) {
        return publishInternal(name, content, null, group, null, false, delaySeconds);
    }

    @Override
    public String publishDelay(String name, Object content, String callbackName, String group, long delaySeconds) {
        return publishInternal(name, content, callbackName, group, null, false, delaySeconds);
    }

    @Override
    public String publishDelay(String name, Object content, Map<String, String> headers, long delaySeconds) {
        return publishInternal(name, content, null, capProperties != null ? capProperties.getDefaultGroupName() : "default",
                headers, false, delaySeconds);
    }

    @Override
    public String publishDelay(String name, Object content, Map<String, String> headers, String group,
            long delaySeconds) {
        return publishInternal(name, content, null, group, headers, false, delaySeconds);
    }

    @Override
    public CompletableFuture<String> publishDelayAsync(String name, Object content, long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, delaySeconds));
    }

    @Override
    public CompletableFuture<String> publishDelayAsync(String name, Object content, String group, long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, group, delaySeconds));
    }

    @Override
    public CompletableFuture<String> publishDelayAsync(String name, Object content, String callbackName, String group,
            long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, callbackName, group, delaySeconds));
    }

    @Override
    public CompletableFuture<String> publishDelayAsync(String name, Object content, Map<String, String> headers,
            long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, headers, delaySeconds));
    }

    @Override
    public CompletableFuture<String> publishDelayAsync(String name, Object content, Map<String, String> headers,
            String group, long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, headers, group, delaySeconds));
    }

    @Override
    public String publishTransactional(String name, Object content) {
        return publishTransactional(name, content, capProperties != null ? capProperties.getDefaultGroupName() : "default");
    }

    @Override
    public String publishTransactional(String name, Object content, String group) {
        return publishInternal(name, content, null, group, null, true);
    }

    @Override
    public String publishTransactional(String name, Object content, String callbackName, String group) {
        return publishInternal(name, content, callbackName, group, null, true);
    }

    @Override
    public String publishTransactional(String name, Object content, Map<String, String> headers) {
        return publishInternal(name, content, null, capProperties != null ? capProperties.getDefaultGroupName() : "default",
                headers, true);
    }

    @Override
    public String publishTransactional(String name, Object content, Map<String, String> headers, String group) {
        return publishInternal(name, content, null, group, headers, true);
    }

    @Override
    public CompletableFuture<String> publishTransactionalAsync(String name, Object content) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content));
    }

    @Override
    public CompletableFuture<String> publishTransactionalAsync(String name, Object content, String group) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content, group));
    }

    @Override
    public CompletableFuture<String> publishTransactionalAsync(String name, Object content, String callbackName,
            String group) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content, callbackName, group));
    }

    @Override
    public CompletableFuture<String> publishTransactionalAsync(String name, Object content,
            Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content, headers));
    }

    @Override
    public CompletableFuture<String> publishTransactionalAsync(String name, Object content, Map<String, String> headers,
            String group) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content, headers, group));
    }

    /**
     * 内部发布方法
     */
    private String publishInternal(String name, Object content, String callbackName, String group,
            Map<String, String> headers, boolean transactional) {
        return publishInternal(name, content, callbackName, group, headers, transactional, null);
    }

    /**
     * 内部发布方法（支持延迟）
     */
    private String publishInternal(String name, Object content, String callbackName, String group,
            Map<String, String> headers, boolean transactional, Long delaySeconds) {
        try {
            // 生成消息ID
            String messageId = UUID.randomUUID().toString();

            // 创建CAP消息
            CapMessage capMessage = new CapMessage(name, content);
            capMessage.setDbId(messageId);
            capMessage.setGroup(group);
            capMessage.setStatus(CapMessageStatus.SCHEDULED);
            capMessage.setRetries(0);
            capMessage.setAdded(LocalDateTime.now());
            capMessage.setVersion("v1");

            // 设置延迟时间
            if (delaySeconds != null) {
                capMessage.setExpiresAt(LocalDateTime.now().plusSeconds(delaySeconds));
            }

            // 设置消息头
            if (headers != null) {
                capMessage.setHeaders(new HashMap<>(headers));
            }

            // 构建队列名称：routeKey + "." + groupName
            String queueName = buildQueueName(name, group);

            // 如果是事务性消息，检查是否有活动的事务
            if (transactional && transactionManager != null && transactionManager.hasActiveTransaction()) {
                // 在事务中存储消息，但不立即发送
                try {
                    CapMessage storedMessage = messageStorage.storeMessageAsync(name, content, null).get();
                    if (storedMessage == null) {
                        throw new RuntimeException("Failed to store transactional message");
                    }
                    log.info("Stored transactional message: {} in current transaction", messageId);
                    return messageId;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store transactional message", e);
                }
            } else {
                // 存储消息
                try {
                    CapMessage storedMessage = messageStorage.storeMessageAsync(name, content, null).get();
                    if (storedMessage == null) {
                        throw new RuntimeException("Failed to store message");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store message", e);
                }

                // 发送到消息队列
                boolean success;
                if (delaySeconds != null) {
                    success = messageQueue.sendDelay(queueName, capMessage, delaySeconds);
                } else {
                    success = messageQueue.send(queueName, capMessage);
                }

                if (success) {
                    log.info("Successfully published message: {} to queue: {} (delay: {}s, transactional: {})",
                            messageId, queueName, delaySeconds, transactional);
                    return messageId;
                } else {
                    log.error("Failed to publish message: {} to queue: {}", messageId, queueName);
                    throw new RuntimeException("Failed to publish message to queue");
                }
            }
        } catch (Exception e) {
            log.error("Error publishing message: {} to group: {} (delay: {}s, transactional: {})",
                    name, group, delaySeconds, transactional, e);
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    /**
     * 构建队列名称
     * 参考 GitHub CAP 源码：routeKey + "." + groupName
     */
    private String buildQueueName(String name, String group) {
        return name + "." + group;
    }
}