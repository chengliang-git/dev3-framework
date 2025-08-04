package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CAP 订阅者处理器
 * 用于处理订阅者的消息分发
 * 参考 .NET Core CAP 组件的订阅者处理机制
 */
@Slf4j
public class CapSubscriberProcessor {

    private final CapSubscriber capSubscriber;
    private final Map<String, CapSubscribeProcessor.SubscribeHandler> handlers = new ConcurrentHashMap<>();

    public CapSubscriberProcessor(CapSubscriber capSubscriber) {
        this.capSubscriber = capSubscriber;
    }

    /**
     * 注册处理器
     */
    public void registerHandler(String messageName, String group, CapSubscribeProcessor.SubscribeHandler handler) {
        String key = buildHandlerKey(messageName, group);
        handlers.put(key, handler);

        // 注册到订阅者
        capSubscriber.subscribe(messageName, group, message -> {
            try {
                handler.handle(message);
            } catch (Exception e) {
                log.error("Error handling message: {} with handler: {}", message.getId(), handler.getMethod().getName(),
                        e);
                throw new RuntimeException("Failed to handle message", e);
            }
        });
    }

    /**
     * 获取处理器
     */
    public CapSubscribeProcessor.SubscribeHandler getHandler(String messageName, String group) {
        String key = buildHandlerKey(messageName, group);
        return handlers.get(key);
    }

    /**
     * 构建处理器键
     */
    private String buildHandlerKey(String messageName, String group) {
        return messageName + ":" + group;
    }
}