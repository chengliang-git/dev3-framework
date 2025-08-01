package com.guanwei.framework.service;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapPublisher;
import com.guanwei.framework.cap.CapSubscriber;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * CAP 使用示例服务
 * 展示在实际业务场景中如何使用 CAP 组件
 */
@Slf4j
@Service
public class CapExampleService {

    @Autowired
    private CapPublisher capPublisher;

    @Autowired
    private CapSubscriber capSubscriber;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        // 注册消息处理器
        registerMessageHandlers();
    }

    /**
     * 注册消息处理器
     */
    private void registerMessageHandlers() {
        // 注册用户注册事件处理器
        capSubscriber.subscribe("user.registered", "user-service", message -> {
            log.info("Processing user registration event: {}", message.getContent());
            handleUserRegistration(message);
        });

        // 注册订单创建事件处理器
        capSubscriber.subscribe("order.created", "order-service", message -> {
            log.info("Processing order creation event: {}", message.getContent());
            handleOrderCreation(message);
        });

        // 注册支付完成事件处理器（带返回值）
        capSubscriber.subscribe("payment.completed", "payment-service", message -> {
            log.info("Processing payment completion event: {}", message.getContent());
            return handlePaymentCompletion(message);
        });

        // 注册库存更新事件处理器
        capSubscriber.subscribe("inventory.updated", "inventory-service", message -> {
            log.info("Processing inventory update event: {}", message.getContent());
            handleInventoryUpdate(message);
        });
    }

    /**
     * 模拟用户注册业务
     */
    public void registerUser(String username, String email) {
        log.info("Registering user: {}", username);

        // 执行用户注册业务逻辑
        // ... 数据库操作等

        // 发布用户注册事件
        Map<String, Object> eventData = Map.of(
                "username", username,
                "email", email,
                "timestamp", System.currentTimeMillis());

        String messageId = capPublisher.publish("user.registered", eventData, "user-service");
        log.info("Published user registration event: {}", messageId);
    }

    /**
     * 模拟创建订单业务
     */
    public void createOrder(String userId, String productId, int quantity) {
        log.info("Creating order for user: {}, product: {}, quantity: {}", userId, productId, quantity);

        // 执行订单创建业务逻辑
        // ... 数据库操作等

        // 发布订单创建事件
        Map<String, Object> eventData = Map.of(
                "userId", userId,
                "productId", productId,
                "quantity", quantity,
                "timestamp", System.currentTimeMillis());

        String messageId = capPublisher.publish("order.created", eventData, "order-service");
        log.info("Published order creation event: {}", messageId);
    }

    /**
     * 模拟支付业务（使用事务性消息）
     */
    public void processPayment(String orderId, double amount) {
        log.info("Processing payment for order: {}, amount: {}", orderId, amount);

        // 执行支付业务逻辑
        // ... 数据库操作等

        // 发布事务性支付完成事件
        Map<String, Object> eventData = Map.of(
                "orderId", orderId,
                "amount", amount,
                "status", "completed",
                "timestamp", System.currentTimeMillis());

        String messageId = capPublisher.publishTransactional("payment.completed", eventData, "payment-service");
        log.info("Published transactional payment completion event: {}", messageId);
    }

    /**
     * 模拟发送通知（使用延迟消息）
     */
    public void sendReminderNotification(String userId, String message, long delaySeconds) {
        log.info("Scheduling reminder notification for user: {} (delay: {}s)", userId, delaySeconds);

        Map<String, Object> eventData = Map.of(
                "userId", userId,
                "message", message,
                "type", "reminder",
                "timestamp", System.currentTimeMillis());

        String messageId = capPublisher.publishDelay("notification.sent", eventData, "notification-service",
                delaySeconds);
        log.info("Published delay notification event: {}", messageId);
    }

    /**
     * 处理用户注册事件
     */
    private void handleUserRegistration(CapMessage message) {
        try {
            // 解析消息内容
            Map<String, Object> data = parseMessageContent(message);
            String username = (String) data.get("username");
            String email = (String) data.get("email");

            // 执行后续业务逻辑
            log.info("Sending welcome email to: {}", email);
            // ... 发送欢迎邮件

            log.info("Creating user profile for: {}", username);
            // ... 创建用户档案

            log.info("User registration event processed successfully");
        } catch (Exception e) {
            log.error("Failed to process user registration event", e);
            throw new RuntimeException("Failed to process user registration", e);
        }
    }

    /**
     * 处理订单创建事件
     */
    private void handleOrderCreation(CapMessage message) {
        try {
            // 解析消息内容
            Map<String, Object> data = parseMessageContent(message);
            String userId = (String) data.get("userId");
            String productId = (String) data.get("productId");
            Integer quantity = (Integer) data.get("quantity");

            // 执行后续业务逻辑
            log.info("Updating inventory for product: {} (quantity: {})", productId, quantity);
            // ... 更新库存

            log.info("Sending order confirmation to user: {}", userId);
            // ... 发送订单确认

            log.info("Order creation event processed successfully");
        } catch (Exception e) {
            log.error("Failed to process order creation event", e);
            throw new RuntimeException("Failed to process order creation", e);
        }
    }

    /**
     * 处理支付完成事件（带返回值）
     */
    private String handlePaymentCompletion(CapMessage message) {
        try {
            // 解析消息内容
            Map<String, Object> data = parseMessageContent(message);
            String orderId = (String) data.get("orderId");
            Double amount = (Double) data.get("amount");

            // 执行后续业务逻辑
            log.info("Updating order status for: {} (amount: {})", orderId, amount);
            // ... 更新订单状态

            log.info("Sending payment receipt for order: {}", orderId);
            // ... 发送支付收据

            log.info("Payment completion event processed successfully");
            return "Payment processed successfully for order: " + orderId;
        } catch (Exception e) {
            log.error("Failed to process payment completion event", e);
            throw new RuntimeException("Failed to process payment completion", e);
        }
    }

    /**
     * 处理库存更新事件
     */
    private void handleInventoryUpdate(CapMessage message) {
        try {
            // 解析消息内容
            Map<String, Object> data = parseMessageContent(message);
            String productId = (String) data.get("productId");
            Integer quantity = (Integer) data.get("quantity");

            // 执行后续业务逻辑
            log.info("Updating product inventory: {} -> {}", productId, quantity);
            // ... 更新产品库存

            log.info("Checking low stock alerts for product: {}", productId);
            // ... 检查低库存警告

            log.info("Inventory update event processed successfully");
        } catch (Exception e) {
            log.error("Failed to process inventory update event", e);
            throw new RuntimeException("Failed to process inventory update", e);
        }
    }

    /**
     * 解析消息内容
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMessageContent(CapMessage message) {
        try {
            return objectMapper.readValue(message.getContent(), new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message content: {}", message.getContent(), e);
            throw new RuntimeException("Failed to parse message content", e);
        }
    }
}