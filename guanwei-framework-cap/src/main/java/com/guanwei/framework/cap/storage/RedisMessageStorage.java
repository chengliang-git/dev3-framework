package com.guanwei.framework.cap.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapMessageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.ScanOptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Redis 消息存储实现
 * 完整实现，支持Redis的分布式锁、消息存储和过期清理
 */
@Slf4j
public class RedisMessageStorage implements MessageStorage {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Redis键前缀
    private static final String PUBLISHED_PREFIX = "cap:published:";
    private static final String RECEIVED_PREFIX = "cap:received:";
    private static final String LOCK_PREFIX = "cap:lock:";
    private static final String EXCEPTION_PREFIX = "cap:exception:";

    // Lua脚本：获取分布式锁
    private static final String ACQUIRE_LOCK_SCRIPT = 
        "if redis.call('exists', KEYS[1]) == 0 then " +
        "  redis.call('setex', KEYS[1], ARGV[1], ARGV[2]) " +
        "  return 1 " +
        "else " +
        "  return 0 " +
        "end";

    // Lua脚本：释放分布式锁
    private static final String RELEASE_LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "  return redis.call('del', KEYS[1]) " +
        "else " +
        "  return 0 " +
        "end";

    @Override
    public CompletableFuture<Boolean> acquireLockAsync(String key, Duration ttl, String instance) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String lockKey = LOCK_PREFIX + key;
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptText(ACQUIRE_LOCK_SCRIPT);
                script.setResultType(Long.class);
                
                Long result = redisTemplate.execute(script, 
                    List.of(lockKey), 
                    String.valueOf(ttl.getSeconds()), 
                    instance);
                
