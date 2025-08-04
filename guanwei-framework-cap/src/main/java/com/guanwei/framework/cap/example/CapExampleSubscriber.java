package com.guanwei.framework.cap.example;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapSubscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CAP 示例订阅者
 * 演示如何使用 CAP 组件的订阅功能
 * 参考 .NET Core CAP 组件的订阅者示例
 */
@Slf4j
@Component
public class CapExampleSubscriber implements CapSubscribe {

    /**
     * 订阅用户注册消息
     */
    @com.guanwei.framework.cap.annotation.CapSubscribe(value = "user.registered", group = "user-service")
    public void handleUserRegistered(CapMessage message) {
        log.info("Received user registered message: {}", message.getId());
        // 处理用户注册逻辑
    }

    /**
     * 订阅订单创建消息
     */
    @com.guanwei.framework.cap.annotation.CapSubscribe(value = "order.created", group = "order-service", maxRetries = 5)
    public void handleOrderCreated(CapMessage message) {
        log.info("Received order created message: {}", message.getId());
        // 处理订单创建逻辑
    }

    /**
     * 订阅支付完成消息（带延迟）
     */
    @com.guanwei.framework.cap.annotation.CapSubscribe(value = "payment.completed", group = "payment-service", messageType = com.guanwei.framework.cap.annotation.CapSubscribe.MessageType.DELAY)
    public void handlePaymentCompleted(CapMessage message) {
        log.info("Received payment completed message: {}", message.getId());
        // 处理支付完成逻辑
    }

    /**
     * 订阅事务性消息
     */
    @com.guanwei.framework.cap.annotation.CapSubscribe(value = "transaction.completed", group = "transaction-service", messageType = com.guanwei.framework.cap.annotation.CapSubscribe.MessageType.TRANSACTIONAL, async = true)
    public void handleTransactionCompleted(CapMessage message) {
        log.info("Received transaction completed message: {}", message.getId());
        // 处理事务完成逻辑
    }

    /**
     * 订阅消息（带内容参数）
     */
    @com.guanwei.framework.cap.annotation.CapSubscribe(value = "notification.sent", group = "notification-service")
    public void handleNotificationSent(String content, CapMessage message) {
        log.info("Received notification sent message: {} with content: {}", message.getId(), content);
        // 处理通知发送逻辑
    }

    /**
     * 订阅消息（带自定义重试配置）
     */
    @com.guanwei.framework.cap.annotation.CapSubscribe(value = "email.sent", group = "email-service", maxRetries = 3, retryInterval = 30)
    public void handleEmailSent(CapMessage message) {
        log.info("Received email sent message: {}", message.getId());
        // 处理邮件发送逻辑
    }

    /**
     * 订阅消息（带处理器名称）
     */
    @com.guanwei.framework.cap.annotation.CapSubscribe(value = "sms.sent", group = "sms-service", handlerName = "smsHandler")
    public void handleSmsSent(CapMessage message) {
        log.info("Received SMS sent message: {}", message.getId());
        // 处理短信发送逻辑
    }
}