package com.enterprise.framework.cap.impl;

import com.enterprise.framework.cap.CapMessage;
import com.enterprise.framework.cap.CapProperties;
import com.enterprise.framework.cap.CapSubscriber;
import com.enterprise.framework.cap.queue.MessageQueue;
import com.enterprise.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * CAP 订阅者实现类
 * 负责消息的订阅、消费和处理
 */
@Slf4j
@Component
public class CapSubscriberImpl implements CapSubscriber {

    @Autowired
    private MessageStorage messageStorage;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private CapProperties capProperties;

    private final Map<String, Consumer<CapMessage>> handlers = new ConcurrentHashMap<>();
    private final Map<String, CapSubscriber.MessageHandler<?>> typedHandlers = new ConcurrentHashMap<>();
    private final ExecutorService consumerExecutor;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running = true;

    public CapSubscriberImpl() {
        this.consumerExecutor = Executors.newFixedThreadPool(
                capProperties.getMessageQueue().getConsumerThreads());
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @PostConstruct
    public void start() {
        // 启动消息消费调度器
        scheduler.scheduleWithFixedDelay(
                this::consumeMessages,
                0,
                capProperties.getMessageQueue().getPollInterval(),
                TimeUnit.MILLISECONDS);

        // 启动清理过期消息的调度器
        scheduler.scheduleWithFixedDelay(
                this::cleanupExpiredMessages,
                capProperties.getStorage().getCleanupInterval(),
                capProperties.getStorage().getCleanupInterval(),
                TimeUnit.SECONDS);

        log.info("CAP Subscriber started with {} consumer threads",
                capProperties.getMessageQueue().getConsumerThreads());
    }

    @PreDestroy
    public void stop() {
        running = false;
        consumerExecutor.shutdown();
        scheduler.shutdown();

        try {
            if (!consumerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                consumerExecutor.shutdownNow();
            }
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            consumerExecutor.shutdownNow();
            scheduler.shutdownNow();
        }

        log.info("CAP Subscriber stopped");
    }

    @Override
    public void subscribe(String name, Consumer<CapMessage> handler) {
        subscribe(name, capProperties.getDefaultGroup(), handler);
    }

    @Override
    public void subscribe(String name, String group, Consumer<CapMessage> handler) {
        String key = buildHandlerKey(name, group);
        handlers.put(key, handler);
        log.info("Subscribed to message: {} (group: {})", name, group);
    }

    @Override
    public <T> void subscribe(String name, CapSubscriber.MessageHandler<T> handler) {
        subscribe(name, capProperties.getDefaultGroup(), handler);
    }

    @Override
    public <T> void subscribe(String name, String group, CapSubscriber.MessageHandler<T> handler) {
        String key = buildHandlerKey(name, group);
        typedHandlers.put(key, handler);
        log.info("Subscribed to typed message: {} (group: {})", name, group);
    }

    @Override
    public void unsubscribe(String name) {
        unsubscribe(name, capProperties.getDefaultGroup());
    }

    @Override
    public void unsubscribe(String name, String group) {
        String key = buildHandlerKey(name, group);
        handlers.remove(key);
        typedHandlers.remove(key);
        log.info("Unsubscribed from message: {} (group: {})", name, group);
    }

    /**
     * 消费消息
     */
    private void consumeMessages() {
        if (!running) {
            return;
        }

        // 处理所有注册的处理器
        handlers.forEach((key, handler) -> {
            String[] parts = key.split(":");
            String name = parts[0];
            String group = parts[1];

            consumeMessagesForHandler(name, group, handler);
        });

        // 处理带类型的处理器
        typedHandlers.forEach((key, handler) -> {
            String[] parts = key.split(":");
            String name = parts[0];
            String group = parts[1];

            consumeMessagesForTypedHandler(name, group, handler);
        });
    }

    /**
     * 为指定处理器消费消息
     */
    private void consumeMessagesForHandler(String name, String group, Consumer<CapMessage> handler) {
        String queueName = buildQueueName(name, group);

        try {
            List<CapMessage> messages = messageQueue.receiveBatch(
                    queueName,
                    capProperties.getMessageQueue().getBatchSize(),
                    1000 // 1秒超时
            );

            for (CapMessage message : messages) {
                processMessage(message, handler, queueName);
            }
        } catch (Exception e) {
            log.error("Error consuming messages for {}:{}", name, group, e);
        }
    }

    /**
     * 为指定类型处理器消费消息
     */
    @SuppressWarnings("unchecked")
    private void consumeMessagesForTypedHandler(String name, String group, CapSubscriber.MessageHandler<?> handler) {
        String queueName = buildQueueName(name, group);

        try {
            List<CapMessage> messages = messageQueue.receiveBatch(
                    queueName,
                    capProperties.getMessageQueue().getBatchSize(),
                    1000 // 1秒超时
            );

            for (CapMessage message : messages) {
                processTypedMessage(message, (CapSubscriber.MessageHandler<Object>) handler, queueName);
            }
        } catch (Exception e) {
            log.error("Error consuming typed messages for {}:{}", name, group, e);
        }
    }

    /**
     * 处理消息
     */
    private void processMessage(CapMessage message, Consumer<CapMessage> handler, String queueName) {
        consumerExecutor.submit(() -> {
            try {
                // 更新消息状态为重试中
                messageStorage.updateStatus(message.getId(), CapMessage.MessageStatus.RETRYING);

                // 执行处理器
                handler.accept(message);

                // 更新消息状态为成功
                messageStorage.updateStatus(message.getId(), CapMessage.MessageStatus.SUCCEEDED);
                messageQueue.acknowledge(queueName, message.getId());

                log.debug("Successfully processed message: {}", message.getId());
            } catch (Exception e) {
                log.error("Failed to process message: {}", message.getId(), e);
                handleMessageError(message, queueName);
            }
        });
    }

    /**
     * 处理类型消息
     */
    private void processTypedMessage(CapMessage message, CapSubscriber.MessageHandler<Object> handler,
            String queueName) {
        consumerExecutor.submit(() -> {
            try {
                // 更新消息状态为重试中
                messageStorage.updateStatus(message.getId(), CapMessage.MessageStatus.RETRYING);

                // 执行处理器
                Object result = handler.handle(message);

                // 更新消息状态为成功
                messageStorage.updateStatus(message.getId(), CapMessage.MessageStatus.SUCCEEDED);
                messageQueue.acknowledge(queueName, message.getId());

                log.debug("Successfully processed typed message: {} -> {}", message.getId(), result);
            } catch (Exception e) {
                log.error("Failed to process typed message: {}", message.getId(), e);
                handleMessageError(message, queueName);
            }
        });
    }

    /**
     * 处理消息错误
     */
    private void handleMessageError(CapMessage message, String queueName) {
        // 增加重试次数
        messageStorage.incrementRetries(message.getId());

        // 检查是否超过最大重试次数
        if (message.getRetries() != null && message.getRetries() >= message.getMaxRetries()) {
            // 超过最大重试次数，标记为失败
            messageStorage.updateStatus(message.getId(), CapMessage.MessageStatus.FAILED);
            messageQueue.reject(queueName, message.getId(), false);
            log.error("Message {} exceeded max retries ({})", message.getId(), message.getMaxRetries());
        } else {
            // 重新入队重试
            messageStorage.updateStatus(message.getId(), CapMessage.MessageStatus.PENDING);
            messageQueue.reject(queueName, message.getId(), true);
            log.warn("Message {} will be retried (attempt {}/{})",
                    message.getId(), message.getRetries(), message.getMaxRetries());
        }
    }

    /**
     * 清理过期消息
     */
    private void cleanupExpiredMessages() {
        try {
            int deletedCount = messageStorage.deleteExpiredMessages(
                    capProperties.getStorage().getMessageExpired());
            if (deletedCount > 0) {
                log.info("Cleaned up {} expired messages", deletedCount);
            }
        } catch (Exception e) {
            log.error("Error cleaning up expired messages", e);
        }
    }

    /**
     * 构建处理器键
     */
    private String buildHandlerKey(String name, String group) {
        return name + ":" + group;
    }

    /**
     * 构建队列名称
     */
    private String buildQueueName(String name, String group) {
        return capProperties.getMessageQueue().getQueuePrefix() + name + "_" + group;
    }
}