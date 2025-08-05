package com.guanwei.framework.cap.impl;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.CapSubscriber;
import com.guanwei.framework.cap.queue.CapQueueManager;
import com.guanwei.framework.cap.queue.MessageQueue;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import com.guanwei.framework.cap.CapMessageStatus;

/**
 * CAP 订阅者实现类
 * 负责消息的订阅、消费和处理
 * 参考 GitHub CAP 源码的消息处理机制
 */
@Slf4j
public class CapSubscriberImpl implements CapSubscriber {

    private final MessageStorage messageStorage;
    private final MessageQueue messageQueue;
    private final CapProperties capProperties;
    private final CapQueueManager capQueueManager;

    private final Map<String, Consumer<CapMessage>> handlers = new ConcurrentHashMap<>();
    private final Map<String, CapSubscriber.MessageHandler<?>> typedHandlers = new ConcurrentHashMap<>();
    private ExecutorService consumerExecutor;
    private ScheduledExecutorService scheduler;
    private volatile boolean running = true;

    public CapSubscriberImpl(MessageStorage messageStorage, MessageQueue messageQueue, 
                           CapProperties capProperties) {
        this.messageStorage = messageStorage;
        this.messageQueue = messageQueue;
        this.capProperties = capProperties;
        this.capQueueManager = null;
        
        // 初始化默认线程池，在@PostConstruct中重新配置
        this.consumerExecutor = Executors.newFixedThreadPool(4);
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @PostConstruct
    public void start() {
        log.info("Starting CAP Subscriber...");
        
        try {
            // 重新配置线程池大小
            if (capProperties != null && capProperties.getMessageQueue() != null) {
                // 关闭旧的线程池
                if (consumerExecutor != null && !consumerExecutor.isShutdown()) {
                    consumerExecutor.shutdown();
                    try {
                        if (!consumerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                            consumerExecutor.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        consumerExecutor.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                }

                // 创建新的线程池
                int consumerThreads = capProperties.getConsumerThreadCount();
                consumerExecutor = new ThreadPoolExecutor(
                        consumerThreads,
                        consumerThreads,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.CallerRunsPolicy());

                // 启动消息消费调度器
                long pollInterval = 1000; // 可根据需要改为capProperties.getCollectorCleaningInterval()或自定义字段
                scheduler.scheduleWithFixedDelay(
                        this::consumeMessages,
                        0,
                        pollInterval,
                        TimeUnit.MILLISECONDS);

                // 启动清理过期消息的调度器
                long cleanupInterval = capProperties.getCollectorCleaningInterval();
                scheduler.scheduleWithFixedDelay(
                        this::cleanupExpiredMessages,
                        cleanupInterval,
                        cleanupInterval,
                        TimeUnit.SECONDS);

                log.info("CAP Subscriber started with {} consumer threads, poll interval: {}ms",
                        consumerThreads, pollInterval);
            } else {
                log.warn("CAP Properties not available, using default configuration");
                
                // 使用默认配置
                scheduler.scheduleWithFixedDelay(
                        this::consumeMessages,
                        0,
                        1000, // 1秒轮询间隔
                        TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.error("Failed to start CAP Subscriber", e);
            throw new RuntimeException("Failed to start CAP Subscriber", e);
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping CAP Subscriber...");
        running = false;
        
        if (consumerExecutor != null) {
            consumerExecutor.shutdown();
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }

        try {
            if (consumerExecutor != null && !consumerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                consumerExecutor.shutdownNow();
            }
            if (scheduler != null && !scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (consumerExecutor != null) {
                consumerExecutor.shutdownNow();
            }
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
        }

        log.info("CAP Subscriber stopped");
    }

    @Override
    public void subscribe(String name, Consumer<CapMessage> handler) {
        subscribe(name, capProperties != null ? capProperties.getDefaultGroupName() : "default", handler);
    }

    @Override
    public void subscribe(String name, String group, Consumer<CapMessage> handler) {
        String key = buildHandlerKey(name, group);
        handlers.put(key, handler);
        
        // 确保队列存在并正确绑定
        ensureQueueExists(name, group);
        
        log.info("Subscribed to message: {} (group: {})", name, group);
    }

    @Override
    public <T> void subscribe(String name, CapSubscriber.MessageHandler<T> handler) {
        subscribe(name, capProperties != null ? capProperties.getDefaultGroupName() : "default", handler);
    }

    @Override
    public <T> void subscribe(String name, String group, CapSubscriber.MessageHandler<T> handler) {
        String key = buildHandlerKey(name, group);
        typedHandlers.put(key, handler);
        
        // 确保队列存在并正确绑定
        ensureQueueExists(name, group);
        
        log.info("Subscribed to typed message: {} (group: {})", name, group);
    }

    @Override
    public void unsubscribe(String name) {
        unsubscribe(name, capProperties != null ? capProperties.getDefaultGroupName() : "default");
    }

    @Override
    public void unsubscribe(String name, String group) {
        String key = buildHandlerKey(name, group);
        handlers.remove(key);
        typedHandlers.remove(key);
        log.info("Unsubscribed from message: {} (group: {})", name, group);
    }

    /**
     * 确保队列存在并正确绑定
     * 参考 GitHub CAP 源码：在订阅时创建队列和绑定
     */
    private void ensureQueueExists(String messageName, String group) {
        try {
            if (capQueueManager != null) {
                // 使用队列管理器创建队列并绑定
                String queueName = capQueueManager.createQueueAndBind(messageName, group);
                log.info("Ensured queue exists and bound: {} for message: {} (group: {})", 
                        queueName, messageName, group);
            } else {
                log.debug("Queue manager not available, queue will be created when first message is sent");
            }
        } catch (Exception e) {
            log.error("Failed to ensure queue exists for message: {} (group: {})", messageName, group, e);
        }
    }

    /**
     * 消费消息
     */
    private void consumeMessages() {
        if (!running) {
            return;
        }

        try {
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
        } catch (Exception e) {
            log.error("Error in consumeMessages", e);
        }
    }

    /**
     * 为指定处理器消费消息
     */
    private void consumeMessagesForHandler(String name, String group, Consumer<CapMessage> handler) {
        String queueName = buildQueueName(name, group);

        try {
            int batchSize = capProperties.getSchedulerBatchSize();
            List<CapMessage> messages = messageQueue.receiveBatch(
                    queueName,
                    batchSize,
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
            int batchSize = capProperties.getSchedulerBatchSize();
            List<CapMessage> messages = messageQueue.receiveBatch(
                    queueName,
                    batchSize,
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
        if (consumerExecutor == null || consumerExecutor.isShutdown()) {
            log.warn("Consumer executor is not available, processing message synchronously");
            try {
                handler.accept(message);
                messageQueue.acknowledge(queueName, message.getId());
            } catch (Exception e) {
                log.error("Failed to process message: {}", message.getId(), e);
                handleMessageError(message, queueName);
            }
            return;
        }

        consumerExecutor.submit(() -> {
            try {
                // 更新消息状态为重试中
                messageStorage.updateStatusAsync(message.getId(), CapMessageStatus.RETRYING);

                // 执行处理器
                handler.accept(message);

                // 更新消息状态为成功
                messageStorage.updateStatusAsync(message.getId(), CapMessageStatus.SUCCEEDED);
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
        if (consumerExecutor == null || consumerExecutor.isShutdown()) {
            log.warn("Consumer executor is not available, processing typed message synchronously");
            try {
                Object result = handler.handle(message);
                messageQueue.acknowledge(queueName, message.getId());
                log.debug("Successfully processed typed message: {} -> {}", message.getId(), result);
            } catch (Exception e) {
                log.error("Failed to process typed message: {}", message.getId(), e);
                handleMessageError(message, queueName);
            }
            return;
        }

        consumerExecutor.submit(() -> {
            try {
                // 更新消息状态为重试中
                messageStorage.updateStatusAsync(message.getId(), CapMessageStatus.RETRYING);

                // 执行处理器
                Object result = handler.handle(message);

                // 更新消息状态为成功
                messageStorage.updateStatusAsync(message.getId(), CapMessageStatus.SUCCEEDED);
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
        message.incrementRetries();

        // 检查是否超过最大重试次数
        Integer retries = message.getRetries();
        if (retries != null && retries >= capProperties.getFailedRetryCount()) {
            // 超过最大重试次数，标记为失败
            messageStorage.updateStatusAsync(message.getId(), CapMessageStatus.FAILED);
            messageQueue.reject(queueName, message.getId(), false);
            log.error("Message {} exceeded max retries ({})", message.getId(), capProperties.getFailedRetryCount());
        } else {
            // 重新入队重试
            messageStorage.updateStatusAsync(message.getId(), CapMessageStatus.PENDING);
            messageQueue.reject(queueName, message.getId(), true);
            log.warn("Message {} will be retried (attempt {}/{})",
                    message.getId(), message.getRetries(), capProperties.getFailedRetryCount());
        }
    }

    /**
     * 清理过期消息
     */
    private void cleanupExpiredMessages() {
        try {
            if (capProperties != null) {
                long expired = capProperties.getSucceedMessageExpiredAfter();
                int deleted = 0;
                try {
                    deleted = messageStorage.deleteExpiredMessagesAsync(CapMessageStatus.SUCCEEDED, expired).get();
                } catch (Exception e) {
                    log.error("Failed to delete expired messages", e);
                }
                if (deleted > 0) {
                    log.info("Cleaned up {} expired messages", deleted);
                }
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
     * 参考 GitHub CAP 源码：routeKey + "." + groupName
     */
    private String buildQueueName(String name, String group) {
        if (capQueueManager != null) {
            return capQueueManager.buildQueueName(name, group);
        }
        return name + "." + group;
    }
}