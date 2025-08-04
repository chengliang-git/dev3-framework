package com.guanwei.framework.cap.queue;

import com.guanwei.framework.cap.CapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 内存消息队列实现
 * 用于开发和测试环境，生产环境建议使用 RabbitMQ 或 Kafka
 */
@Slf4j
public class MemoryMessageQueue implements MessageQueue {

    private final Map<String, BlockingQueue<CapMessage>> queues = new ConcurrentHashMap<>();
    private final Map<String, BlockingQueue<CapMessage>> delayQueues = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicLong messageIdCounter = new AtomicLong(0);

    public MemoryMessageQueue() {
        log.info("CAP Memory MessageQueue initialized");
        // 启动延迟消息处理线程
        scheduler.scheduleWithFixedDelay(this::processDelayMessages, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean send(String queueName, CapMessage message) {
        try {
            BlockingQueue<CapMessage> queue = getOrCreateQueue(queueName);
            if (message.getId() == null) {
                message.setId(generateMessageId());
            }
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(LocalDateTime.now());
            }

            boolean result = queue.offer(message);
            log.debug("Sent message to queue {}: {}", queueName, message.getId());
            return result;
        } catch (Exception e) {
            log.error("Failed to send message to queue {}: {}", queueName, message, e);
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> sendAsync(String queueName, CapMessage message) {
        return CompletableFuture.supplyAsync(() -> send(queueName, message));
    }

    @Override
    public boolean sendDelay(String queueName, CapMessage message, long delaySeconds) {
        try {
            BlockingQueue<CapMessage> delayQueue = getOrCreateDelayQueue(queueName);
            if (message.getId() == null) {
                message.setId(generateMessageId());
            }
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(LocalDateTime.now());
            }

            // 设置过期时间
            message.setExpiresAt(LocalDateTime.now().plusSeconds(delaySeconds));

            boolean result = delayQueue.offer(message);
            log.debug("Sent delay message to queue {}: {} (delay: {}s)", queueName, message.getId(), delaySeconds);
            return result;
        } catch (Exception e) {
            log.error("Failed to send delay message to queue {}: {}", queueName, message, e);
            return false;
        }
    }

    @Override
    public CapMessage receive(String queueName, long timeout) {
        try {
            BlockingQueue<CapMessage> queue = getOrCreateQueue(queueName);
            CapMessage message = queue.poll(timeout, TimeUnit.MILLISECONDS);
            if (message != null) {
                log.debug("Received message from queue {}: {}", queueName, message.getId());
            }
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while receiving message from queue: {}", queueName);
            return null;
        } catch (Exception e) {
            log.error("Failed to receive message from queue {}: {}", queueName, e.getMessage());
            return null;
        }
    }

    @Override
    public List<CapMessage> receiveBatch(String queueName, int maxCount, long timeout) {
        List<CapMessage> messages = new CopyOnWriteArrayList<>();
        long startTime = System.currentTimeMillis();

        while (messages.size() < maxCount && (System.currentTimeMillis() - startTime) < timeout) {
            CapMessage message = receive(queueName, 100); // 短超时，避免阻塞
            if (message != null) {
                messages.add(message);
            } else {
                break; // 没有更多消息
            }
        }

        log.debug("Received {} messages from queue {}", messages.size(), queueName);
        return messages;
    }

    @Override
    public boolean acknowledge(String queueName, String messageId) {
        // 内存队列中，消息一旦被接收就被确认，这里只是记录日志
        log.debug("Acknowledged message {} from queue {}", messageId, queueName);
        return true;
    }

    @Override
    public boolean reject(String queueName, String messageId, boolean requeue) {
        if (requeue) {
            // 重新入队逻辑，这里简化处理
            log.debug("Rejected and requeued message {} to queue {}", messageId, queueName);
            return true;
        } else {
            log.debug("Rejected message {} from queue {} (no requeue)", messageId, queueName);
            return true;
        }
    }

    @Override
    public long getQueueLength(String queueName) {
        BlockingQueue<CapMessage> queue = queues.get(queueName);
        return queue != null ? queue.size() : 0;
    }

    @Override
    public boolean clearQueue(String queueName) {
        BlockingQueue<CapMessage> queue = queues.get(queueName);
        if (queue != null) {
            queue.clear();
            log.debug("Cleared queue: {}", queueName);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteQueue(String queueName) {
        BlockingQueue<CapMessage> queue = queues.remove(queueName);
        BlockingQueue<CapMessage> delayQueue = delayQueues.remove(queueName);
        if (queue != null || delayQueue != null) {
            log.debug("Deleted queue: {}", queueName);
            return true;
        }
        return false;
    }

    @Override
    public boolean queueExists(String queueName) {
        return queues.containsKey(queueName);
    }

    /**
     * 获取或创建队列
     */
    private BlockingQueue<CapMessage> getOrCreateQueue(String queueName) {
        return queues.computeIfAbsent(queueName, k -> new LinkedBlockingQueue<>());
    }

    /**
     * 获取或创建延迟队列
     */
    private BlockingQueue<CapMessage> getOrCreateDelayQueue(String queueName) {
        return delayQueues.computeIfAbsent(queueName, k -> new LinkedBlockingQueue<>());
    }

    /**
     * 处理延迟消息
     */
    private void processDelayMessages() {
        LocalDateTime now = LocalDateTime.now();

        delayQueues.forEach((queueName, delayQueue) -> {
            List<CapMessage> readyMessages = new CopyOnWriteArrayList<>();

            // 收集到期的消息
            delayQueue.drainTo(readyMessages);

            readyMessages.stream()
                    .filter(message -> message.getExpiresAt() != null &&
                            message.getExpiresAt().isBefore(now))
                    .forEach(message -> {
                        // 将到期的消息发送到主队列
                        send(queueName, message);
                        log.debug("Processed delay message: {} -> {}", message.getId(), queueName);
                    });

            // 将未到期的消息重新放回延迟队列
            readyMessages.stream()
                    .filter(message -> message.getExpiresAt() != null &&
                            message.getExpiresAt().isAfter(now))
                    .forEach(delayQueue::offer);
        });
    }

    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + messageIdCounter.incrementAndGet();
    }
}