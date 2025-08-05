package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.queue.MessageQueue;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CAP 消息分发器默认实现
 * 参考 .NET Core CAP 的 Dispatcher 类
 * 负责消息的分发和调度
 */
@Slf4j
@Component
public class DefaultMessageDispatcher implements MessageDispatcher {

    private final CapProperties properties;
    private final MessageStorage messageStorage;
    private final MessageQueue messageQueue;
    private final SubscribeExecutor subscribeExecutor;
    private final MessageSender messageSender;

    // 线程池
    private final ThreadPoolExecutor publishExecutor;
    private final ThreadPoolExecutor executeExecutor;
    private final ScheduledExecutorService schedulerExecutor;

    // 队列
    private final BlockingQueue<CapMessage> publishedQueue;
    private final BlockingQueue<CapMessage> receivedQueue;
    private final PriorityBlockingQueue<ScheduledMessage> scheduledQueue;

    // 控制标志
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);

    @Autowired
    public DefaultMessageDispatcher(CapProperties properties,
                                   MessageStorage messageStorage,
                                   MessageQueue messageQueue,
                                   SubscribeExecutor subscribeExecutor,
                                   MessageSender messageSender) {
        this.properties = properties;
        this.messageStorage = messageStorage;
        this.messageQueue = messageQueue;
        this.subscribeExecutor = subscribeExecutor;
        this.messageSender = messageSender;

        // 初始化线程池
        int publishThreads = properties.isEnablePublishParallelSend() ? 
            Runtime.getRuntime().availableProcessors() : 1;
        this.publishExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(publishThreads);
        
        int executeThreads = properties.isEnableSubscriberParallelExecute() ? 
            properties.getSubscriberParallelExecuteThreadCount() : 1;
        this.executeExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(executeThreads);
        
        this.schedulerExecutor = Executors.newScheduledThreadPool(1);

        // 初始化队列
        int publishQueueSize = Runtime.getRuntime().availableProcessors() * 500;
        this.publishedQueue = new LinkedBlockingQueue<>(publishQueueSize);
        
        int executeQueueSize = properties.getSubscriberParallelExecuteThreadCount() * 
            properties.getSubscriberParallelExecuteBufferFactor();
        this.receivedQueue = new LinkedBlockingQueue<>(executeQueueSize);
        
        this.scheduledQueue = new PriorityBlockingQueue<>();

        log.info("CAP message dispatcher initialized with publish threads: {}, execute threads: {}", 
                publishThreads, executeThreads);
    }

    @Override
    public CompletableFuture<Void> start() {
        if (running.compareAndSet(false, true)) {
            log.info("Starting CAP message dispatcher");
            
            // 启动发布处理线程
            for (int i = 0; i < publishExecutor.getMaximumPoolSize(); i++) {
                publishExecutor.submit(this::processPublishMessages);
            }

            // 启动执行处理线程
            for (int i = 0; i < executeExecutor.getMaximumPoolSize(); i++) {
                executeExecutor.submit(this::processExecuteMessages);
            }

            // 启动调度处理线程
            schedulerExecutor.scheduleWithFixedDelay(
                this::processScheduledMessages,
                1, 1, TimeUnit.SECONDS
            );

            log.info("CAP message dispatcher started successfully");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (stopping.compareAndSet(false, true)) {
            log.info("Stopping CAP message dispatcher");
            
            // 停止调度器
            if (schedulerExecutor != null && !schedulerExecutor.isShutdown()) {
                schedulerExecutor.shutdown();
                try {
                    if (!schedulerExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                        schedulerExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    schedulerExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // 停止执行器
            if (executeExecutor != null && !executeExecutor.isShutdown()) {
                executeExecutor.shutdown();
                try {
                    if (!executeExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executeExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executeExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // 停止发布器
            if (publishExecutor != null && !publishExecutor.isShutdown()) {
                publishExecutor.shutdown();
                try {
                    if (!publishExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                        publishExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    publishExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            running.set(false);
            stopping.set(false);
            log.info("CAP message dispatcher stopped");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> enqueueToPublish(CapMessage message) {
        if (stopping.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Dispatcher is stopping"));
        }

        try {
            publishedQueue.put(message);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> enqueueToExecute(CapMessage message) {
        if (stopping.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Dispatcher is stopping"));
        }

        try {
            receivedQueue.put(message);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> enqueueToScheduler(CapMessage message, LocalDateTime publishTime, Object transaction) {
        if (stopping.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Dispatcher is stopping"));
        }
        ScheduledMessage scheduledMessage = new ScheduledMessage(message, publishTime);
        scheduledQueue.offer(scheduledMessage);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 处理发布消息
     */
    private void processPublishMessages() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                CapMessage message = publishedQueue.poll(1, TimeUnit.SECONDS);
                if (message != null) {
                    messageSender.sendAsync(message)
                        .thenAccept(result -> {
                            if (!result.isSucceeded()) {
                                log.error("Failed to send message: {}, error: {}", 
                                        message.getId(), result.getError());
                            }
                        })
                        .exceptionally(ex -> {
                            log.error("Error sending message: {}", message.getId(), ex);
                            return null;
                        });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing publish message", e);
            }
        }
    }

    /**
     * 处理执行消息
     */
    private void processExecuteMessages() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                CapMessage message = receivedQueue.poll(1, TimeUnit.SECONDS);
                if (message != null) {
                    subscribeExecutor.executeAsync(message)
                        .exceptionally(ex -> {
                            log.error("Error executing message: {}", message.getId(), ex);
                            return null;
                        });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing execute message", e);
            }
        }
    }

    /**
     * 处理调度消息
     */
    private void processScheduledMessages() {
        try {
            LocalDateTime now = LocalDateTime.now();
            java.util.List<ScheduledMessage> readyMessages = new ArrayList<>();
            
            // 安全地从队列中获取消息
            ScheduledMessage message;
            while ((message = scheduledQueue.poll()) != null) {
                if (message.publishTime.isBefore(now) || message.publishTime.isEqual(now)) {
                    readyMessages.add(message);
                } else {
                    // 如果消息还没到时间，重新放回队列
                    scheduledQueue.offer(message);
                    break; // 由于是优先级队列，后面的消息时间更晚，可以退出
                }
            }

            for (ScheduledMessage scheduledMessage : readyMessages) {
                enqueueToPublish(scheduledMessage.message)
                    .exceptionally(ex -> {
                        log.error("Error enqueueing scheduled message: {}", 
                                scheduledMessage.message.getId(), ex);
                        return null;
                    });
            }
        } catch (Exception e) {
            log.error("Error processing scheduled messages", e);
        }
    }

    /**
     * 调度消息包装类
     */
    private static class ScheduledMessage implements Comparable<ScheduledMessage> {
        final CapMessage message;
        final LocalDateTime publishTime;

        ScheduledMessage(CapMessage message, LocalDateTime publishTime) {
            this.message = message;
            this.publishTime = publishTime;
        }

        @Override
        public int compareTo(ScheduledMessage other) {
            return this.publishTime.compareTo(other.publishTime);
        }
    }
} 