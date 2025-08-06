package com.guanwei.framework.cap.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.queue.MessageQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * CAP 默认消息发送器
 * 参考 .NET Core CAP 的 MessageSender 类
 */
@Slf4j
@Component
public class DefaultMessageSender implements MessageSender {

    private final CapProperties properties;
    private final MessageQueue messageQueue;
    private final ObjectMapper objectMapper;

    @Autowired
    public DefaultMessageSender(CapProperties properties, MessageQueue messageQueue) {
        this.properties = properties;
        this.messageQueue = messageQueue;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CompletableFuture<OperateResult> sendAsync(CapMessage message) {
        return sendAsync(message, 30000); // 默认30秒超时
    }

    @Override
    public CompletableFuture<OperateResult> sendAsync(CapMessage message, long timeout) {
        if (message == null) {
            return CompletableFuture.completedFuture(OperateResult.failed("Message is null"));
        }

        try {
            log.debug("Sending message: {} to queue", message.getName());

            // 序列化消息内容
            if (message.getContent() == null && message.getOrigin() != null) {
                try {
                    message.setContent(objectMapper.writeValueAsString(message.getOrigin()));
                } catch (Exception ex) {
                    log.warn("Failed to serialize message content, using toString(): {}", ex.getMessage());
                    message.setContent(message.getOrigin().toString());
                }
            }

            // 发送到消息队列
            boolean sent = messageQueue.send(message.getName(), message);
            
            if (sent) {
                return CompletableFuture.completedFuture(OperateResult.success());
            } else {
                log.error("Failed to send message: {}", message.getName());
                return CompletableFuture.completedFuture(OperateResult.failed("Failed to send message to queue"));
            }

        } catch (Exception ex) {
            log.error("Error sending message: {}", message.getName(), ex);
            return CompletableFuture.completedFuture(OperateResult.failed(ex));
        }
    }

    /**
     * 发送消息（带超时）
     */
    public CompletableFuture<OperateResult> sendAsyncWithTimeout(CapMessage message, long timeout) {
        return sendAsync(message, timeout)
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> {
                    log.error("Message send timeout after {}ms: {}", timeout, message.getName(), ex);
                    return OperateResult.failed("Message send timeout: " + ex.getMessage());
                });
    }
} 