                boolean acquired = result != null && result == 1L;
                log.debug("Lock acquisition for key: {}, instance: {}, result: {}", key, instance, acquired);
                return acquired;
            } catch (Exception e) {
                log.error("Error acquiring lock for key: {}", key, e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Void> releaseLockAsync(String key, String instance) {
        return CompletableFuture.runAsync(() -> {
            try {
                String lockKey = LOCK_PREFIX + key;
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptText(RELEASE_LOCK_SCRIPT);
                script.setResultType(Long.class);
                
                Long result = redisTemplate.execute(script, List.of(lockKey), instance);
                log.debug("Lock release for key: {}, instance: {}, result: {}", key, instance, result);
            } catch (Exception e) {
                log.error("Error releasing lock for key: {}", key, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> renewLockAsync(String key, Duration ttl, String instance) {
        return CompletableFuture.runAsync(() -> {
            try {
                String lockKey = LOCK_PREFIX + key;
                String currentInstance = (String) redisTemplate.opsForValue().get(lockKey);
                
                if (instance.equals(currentInstance)) {
                    redisTemplate.expire(lockKey, ttl);
                    log.debug("Lock renewed for key: {}, instance: {}", key, instance);
                } else {
                    log.warn("Lock renewal failed for key: {}, instance: {}, current: {}", key, instance, currentInstance);
                }
            } catch (Exception e) {
                log.error("Error renewing lock for key: {}", key, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> changePublishStateToDelayedAsync(List<String> ids) {
        return CompletableFuture.runAsync(() -> {
            try {
                for (String id : ids) {
                    String key = PUBLISHED_PREFIX + id;
                    CapMessage message = (CapMessage) redisTemplate.opsForValue().get(key);
                    if (message != null) {
                        message.setStatus(CapMessageStatus.DELAYED);
                        redisTemplate.opsForValue().set(key, message);
                        log.debug("Changed publish state to delayed: {}", id);
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
                    String key = PUBLISHED_PREFIX + message.getId();
                    message.setStatus(status);
                    redisTemplate.opsForValue().set(key, message);
                    log.debug("Changed publish state: {} -> {}", message.getId(), status);
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
                    String key = RECEIVED_PREFIX + message.getId();
                    message.setStatus(status);
                    redisTemplate.opsForValue().set(key, message);
                    log.debug("Changed receive state: {} -> {}", message.getId(), status);
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
                String id = "msg_" + System.currentTimeMillis() + "_" + System.nanoTime();
                CapMessage message = new CapMessage(name, content);
                message.setDbId(id);
                message.setStatus(CapMessageStatus.SCHEDULED);
                message.setAdded(LocalDateTime.now());
                message.setRetries(0);
                
                String key = PUBLISHED_PREFIX + id;
                redisTemplate.opsForValue().set(key, message);
                
                log.debug("Stored published message: {}", id);
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
                String id = "exc_" + System.currentTimeMillis() + "_" + System.nanoTime();
                String key = EXCEPTION_PREFIX + id;
                
                CapMessage exceptionMessage = new CapMessage(name, group, content);
                exceptionMessage.setDbId(id);
                exceptionMessage.setStatus(CapMessageStatus.FAILED);
                exceptionMessage.setAdded(LocalDateTime.now());
                
                redisTemplate.opsForValue().set(key, exceptionMessage);
                log.debug("Stored exception message: {}", id);
            } catch (Exception e) {
                log.error("Error storing exception message", e);
            }
        });
    }

    @Override
    public CompletableFuture<CapMessage> storeReceivedMessageAsync(String name, String group, Object content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String id = "recv_" + System.currentTimeMillis() + "_" + System.nanoTime();
                CapMessage message = new CapMessage(name, group, content);
                message.setDbId(id);
                message.setStatus(CapMessageStatus.SCHEDULED);
                message.setAdded(LocalDateTime.now());
                message.setRetries(0);
                
                String key = RECEIVED_PREFIX + id;
                redisTemplate.opsForValue().set(key, message);
                
                log.debug("Stored received message: {}", id);
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
                int deletedCount = 0;
                String pattern = table.equals("published") ? PUBLISHED_PREFIX + "*" : RECEIVED_PREFIX + "*";
                
                // 使用SCAN命令遍历键
                long cursor = 0;
                do {
                    var scanOptions = ScanOptions.scanOptions().match(pattern).count(batchCount).build();
                    var scanResult = redisTemplate.scan(scanOptions);
                    cursor = scanResult.getCursorId();
                    
                    while (scanResult.hasNext()) {
                        String key = scanResult.next();
                        CapMessage message = (CapMessage) redisTemplate.opsForValue().get(key);
                        if (message != null && message.getAdded() != null && 
                            message.getAdded().isBefore(timeout)) {
                            redisTemplate.delete(key);
                            deletedCount++;
                        }
                    }
                } while (cursor != 0);
                
                log.debug("Deleted {} expired messages from {}", deletedCount, table);
                return deletedCount;
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
                List<CapMessage> messages = new java.util.ArrayList<>();
                LocalDateTime cutoff = LocalDateTime.now().minus(lookbackSeconds);
                String pattern = PUBLISHED_PREFIX + "*";
                
                long cursor = 0;
                do {
                    var scanOptions = ScanOptions.scanOptions().match(pattern).count(100).build();
                    var scanResult = redisTemplate.scan(scanOptions);
                    cursor = scanResult.getCursorId();
                    
                    while (scanResult.hasNext()) {
                        String key = scanResult.next();
                        CapMessage message = (CapMessage) redisTemplate.opsForValue().get(key);
                        if (message != null && 
                            message.getStatus() == CapMessageStatus.FAILED &&
                            message.getAdded() != null && 
                            message.getAdded().isAfter(cutoff)) {
                            messages.add(message);
                        }
                    }
                } while (cursor != 0);
                
                log.debug("Found {} published messages needing retry", messages.size());
                return messages;
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
                List<CapMessage> messages = new java.util.ArrayList<>();
                LocalDateTime cutoff = LocalDateTime.now().minus(lookbackSeconds);
                String pattern = RECEIVED_PREFIX + "*";
                
                long cursor = 0;
                do {
                    var scanOptions = ScanOptions.scanOptions().match(pattern).count(100).build();
                    var scanResult = redisTemplate.scan(scanOptions);
                    cursor = scanResult.getCursorId();
                    
                    while (scanResult.hasNext()) {
                        String key = scanResult.next();
                        CapMessage message = (CapMessage) redisTemplate.opsForValue().get(key);
                        if (message != null && 
                            message.getStatus() == CapMessageStatus.FAILED &&
                            message.getAdded() != null && 
                            message.getAdded().isAfter(cutoff)) {
                            messages.add(message);
                        }
                    }
                } while (cursor != 0);
                
                log.debug("Found {} received messages needing retry", messages.size());
                return messages;
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
                String key = RECEIVED_PREFIX + id;
                Boolean deleted = redisTemplate.delete(key);
                log.debug("Deleted received message: {}, result: {}", id, deleted);
                return deleted != null && deleted ? 1 : 0;
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
                String key = PUBLISHED_PREFIX + id;
                Boolean deleted = redisTemplate.delete(key);
                log.debug("Deleted published message: {}, result: {}", id, deleted);
                return deleted != null && deleted ? 1 : 0;
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
                List<CapMessage> delayedMessages = new java.util.ArrayList<>();
                String pattern = PUBLISHED_PREFIX + "*";
                
                long cursor = 0;
                do {
                    var scanOptions = ScanOptions.scanOptions().match(pattern).count(100).build();
                    var scanResult = redisTemplate.scan(scanOptions);
                    cursor = scanResult.getCursorId();
                    
                    while (scanResult.hasNext()) {
                        String key = scanResult.next();
                        CapMessage message = (CapMessage) redisTemplate.opsForValue().get(key);
                        if (message != null && 
                            message.getStatus() == CapMessageStatus.DELAYED &&
                            message.getExpiresAt() != null && 
                            message.getExpiresAt().isBefore(LocalDateTime.now())) {
                            delayedMessages.add(message);
                        }
                    }
                } while (cursor != 0);
                
                if (!delayedMessages.isEmpty()) {
                    scheduleTask.schedule(null, delayedMessages);
                    log.debug("Scheduled {} delayed messages", delayedMessages.size());
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
                int deletedCount = 0;
                LocalDateTime expiredTime = LocalDateTime.ofEpochSecond(expiredBefore, 0, java.time.ZoneOffset.UTC);
                
                // 删除已发布消息
                String publishedPattern = PUBLISHED_PREFIX + "*";
                long cursor = 0;
                do {
                    var scanOptions = ScanOptions.scanOptions().match(publishedPattern).count(100).build();
                    var scanResult = redisTemplate.scan(scanOptions);
                    cursor = scanResult.getCursorId();
                    
                    while (scanResult.hasNext()) {
                        String key = scanResult.next();
                        CapMessage message = (CapMessage) redisTemplate.opsForValue().get(key);
                        if (message != null && 
                            message.getStatus() == status &&
                            message.getExpiresAt() != null && 
                            message.getExpiresAt().isBefore(expiredTime)) {
                            redisTemplate.delete(key);
                            deletedCount++;
                        }
                    }
                } while (cursor != 0);
                
                // 删除已接收消息中指定状态且过期的消息
                String receivedPattern = RECEIVED_PREFIX + "*";
                cursor = 0;
                do {
                    var scanOptions = ScanOptions.scanOptions().match(receivedPattern).count(100).build();
                    var scanResult = redisTemplate.scan(scanOptions);
                    cursor = scanResult.getCursorId();
                    
                    while (scanResult.hasNext()) {
                        String key = scanResult.next();
                        CapMessage message = (CapMessage) redisTemplate.opsForValue().get(key);
                        if (message != null && 
                            message.getStatus() == status &&
                            message.getExpiresAt() != null && 
                            message.getExpiresAt().isBefore(expiredTime)) {
                            redisTemplate.delete(key);
                            deletedCount++;
                        }
                    }
                } while (cursor != 0);
                
                log.debug("Deleted {} expired messages with status: {}", deletedCount, status);
                return deletedCount;
            } catch (Exception e) {
                log.error("Error deleting expired messages from Redis", e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateStatusAsync(String messageId, CapMessageStatus status) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 尝试更新已发布消息
                String publishedKey = PUBLISHED_PREFIX + messageId;
                CapMessage publishedMessage = (CapMessage) redisTemplate.opsForValue().get(publishedKey);
                if (publishedMessage != null) {
                    publishedMessage.setStatus(status);
                    redisTemplate.opsForValue().set(publishedKey, publishedMessage);
                    log.debug("Updated published message status: {} -> {}", messageId, status);
                    return;
                }
                
                // 尝试更新已接收消息
                String receivedKey = RECEIVED_PREFIX + messageId;
                CapMessage receivedMessage = (CapMessage) redisTemplate.opsForValue().get(receivedKey);
                if (receivedMessage != null) {
                    receivedMessage.setStatus(status);
                    redisTemplate.opsForValue().set(receivedKey, receivedMessage);
                    log.debug("Updated received message status: {} -> {}", messageId, status);
                }
            } catch (Exception e) {
                log.error("Error updating message status in Redis: {}", messageId, e);
            }
        });
    }
} 