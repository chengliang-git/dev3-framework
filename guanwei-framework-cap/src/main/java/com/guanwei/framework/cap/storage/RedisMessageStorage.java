package com.guanwei.framework.cap.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 消息存储实现
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
public class RedisMessageStorage implements MessageStorage {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MESSAGE_KEY_PREFIX = "cap:message:";
    private static final String MESSAGE_INDEX_KEY = "cap:message:index";
    private static final String MESSAGE_STATUS_KEY_PREFIX = "cap:message:status:";

    @Override
    public boolean store(CapMessage message) {
        try {
            String messageKey = MESSAGE_KEY_PREFIX + message.getId();
            String messageJson = objectMapper.writeValueAsString(message);

            // 存储消息
            redisTemplate.opsForValue().set(messageKey, messageJson);

            // 添加到消息索引
            redisTemplate.opsForZSet().add(MESSAGE_INDEX_KEY, message.getId(),
                    System.currentTimeMillis());

            // 设置过期时间
            redisTemplate.expire(messageKey, 24 * 60 * 60, TimeUnit.SECONDS); // 24小时
            redisTemplate.expire(MESSAGE_INDEX_KEY, 24 * 60 * 60, TimeUnit.SECONDS);

            log.debug("Stored message in Redis: {}", message.getId());
            return true;
        } catch (Exception e) {
            log.error("Failed to store message in Redis: {}", message.getId(), e);
            return false;
        }
    }

    @Override
    public int storeBatch(List<CapMessage> messages) {
        int successCount = 0;
        for (CapMessage message : messages) {
            if (store(message)) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public Optional<CapMessage> getById(String id) {
        CapMessage message = retrieve(id);
        return Optional.ofNullable(message);
    }

    public CapMessage retrieve(String messageId) {
        try {
            String messageKey = MESSAGE_KEY_PREFIX + messageId;
            Object messageJson = redisTemplate.opsForValue().get(messageKey);

            if (messageJson != null) {
                return objectMapper.readValue(messageJson.toString(), CapMessage.class);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to retrieve message from Redis: {}", messageId, e);
            return null;
        }
    }

    @Override
    public boolean updateStatus(String messageId, CapMessage.MessageStatus status) {
        try {
            String statusKey = MESSAGE_STATUS_KEY_PREFIX + messageId;
            redisTemplate.opsForValue().set(statusKey, status.name());
            redisTemplate.expire(statusKey, 24 * 60 * 60, TimeUnit.SECONDS);

            log.debug("Updated message status in Redis: {} -> {}", messageId, status);
            return true;
        } catch (Exception e) {
            log.error("Failed to update message status in Redis: {}", messageId, e);
            return false;
        }
    }

    @Override
    public boolean incrementRetries(String messageId) {
        try {
            String retryKey = "cap:message:retries:" + messageId;
            Long retries = redisTemplate.opsForValue().increment(retryKey);
            redisTemplate.expire(retryKey, 24 * 60 * 60, TimeUnit.SECONDS);

            log.debug("Incremented retries for message in Redis: {} -> {}", messageId, retries);
            return true;
        } catch (Exception e) {
            log.error("Failed to increment retries for message in Redis: {}", messageId, e);
            return false;
        }
    }

    @Override
    public List<CapMessage> getPendingMessages(String name, String group, int limit) {
        try {
            Set<Object> messageIds = redisTemplate.opsForZSet().range(MESSAGE_INDEX_KEY, 0, -1);

            if (messageIds != null) {
                return messageIds.stream()
                        .map(id -> retrieve(id.toString()))
                        .filter(message -> message != null &&
                                message.getStatus() == CapMessage.MessageStatus.PENDING &&
                                name.equals(message.getName()) &&
                                group.equals(message.getGroup()))
                        .limit(limit)
                        .collect(Collectors.toList());
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to get pending messages from Redis", e);
            return List.of();
        }
    }

    @Override
    public long getPendingMessageCount(String name, String group) {
        try {
            Set<Object> messageIds = redisTemplate.opsForZSet().range(MESSAGE_INDEX_KEY, 0, -1);

            if (messageIds != null) {
                return messageIds.stream()
                        .map(id -> retrieve(id.toString()))
                        .filter(message -> message != null &&
                                message.getStatus() == CapMessage.MessageStatus.PENDING &&
                                name.equals(message.getName()) &&
                                group.equals(message.getGroup()))
                        .count();
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get pending message count from Redis", e);
            return 0;
        }
    }

    @Override
    public long getFailedMessageCount(String name, String group) {
        try {
            Set<Object> messageIds = redisTemplate.opsForZSet().range(MESSAGE_INDEX_KEY, 0, -1);

            if (messageIds != null) {
                return messageIds.stream()
                        .map(id -> retrieve(id.toString()))
                        .filter(message -> message != null &&
                                message.getStatus() == CapMessage.MessageStatus.FAILED &&
                                name.equals(message.getName()) &&
                                group.equals(message.getGroup()))
                        .count();
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get failed message count from Redis", e);
            return 0;
        }
    }

    @Override
    public boolean delete(String messageId) {
        try {
            String messageKey = MESSAGE_KEY_PREFIX + messageId;
            String statusKey = MESSAGE_STATUS_KEY_PREFIX + messageId;
            String retryKey = "cap:message:retries:" + messageId;

            redisTemplate.delete(messageKey);
            redisTemplate.delete(statusKey);
            redisTemplate.delete(retryKey);
            redisTemplate.opsForZSet().remove(MESSAGE_INDEX_KEY, messageId);

            log.debug("Deleted message from Redis: {}", messageId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete message from Redis: {}", messageId, e);
            return false;
        }
    }

    @Override
    public int deleteExpiredMessages(long expiredSeconds) {
        try {
            long cutoffTime = System.currentTimeMillis() - (expiredSeconds * 1000);
            Set<Object> expiredIds = redisTemplate.opsForZSet()
                    .rangeByScore(MESSAGE_INDEX_KEY, 0, cutoffTime);

            if (expiredIds != null && !expiredIds.isEmpty()) {
                for (Object id : expiredIds) {
                    String messageId = id.toString();
                    String messageKey = MESSAGE_KEY_PREFIX + messageId;
                    String statusKey = MESSAGE_STATUS_KEY_PREFIX + messageId;
                    String retryKey = "cap:message:retries:" + messageId;

                    redisTemplate.delete(messageKey);
                    redisTemplate.delete(statusKey);
                    redisTemplate.delete(retryKey);
                }

                redisTemplate.opsForZSet().removeRangeByScore(MESSAGE_INDEX_KEY, 0, cutoffTime);

                log.info("Deleted {} expired messages from Redis", expiredIds.size());
                return expiredIds.size();
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to delete expired messages from Redis", e);
            return 0;
        }
    }

    public long getMessageCount() {
        try {
            Long count = redisTemplate.opsForZSet().size(MESSAGE_INDEX_KEY);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Failed to get message count from Redis", e);
            return 0;
        }
    }

    public void clear() {
        try {
            Set<String> keys = redisTemplate.keys("cap:message:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared all messages from Redis");
            }
        } catch (Exception e) {
            log.error("Failed to clear messages from Redis", e);
        }
    }
}