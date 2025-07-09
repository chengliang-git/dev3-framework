package com.enterprise.framework.controller;

import com.enterprise.framework.cap.CapMessage;
import com.enterprise.framework.cap.CapPublisher;
import com.enterprise.framework.cap.CapSubscriber;
import com.enterprise.framework.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * CAP 演示控制器
 * 展示如何使用 CAP 组件进行消息发布和订阅
 */
@RestController
@RequestMapping("/api/cap")
public class CapDemoController {

    private static final Logger log = LoggerFactory.getLogger(CapDemoController.class);

    @Autowired
    private CapPublisher capPublisher;

    @Autowired
    private CapSubscriber capSubscriber;

    private final Map<String, String> receivedMessages = new HashMap<>();

    public CapDemoController() {
        // 在构造函数中注册消息处理器
        // 注意：这里只是演示，实际应用中应该在 @PostConstruct 中注册
    }

    /**
     * 发布消息
     */
    @PostMapping("/publish")
    public Result<String> publishMessage(@RequestBody Map<String, Object> request) {
        try {
            String messageName = (String) request.get("name");
            Object content = request.get("content");
            String group = (String) request.getOrDefault("group", "default");

            String messageId = capPublisher.publish(messageName, content, group);

            log.info("Published message: {} -> {}", messageId, messageName);
            return Result.success(messageId);
        } catch (Exception e) {
            log.error("Failed to publish message", e);
            return Result.error("Failed to publish message: " + e.getMessage());
        }
    }

    /**
     * 发布延迟消息
     */
    @PostMapping("/publish-delay")
    public Result<String> publishDelayMessage(@RequestBody Map<String, Object> request) {
        try {
            String messageName = (String) request.get("name");
            Object content = request.get("content");
            String group = (String) request.getOrDefault("group", "default");
            Long delaySeconds = Long.valueOf(request.get("delaySeconds").toString());

            String messageId = capPublisher.publishDelay(messageName, content, group, delaySeconds);

            log.info("Published delay message: {} -> {} (delay: {}s)", messageId, messageName, delaySeconds);
            return Result.success(messageId);
        } catch (Exception e) {
            log.error("Failed to publish delay message", e);
            return Result.error("Failed to publish delay message: " + e.getMessage());
        }
    }

    /**
     * 发布事务性消息
     */
    @PostMapping("/publish-transactional")
    public Result<String> publishTransactionalMessage(@RequestBody Map<String, Object> request) {
        try {
            String messageName = (String) request.get("name");
            Object content = request.get("content");
            String group = (String) request.getOrDefault("group", "default");

            String messageId = capPublisher.publishTransactional(messageName, content, group);

            log.info("Published transactional message: {} -> {}", messageId, messageName);
            return Result.success(messageId);
        } catch (Exception e) {
            log.error("Failed to publish transactional message", e);
            return Result.error("Failed to publish transactional message: " + e.getMessage());
        }
    }

    /**
     * 订阅消息
     */
    @PostMapping("/subscribe")
    public Result<String> subscribeMessage(@RequestBody Map<String, Object> request) {
        try {
            String messageName = (String) request.get("name");
            String group = (String) request.getOrDefault("group", "default");

            // 注册消息处理器
            capSubscriber.subscribe(messageName, group, message -> {
                log.info("Received message: {} -> {}", message.getId(), message.getContent());
                receivedMessages.put(message.getId(), message.getContent());
            });

            log.info("Subscribed to message: {} (group: {})", messageName, group);
            return Result.success("Subscribed to " + messageName);
        } catch (Exception e) {
            log.error("Failed to subscribe message", e);
            return Result.error("Failed to subscribe message: " + e.getMessage());
        }
    }

    /**
     * 获取接收到的消息
     */
    @GetMapping("/messages")
    public Result<Map<String, String>> getReceivedMessages() {
        return Result.success(receivedMessages);
    }

    /**
     * 清除接收到的消息
     */
    @DeleteMapping("/messages")
    public Result<String> clearReceivedMessages() {
        receivedMessages.clear();
        return Result.success("Messages cleared");
    }

    /**
     * 取消订阅
     */
    @DeleteMapping("/subscribe")
    public Result<String> unsubscribeMessage(@RequestBody Map<String, Object> request) {
        try {
            String messageName = (String) request.get("name");
            String group = (String) request.getOrDefault("group", "default");

            capSubscriber.unsubscribe(messageName, group);

            log.info("Unsubscribed from message: {} (group: {})", messageName, group);
            return Result.success("Unsubscribed from " + messageName);
        } catch (Exception e) {
            log.error("Failed to unsubscribe message", e);
            return Result.error("Failed to unsubscribe message: " + e.getMessage());
        }
    }
}