package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CAP 消息重试处理器
 * 参考 .NET Core CAP 的 MessageNeedToRetryProcessor
 * 负责处理失败消息的重试逻辑
 */
@Slf4j
@Component
public class MessageRetryProcessor {

    private final CapProperties properties;
    private final MessageStorage messageStorage;
    private final MessageDispatcher messageDispatcher;
    private final ScheduledExecutorService scheduler;

    // 最小建议的回退窗口回溯时间（秒）
    private static final int MIN_SUGGESTED_FALLBACK_WINDOW_LOOKBACK_SECONDS = 30;

    @Autowired
    public MessageRetryProcessor(CapProperties properties, 
                                MessageStorage messageStorage,
                                MessageDispatcher messageDispatcher) {
        this.properties = properties;
        this.messageStorage = messageStorage;
        this.messageDispatcher = messageDispatcher;
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        checkSafeOptionsSet();
        startRetryProcessor();
    }

    /**
     * 启动重试处理器
     */
    private void startRetryProcessor() {
        // 启动发布消息重试处理器
        scheduler.scheduleWithFixedDelay(
            this::processPublishedRetry,
            properties.getFailedRetryInterval(),
            properties.getFailedRetryInterval(),
            TimeUnit.SECONDS
        );

        // 启动接收消息重试处理器
        scheduler.scheduleWithFixedDelay(
            this::processReceivedRetry,
            properties.getFailedRetryInterval(),
            properties.getFailedRetryInterval(),
            TimeUnit.SECONDS
        );

        log.info("CAP message retry processor started with interval: {} seconds", 
                properties.getFailedRetryInterval());
    }

    /**
     * 处理发布消息重试
     */
    private void processPublishedRetry() {
        try {
            if (properties.isUseStorageLock()) {
                String lockKey = "publish_retry_" + properties.getVersion();
                Duration ttl = Duration.ofSeconds(properties.getFailedRetryInterval() + 10);
                String instance = getInstanceId();

                messageStorage.acquireLockAsync(lockKey, ttl, instance)
                    .thenCompose(acquired -> {
                        if (!acquired) {
                            log.debug("Failed to acquire publish retry lock");
                            return CompletableFuture.completedFuture(null);
                        }
                        return processPublishedMessages()
                            .thenCompose(v -> messageStorage.releaseLockAsync(lockKey, instance));
                    })
                    .exceptionally(ex -> {
                        log.error("Error processing published retry messages", ex);
                        return null;
                    });
            } else {
                processPublishedMessages()
                    .exceptionally(ex -> {
                        log.error("Error processing published retry messages", ex);
                        return null;
                    });
            }
        } catch (Exception ex) {
            log.error("Error in published retry processor", ex);
        }
    }

    /**
     * 处理接收消息重试
     */
    private void processReceivedRetry() {
        try {
            if (properties.isUseStorageLock()) {
                String lockKey = "received_retry_" + properties.getVersion();
                Duration ttl = Duration.ofSeconds(properties.getFailedRetryInterval() + 10);
                String instance = getInstanceId();

                messageStorage.acquireLockAsync(lockKey, ttl, instance)
                    .thenCompose(acquired -> {
                        if (!acquired) {
                            log.debug("Failed to acquire received retry lock");
                            return CompletableFuture.completedFuture(null);
                        }
                        return processReceivedMessages()
                            .thenCompose(v -> messageStorage.releaseLockAsync(lockKey, instance));
                    })
                    .exceptionally(ex -> {
                        log.error("Error processing received retry messages", ex);
                        return null;
                    });
            } else {
                processReceivedMessages()
                    .exceptionally(ex -> {
                        log.error("Error processing received retry messages", ex);
                        return null;
                    });
            }
        } catch (Exception ex) {
            log.error("Error in received retry processor", ex);
        }
    }

    /**
     * 处理发布消息
     */
    private CompletableFuture<Void> processPublishedMessages() {
        Duration lookbackSeconds = Duration.ofSeconds(properties.getFallbackWindowLookbackSeconds());
        
        return messageStorage.getPublishedMessagesOfNeedRetry(lookbackSeconds)
            .thenCompose(messages -> {
                if (messages.isEmpty()) {
                    return CompletableFuture.completedFuture(null);
                }

                log.debug("Found {} published messages need retry", messages.size());
                
                List<CompletableFuture<Void>> futures = messages.stream()
                    .map((Function<CapMessage, CompletableFuture<Void>>) message -> messageDispatcher.enqueueToPublish(message)
                        .exceptionally(ex -> {
                            log.error("Failed to enqueue published message for retry: {}", message.getId(), ex);
                            return null;
                        }))
                    .toList();

                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            })
            .exceptionally(ex -> {
                log.warn("Failed to get published messages for retry", ex);
                return null;
            });
    }

    /**
     * 处理接收消息
     */
    private CompletableFuture<Void> processReceivedMessages() {
        Duration lookbackSeconds = Duration.ofSeconds(properties.getFallbackWindowLookbackSeconds());
        
        return messageStorage.getReceivedMessagesOfNeedRetry(lookbackSeconds)
            .thenCompose(messages -> {
                if (messages.isEmpty()) {
                    return CompletableFuture.completedFuture(null);
                }

                log.debug("Found {} received messages need retry", messages.size());
                
                List<CompletableFuture<Void>> futures = messages.stream()
                    .map((Function<CapMessage, CompletableFuture<Void>>) message -> messageDispatcher.enqueueToExecute(message)
                        .exceptionally(ex -> {
                            log.error("Failed to enqueue received message for retry: {}", message.getId(), ex);
                            return null;
                        }))
                    .toList();

                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            })
            .exceptionally(ex -> {
                log.warn("Failed to get received messages for retry", ex);
                return null;
            });
    }

    /**
     * 获取实例ID
     */
    private String getInstanceId() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            int workerId = Math.abs(hostname.hashCode() % 1023);
            return hostname + "_" + workerId;
        } catch (Exception ex) {
            return "unknown_" + System.currentTimeMillis();
        }
    }

    /**
     * 检查安全选项设置
     */
    private void checkSafeOptionsSet() {
        if (properties.getFallbackWindowLookbackSeconds() < MIN_SUGGESTED_FALLBACK_WINDOW_LOOKBACK_SECONDS) {
            log.warn("The provided FallbackWindowLookbackSeconds of {} is set to a value lower than {} seconds. " +
                    "This might cause unwanted unsafe behavior if the consumer takes more than the provided " +
                    "FallbackWindowLookbackSeconds to execute.",
                    properties.getFallbackWindowLookbackSeconds(),
                    MIN_SUGGESTED_FALLBACK_WINDOW_LOOKBACK_SECONDS);
        }
    }

    /**
     * 关闭处理器
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("CAP message retry processor shutdown");
    }
} 