package com.guanwei.framework.cap.impl;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapMessageStatus;
import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.CapPublisher;
import com.guanwei.framework.cap.CapTransactionManager;
import com.guanwei.framework.cap.queue.MessageQueue;
import com.guanwei.framework.cap.processor.MessageDispatcher;
import com.guanwei.framework.cap.CapTransaction;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.guanwei.framework.cap.util.MessageIdGenerator;

/**
 * CAP 发布者实现类
 * 负责消息的发布和事务性消息处理
 * 参考 GitHub CAP 源码的消息发布机制
 */
@Slf4j
public class CapPublisherImpl implements CapPublisher {

    private final MessageQueue messageQueue;
    private final MessageDispatcher messageDispatcher;
    private final MessageStorage messageStorage;
    private final CapProperties capProperties;
    private final CapTransactionManager transactionManager;
    // no-op

    public CapPublisherImpl(MessageQueue messageQueue, MessageStorage messageStorage, 
                           CapProperties capProperties, CapTransactionManager transactionManager,
                           MessageDispatcher messageDispatcher) {
        this.messageQueue = messageQueue;
        this.messageStorage = messageStorage;
        this.capProperties = capProperties;
        this.transactionManager = transactionManager;
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public Long publish(String name, Object content) {
        return publish(name, content, capProperties != null ? capProperties.getDefaultGroupName() : "default");
    }

    @Override
    public Long publish(String name, Object content, String group) {
        return publishInternal(name, content, null, group, null, false);
    }

    @Override
    public Long publish(String name, Object content, String callbackName, String group) {
        return publishInternal(name, content, callbackName, group, null, false);
    }

    @Override
    public Long publish(String name, Object content, Map<String, String> headers) {
        return publishInternal(name, content, null, capProperties != null ? capProperties.getDefaultGroupName() : "default",
                headers, false);
    }

    @Override
    public Long publish(String name, Object content, Map<String, String> headers, String group) {
        return publishInternal(name, content, null, group, headers, false);
    }

    @Override
    public CompletableFuture<Long> publishAsync(String name, Object content) {
        return CompletableFuture.supplyAsync(() -> publish(name, content));
    }

    @Override
    public CompletableFuture<Long> publishAsync(String name, Object content, String group) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, group));
    }

    @Override
    public CompletableFuture<Long> publishAsync(String name, Object content, String callbackName, String group) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, callbackName, group));
    }

    @Override
    public CompletableFuture<Long> publishAsync(String name, Object content, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, headers));
    }

    @Override
    public CompletableFuture<Long> publishAsync(String name, Object content, Map<String, String> headers,
            String group) {
        return CompletableFuture.supplyAsync(() -> publish(name, content, headers, group));
    }

    @Override
    public Long publishDelay(String name, Object content, long delaySeconds) {
        return publishDelay(name, content, capProperties != null ? capProperties.getDefaultGroupName() : "default",
                delaySeconds);
    }

    @Override
    public Long publishDelay(String name, Object content, String group, long delaySeconds) {
        return publishInternal(name, content, null, group, null, false, delaySeconds);
    }

    @Override
    public Long publishDelay(String name, Object content, String callbackName, String group, long delaySeconds) {
        return publishInternal(name, content, callbackName, group, null, false, delaySeconds);
    }

    @Override
    public Long publishDelay(String name, Object content, Map<String, String> headers, long delaySeconds) {
        return publishInternal(name, content, null, capProperties != null ? capProperties.getDefaultGroupName() : "default",
                headers, false, delaySeconds);
    }

    @Override
    public Long publishDelay(String name, Object content, Map<String, String> headers, String group,
            long delaySeconds) {
        return publishInternal(name, content, null, group, headers, false, delaySeconds);
    }

    @Override
    public CompletableFuture<Long> publishDelayAsync(String name, Object content, long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, delaySeconds));
    }

    @Override
    public CompletableFuture<Long> publishDelayAsync(String name, Object content, String group, long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, group, delaySeconds));
    }

    @Override
    public CompletableFuture<Long> publishDelayAsync(String name, Object content, String callbackName, String group,
            long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, callbackName, group, delaySeconds));
    }

    @Override
    public CompletableFuture<Long> publishDelayAsync(String name, Object content, Map<String, String> headers,
            long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, headers, delaySeconds));
    }

    @Override
    public CompletableFuture<Long> publishDelayAsync(String name, Object content, Map<String, String> headers,
            String group, long delaySeconds) {
        return CompletableFuture.supplyAsync(() -> publishDelay(name, content, headers, group, delaySeconds));
    }

    @Override
    public Long publishTransactional(String name, Object content) {
        return publishTransactional(name, content, capProperties != null ? capProperties.getDefaultGroupName() : "default");
    }

    @Override
    public Long publishTransactional(String name, Object content, String group) {
        return publishInternal(name, content, null, group, null, true);
    }

    @Override
    public Long publishTransactional(String name, Object content, String callbackName, String group) {
        return publishInternal(name, content, callbackName, group, null, true);
    }

    @Override
    public Long publishTransactional(String name, Object content, Map<String, String> headers) {
        return publishInternal(name, content, null, capProperties != null ? capProperties.getDefaultGroupName() : "default",
                headers, true);
    }

    @Override
    public Long publishTransactional(String name, Object content, Map<String, String> headers, String group) {
        return publishInternal(name, content, null, group, headers, true);
    }

    @Override
    public CompletableFuture<Long> publishTransactionalAsync(String name, Object content) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content));
    }

    @Override
    public CompletableFuture<Long> publishTransactionalAsync(String name, Object content, String group) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content, group));
    }

    @Override
    public CompletableFuture<Long> publishTransactionalAsync(String name, Object content, String callbackName,
            String group) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content, callbackName, group));
    }

    @Override
    public CompletableFuture<Long> publishTransactionalAsync(String name, Object content,
            Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content, headers));
    }

    @Override
    public CompletableFuture<Long> publishTransactionalAsync(String name, Object content, Map<String, String> headers,
            String group) {
        return CompletableFuture.supplyAsync(() -> publishTransactional(name, content, headers, group));
    }

    /**
     * 内部发布方法
     */
    private Long publishInternal(String name, Object content, String callbackName, String group,
            Map<String, String> headers, boolean transactional) {
        return publishInternal(name, content, callbackName, group, headers, transactional, null);
    }

    /**
     * 内部发布方法（支持延迟）
     */
    private Long publishInternal(String name, Object content, String callbackName, String group,
            Map<String, String> headers, boolean transactional, Long delaySeconds) {
        try {
            // 生成消息ID（使用雪花算法避免重复）
            Long messageId = MessageIdGenerator.getInstance().nextId();

            // 创建CAP消息并统一使用 messageId 作为对外可见的消息ID
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

            // 构建队列名称（用于直接队列路径时）
            String queueName = buildQueueName(name, group); // 保留计算结果以便日志或扩展使用

            // 如果是事务性消息，检查是否有活动的事务
            if (transactional && transactionManager != null && transactionManager.hasActiveTransaction()) {
                // 在事务中存储消息，但不立即发送
                try {
                    CapMessage storedMessage = messageStorage.storeMessageAsync(name, capMessage, null).get();
                    if (storedMessage == null) {
                        throw new RuntimeException("Failed to store transactional message");
                    }
                    storedMessage.setDbId(messageId);
                    log.info("Stored transactional message: {} in current transaction", messageId);

                    // 注册事务提交后发布（与 .NET CAP 语义一致：在提交后才发送到MQ）
                    CapTransaction current = transactionManager.getCurrentTransaction();
                    if (current != null) {
                        current.addTransactionListener(new CapTransaction.TransactionListener() {
                            @Override
                            public void beforeCommit(CapTransaction transaction) { }

                            @Override
                            public void afterCommit(CapTransaction transaction) {
                                try {
                                    if (delaySeconds != null) {
                                        messageDispatcher.enqueueToScheduler(capMessage, java.time.LocalDateTime.now().plusSeconds(delaySeconds), null);
                                    } else {
                                        messageDispatcher.enqueueToPublish(capMessage);
                                    }
                                } catch (Exception ex) {
                                    log.error("Failed to enqueue message after commit: {}", messageId, ex);
                                }
                            }

                            @Override
                            public void beforeRollback(CapTransaction transaction) { }

                            @Override
                            public void afterRollback(CapTransaction transaction) { }
                        });
                    }
                    return messageId;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store transactional message", e);
                }
            } else {
                // 存储消息
                try {
                    CapMessage storedMessage = messageStorage.storeMessageAsync(name, capMessage, null).get();
                    if (storedMessage == null) {
                        throw new RuntimeException("Failed to store message");
                    }
                    storedMessage.setDbId(messageId);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store message", e);
                }

                // 通过分发器发送（保持与 .NET CAP 一致的分发路径）
                if (delaySeconds != null) {
                    messageDispatcher.enqueueToScheduler(capMessage, java.time.LocalDateTime.now().plusSeconds(delaySeconds), null);
                } else {
                    messageDispatcher.enqueueToPublish(capMessage);
                }
                log.info("Enqueued message for publish: {} (delay: {}s, transactional: {})", messageId, delaySeconds, transactional);
                return messageId;
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