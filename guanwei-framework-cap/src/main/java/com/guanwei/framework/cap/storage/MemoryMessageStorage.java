package com.guanwei.framework.cap.storage;

import com.guanwei.framework.cap.CapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 内存消息存储实现
 * 用于开发和测试环境，生产环境建议使用数据库存储
 */
@Slf4j
public class MemoryMessageStorage implements MessageStorage {

    private final Map<String, CapMessage> messageStore = new ConcurrentHashMap<>();
    private final AtomicLong messageIdCounter = new AtomicLong(0);

    @Override
    public boolean store(CapMessage message) {
        try {
            if (message.getId() == null) {
                message.setId(generateMessageId());
            }
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(LocalDateTime.now());
            }
            message.setUpdatedAt(LocalDateTime.now());

            messageStore.put(message.getId(), message);
            log.debug("Stored message: {}", message.getId());
            return true;
        } catch (Exception e) {
            log.error("Failed to store message: {}", message, e);
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
        log.debug("Batch stored {} messages, success: {}", messages.size(), successCount);
        return successCount;
    }

    @Override
    public Optional<CapMessage> getById(String id) {
        return Optional.ofNullable(messageStore.get(id));
    }

    @Override
    public List<CapMessage> getPendingMessages(String name, String group, int limit) {
        return messageStore.values().stream()
                .filter(message -> name.equals(message.getName()) &&
                        group.equals(message.getGroup()) &&
                        message.getStatus() == CapMessage.MessageStatus.PENDING)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(String id, CapMessage.MessageStatus status) {
        CapMessage message = messageStore.get(id);
        if (message != null) {
            message.setStatus(status);
            message.setUpdatedAt(LocalDateTime.now());
            log.debug("Updated message status: {} -> {}", id, status);
            return true;
        }
        return false;
    }

    @Override
    public boolean incrementRetries(String id) {
        CapMessage message = messageStore.get(id);
        if (message != null) {
            message.setRetries(message.getRetries() == null ? 1 : message.getRetries() + 1);
            message.setUpdatedAt(LocalDateTime.now());
            log.debug("Incremented retries for message: {} -> {}", id, message.getRetries());
            return true;
        }
        return false;
    }

    @Override
    public int deleteExpiredMessages(long expiredTime) {
        LocalDateTime expiredDateTime = LocalDateTime.now().minusSeconds(expiredTime);
        List<String> expiredIds = messageStore.values().stream()
                .filter(message -> message.getCreatedAt() != null &&
                        message.getCreatedAt().isBefore(expiredDateTime))
                .map(CapMessage::getId)
                .collect(Collectors.toList());

        expiredIds.forEach(messageStore::remove);
        log.debug("Deleted {} expired messages", expiredIds.size());
        return expiredIds.size();
    }

    @Override
    public boolean delete(String id) {
        CapMessage removed = messageStore.remove(id);
        if (removed != null) {
            log.debug("Deleted message: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long getFailedMessageCount(String name, String group) {
        return messageStore.values().stream()
                .filter(message -> name.equals(message.getName()) &&
                        group.equals(message.getGroup()) &&
                        message.getStatus() == CapMessage.MessageStatus.FAILED)
                .count();
    }

    @Override
    public long getPendingMessageCount(String name, String group) {
        return messageStore.values().stream()
                .filter(message -> name.equals(message.getName()) &&
                        group.equals(message.getGroup()) &&
                        message.getStatus() == CapMessage.MessageStatus.PENDING)
                .count();
    }

    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + messageIdCounter.incrementAndGet();
    }
}