package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CAP 消息清理处理器
 * 参考 .NET Core CAP 的 Collector 处理器
 * 负责清理过期的消息
 */
@Slf4j
@Component
public class MessageCollectorProcessor {

    private final CapProperties properties;
    private final MessageStorage messageStorage;
    private final ScheduledExecutorService scheduler;

    @Autowired
    public MessageCollectorProcessor(CapProperties properties, MessageStorage messageStorage) {
        this.properties = properties;
        this.messageStorage = messageStorage;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        startCollectorProcessor();
    }

    /**
     * 启动清理处理器
     */
    private void startCollectorProcessor() {
        scheduler.scheduleWithFixedDelay(
            this::cleanupExpiredMessages,
            properties.getCollectorCleaningInterval(),
            properties.getCollectorCleaningInterval(),
            TimeUnit.SECONDS
        );

        log.info("CAP message collector processor started with interval: {} seconds", 
                properties.getCollectorCleaningInterval());
    }

    /**
     * 清理过期消息
     */
    private void cleanupExpiredMessages() {
        try {
            log.debug("Starting to cleanup expired messages");

            // 清理成功的发布消息
            LocalDateTime succeedExpiredTime = LocalDateTime.now()
                .minusSeconds(properties.getSucceedMessageExpiredAfter());
            
            messageStorage.deleteExpiresAsync("cap.published", succeedExpiredTime, properties.getSchedulerBatchSize())
                .thenAccept(count -> {
                    if (count > 0) {
                        log.debug("Cleaned up {} expired published messages", count);
                    }
                })
                .exceptionally(ex -> {
                    log.error("Error cleaning up expired published messages", ex);
                    return null;
                });

            // 清理成功的接收消息
            messageStorage.deleteExpiresAsync("cap.received", succeedExpiredTime, properties.getSchedulerBatchSize())
                .thenAccept(count -> {
                    if (count > 0) {
                        log.debug("Cleaned up {} expired received messages", count);
                    }
                })
                .exceptionally(ex -> {
                    log.error("Error cleaning up expired received messages", ex);
                    return null;
                });

            // 清理失败的发布消息
            LocalDateTime failedExpiredTime = LocalDateTime.now()
                .minusSeconds(properties.getFailedMessageExpiredAfter());
            
            messageStorage.deleteExpiresAsync("cap.published", failedExpiredTime, properties.getSchedulerBatchSize())
                .thenAccept(count -> {
                    if (count > 0) {
                        log.debug("Cleaned up {} failed published messages", count);
                    }
                })
                .exceptionally(ex -> {
                    log.error("Error cleaning up failed published messages", ex);
                    return null;
                });

            // 清理失败的接收消息
            messageStorage.deleteExpiresAsync("cap.received", failedExpiredTime, properties.getSchedulerBatchSize())
                .thenAccept(count -> {
                    if (count > 0) {
                        log.debug("Cleaned up {} failed received messages", count);
                    }
                })
                .exceptionally(ex -> {
                    log.error("Error cleaning up failed received messages", ex);
                    return null;
                });

        } catch (Exception ex) {
            log.error("Error in message collector processor", ex);
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
        log.info("CAP message collector processor shutdown");
    }
} 