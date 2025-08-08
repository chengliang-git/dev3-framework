package com.guanwei.framework.cap.storage;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapMessageStatus;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 内存消息存储实现
 * 完整实现，用于开发和测试环境，生产环境建议使用数据库存储
 */
@Slf4j
public class MemoryMessageStorage implements MessageStorage {

    private final Map<Long, CapMessage> publishedMessages = new ConcurrentHashMap<>();
    private final Map<Long, CapMessage> receivedMessages = new ConcurrentHashMap<>();
    private final Map<String, LockInfo> locks = new ConcurrentHashMap<>();
    private final AtomicLong messageIdCounter = new AtomicLong(0);

    @Override
    public CompletableFuture<Boolean> acquireLockAsync(String key, Duration ttl, String instance) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LockInfo existingLock = locks.get(key);
                if (existingLock != null && existingLock.isValid()) {
                    return false; // 锁已被其他实例持有
                }

                LockInfo newLock = new LockInfo(instance, LocalDateTime.now().plus(ttl));
                locks.put(key, newLock);
                return true;
            } catch (Exception e) {
                log.error("Error acquiring lock: {}", key, e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Void> releaseLockAsync(String key, String instance) {
        return CompletableFuture.runAsync(() -> {
            try {
                LockInfo lock = locks.get(key);
                if (lock != null && instance.equals(lock.getInstance())) {
                    locks.remove(key);
                }
            } catch (Exception e) {
                log.error("Error releasing lock: {}", key, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> renewLockAsync(String key, Duration ttl, String instance) {
        return CompletableFuture.runAsync(() -> {
            try {
                LockInfo lock = locks.get(key);
                if (lock != null && instance.equals(lock.getInstance())) {
                    lock.setExpiresAt(LocalDateTime.now().plus(ttl));
                }
            } catch (Exception e) {
                log.error("Error renewing lock: {}", key, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> changePublishStateToDelayedAsync(List<Long> ids) {
        return CompletableFuture.runAsync(() -> {
            try {
                for (Long id : ids) {
                    CapMessage message = publishedMessages.get(id);
                    if (message != null) {
                        message.setStatus(CapMessageStatus.DELAYED);
                    }
                }
            } catch (Exception e) {
                log.error("Error changing publish state to delayed", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> changePublishStateAsync(CapMessage message, CapMessageStatus status, Object transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (message != null && message.getId() != null) {
                    CapMessage storedMessage = publishedMessages.get(message.getId());
                    if (storedMessage != null) {
                        storedMessage.setStatus(status);
                    }
                }
            } catch (Exception e) {
                log.error("Error changing publish state", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> changeReceiveStateAsync(CapMessage message, CapMessageStatus status) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (message != null && message.getId() != null) {
                    CapMessage storedMessage = receivedMessages.get(message.getId());
                    if (storedMessage != null) {
                        storedMessage.setStatus(status);
                    }
                }
            } catch (Exception e) {
                log.error("Error changing receive state", e);
            }
        });
    }

    @Override
    public CompletableFuture<CapMessage> storeMessageAsync(String name, Object content, Object transaction) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CapMessage message;
                Long id;
                if (content instanceof CapMessage) {
                    message = (CapMessage) content;
                    id = message.getId() != null ? message.getId() : generateMessageId();
                    message.setDbId(id);
                    if (message.getStatus() == null) {
                        message.setStatus(CapMessageStatus.SCHEDULED);
                    }
                    if (message.getAdded() == null) {
                        message.setAdded(LocalDateTime.now());
                    }
                } else {
                    id = generateMessageId();
                    message = new CapMessage(name, content);
                    message.setDbId(id);
                    message.setStatus(CapMessageStatus.SCHEDULED);
                    message.setAdded(LocalDateTime.now());
                    message.setRetries(0);
                }

                publishedMessages.put(id, message);
                return message;
            } catch (Exception e) {
                log.error("Error storing published message", e);
                throw new RuntimeException("Failed to store published message", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> storeReceivedExceptionMessageAsync(String name, String group, String content) {
        return CompletableFuture.runAsync(() -> {
            try {
                Long id = generateMessageId();
                CapMessage exceptionMessage = new CapMessage(name, group, content);
                exceptionMessage.setDbId(id);
                exceptionMessage.setStatus(CapMessageStatus.FAILED);
                exceptionMessage.setAdded(LocalDateTime.now());
                
                receivedMessages.put(id, exceptionMessage);
            } catch (Exception e) {
                log.error("Error storing exception message", e);
            }
        });
    }

    @Override
    public CompletableFuture<CapMessage> storeReceivedMessageAsync(String name, String group, Object content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Long id = generateMessageId();
                CapMessage message = new CapMessage(name, group, content);
                message.setDbId(id);
                message.setStatus(CapMessageStatus.SCHEDULED);
                message.setAdded(LocalDateTime.now());
                message.setRetries(0);
                
                receivedMessages.put(id, message);
                return message;
            } catch (Exception e) {
                log.error("Error storing received message", e);
                throw new RuntimeException("Failed to store received message", e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteExpiresAsync(String table, LocalDateTime timeout, int batchCount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<Long, CapMessage> messageMap = "published".equals(table) ? publishedMessages : receivedMessages;
                return deleteExpiredMessages(messageMap, timeout, batchCount);
            } catch (Exception e) {
                log.error("Error deleting expired messages", e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<List<CapMessage>> getPublishedMessagesOfNeedRetry(Duration lookbackSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LocalDateTime cutoff = LocalDateTime.now().minus(lookbackSeconds);
                return publishedMessages.values().stream()
                    .filter(message -> message.getStatus() == CapMessageStatus.FAILED)
                    .filter(message -> message.getAdded() != null && message.getAdded().isAfter(cutoff))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Error getting published messages of need retry", e);
                return new java.util.ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<List<CapMessage>> getReceivedMessagesOfNeedRetry(Duration lookbackSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LocalDateTime cutoff = LocalDateTime.now().minus(lookbackSeconds);
                return receivedMessages.values().stream()
                    .filter(message -> message.getStatus() == CapMessageStatus.FAILED)
                    .filter(message -> message.getAdded() != null && message.getAdded().isAfter(cutoff))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Error getting received messages of need retry", e);
                return new java.util.ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteReceivedMessageAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CapMessage removed = receivedMessages.remove(parseId(id));
                return removed != null ? 1 : 0;
            } catch (Exception e) {
                log.error("Error deleting received message: {}", id, e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deletePublishedMessageAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CapMessage removed = publishedMessages.remove(parseId(id));
                return removed != null ? 1 : 0;
            } catch (Exception e) {
                log.error("Error deleting published message: {}", id, e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> scheduleMessagesOfDelayedAsync(DelayedMessageScheduler scheduleTask) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<CapMessage> delayedMessages = publishedMessages.values().stream()
                    .filter(message -> message.getStatus() == CapMessageStatus.DELAYED)
                    .filter(message -> message.getExpiresAt() != null && 
                                     message.getExpiresAt().isBefore(LocalDateTime.now()))
                    .collect(Collectors.toList());
                
                if (!delayedMessages.isEmpty()) {
                    scheduleTask.schedule(null, delayedMessages);
                }
            } catch (Exception e) {
                log.error("Error scheduling delayed messages", e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteExpiredMessagesAsync(CapMessageStatus status, long expiredBefore) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LocalDateTime expiredTime = LocalDateTime.ofEpochSecond(expiredBefore, 0, java.time.ZoneOffset.UTC);
                int deletedCount = 0;
                
                // 删除已发布消息中指定状态且过期的消息
                deletedCount += publishedMessages.entrySet().removeIf(entry -> {
                    CapMessage message = entry.getValue();
                    return message.getStatus() == status && 
                           message.getExpiresAt() != null && 
                           message.getExpiresAt().isBefore(expiredTime);
                }) ? 1 : 0;
                
                // 删除已接收消息中指定状态且过期的消息
                deletedCount += receivedMessages.entrySet().removeIf(entry -> {
                    CapMessage message = entry.getValue();
                    return message.getStatus() == status && 
                           message.getExpiresAt() != null && 
                           message.getExpiresAt().isBefore(expiredTime);
                }) ? 1 : 0;
                
                return deletedCount;
            } catch (Exception e) {
                log.error("Error deleting expired messages", e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateStatusAsync(Long messageId, CapMessageStatus status) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 尝试更新已发布消息
                CapMessage publishedMessage = publishedMessages.get(messageId);
                if (publishedMessage != null) {
                    publishedMessage.setStatus(status);
                    return;
                }
                
                // 尝试更新已接收消息
                CapMessage receivedMessage = receivedMessages.get(messageId);
                if (receivedMessage != null) {
                    receivedMessage.setStatus(status);
                }
            } catch (Exception e) {
                log.error("Error updating message status: {}", messageId, e);
            }
        });
    }

    private int deleteExpiredMessages(Map<Long, CapMessage> messageMap, LocalDateTime timeout, int batchCount) {
        int deletedCount = 0;
        int processedCount = 0;
        
        for (Map.Entry<Long, CapMessage> entry : messageMap.entrySet()) {
            if (processedCount >= batchCount) {
                break;
            }
            
            CapMessage message = entry.getValue();
            if (message.getAdded() != null && message.getAdded().isBefore(timeout)) {
                messageMap.remove(entry.getKey());
                deletedCount++;
            }
            processedCount++;
        }
        
        return deletedCount;
    }

    private Long generateMessageId() {
        return System.currentTimeMillis() + messageIdCounter.incrementAndGet();
    }

    private static class LockInfo {
        private final String instance;
        private LocalDateTime expiresAt;

        public LockInfo(String instance, LocalDateTime expiresAt) {
            this.instance = instance;
            this.expiresAt = expiresAt;
        }

        public String getInstance() {
            return instance;
        }

        // getter intentionally omitted to reduce unused code warnings

        public void setExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
        }

        public boolean isValid() {
            return expiresAt != null && LocalDateTime.now().isBefore(expiresAt);
        }
    }

    private Long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public CompletableFuture<Integer> batchUpdatePublishedStatusAsync(CapMessageStatus fromStatus, CapMessageStatus toStatus, int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int updatedCount = 0;
                for (CapMessage message : publishedMessages.values()) {
                    if (updatedCount >= batchSize) {
                        break;
                    }
                    if (message.getStatus() == fromStatus) {
                        message.setStatus(toStatus);
                        updatedCount++;
                    }
                }
                if (updatedCount > 0) {
                    log.debug("Batch updated {} published messages from {} to {}", updatedCount, fromStatus, toStatus);
                }
                return updatedCount;
            } catch (Exception e) {
                log.error("Error batch updating published message status from {} to {}", fromStatus, toStatus, e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> batchUpdateReceivedStatusAsync(CapMessageStatus fromStatus, CapMessageStatus toStatus, int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int updatedCount = 0;
                for (CapMessage message : receivedMessages.values()) {
                    if (updatedCount >= batchSize) {
                        break;
                    }
                    if (message.getStatus() == fromStatus) {
                        message.setStatus(toStatus);
                        updatedCount++;
                    }
                }
                if (updatedCount > 0) {
                    log.debug("Batch updated {} received messages from {} to {}", updatedCount, fromStatus, toStatus);
                }
                return updatedCount;
            } catch (Exception e) {
                log.error("Error batch updating received message status from {} to {}", fromStatus, toStatus, e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<List<CapMessage>> getExpiredDelayedMessagesAsync(int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<CapMessage> expiredMessages = publishedMessages.values().stream()
                    .filter(message -> message.getStatus() == CapMessageStatus.DELAYED)
                    .filter(message -> message.getExpiresAt() != null && message.getExpiresAt().isBefore(LocalDateTime.now()))
                    .limit(batchSize)
                    .collect(Collectors.toList());
                
                if (!expiredMessages.isEmpty()) {
                    log.debug("Found {} expired delayed messages", expiredMessages.size());
                }
                return expiredMessages;
            } catch (Exception e) {
                log.error("Error getting expired delayed messages", e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<List<CapMessage>> getPendingPublishedMessagesAsync(CapMessageStatus status, int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<CapMessage> pendingMessages = publishedMessages.values().stream()
                    .filter(message -> message.getStatus() == status)
                    .limit(batchSize)
                    .collect(Collectors.toList());
                
                if (!pendingMessages.isEmpty()) {
                    log.debug("Found {} pending published messages with status {}", pendingMessages.size(), status);
                }
                return pendingMessages;
            } catch (Exception e) {
                log.error("Error getting pending published messages with status {}", status, e);
                return List.of();
            }
        });
    }
}