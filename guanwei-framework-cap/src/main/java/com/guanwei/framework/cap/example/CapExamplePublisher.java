package com.guanwei.framework.cap.example;

import com.guanwei.framework.cap.CapPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * CAP 示例发布者
 * 演示如何使用 CAP 组件的发布功能
 * 参考 .NET Core CAP 组件的发布者示例
 */
@Slf4j
@Component
public class CapExamplePublisher {

    @Autowired
    private CapPublisher capPublisher;

    /**
     * 发布用户注册消息
     */
    public String publishUserRegistered(UserRegisteredEvent event) {
        String messageId = capPublisher.publish("user.registered", event, "user-service");
        log.info("Published user registered message: {}", messageId);
        return messageId;
    }

    /**
     * 异步发布用户注册消息
     */
    public CompletableFuture<String> publishUserRegisteredAsync(UserRegisteredEvent event) {
        return capPublisher.publishAsync("user.registered", event, "user-service")
                .thenApply(messageId -> {
                    log.info("Published user registered message async: {}", messageId);
                    return messageId;
                });
    }

    /**
     * 发布订单创建消息（带回调）
     */
    public String publishOrderCreated(OrderCreatedEvent event) {
        String messageId = capPublisher.publish("order.created", event, "order.created.callback", "order-service");
        log.info("Published order created message: {}", messageId);
        return messageId;
    }

    /**
     * 发布消息（带消息头）
     */
    public String publishWithHeaders(NotificationEvent event) {
        Map<String, String> headers = new HashMap<>();
        headers.put("source", "notification-service");
        headers.put("priority", "high");
        headers.put("user-id", event.getUserId());

        String messageId = capPublisher.publish("notification.sent", event, headers, "notification-service");
        log.info("Published notification message with headers: {}", messageId);
        return messageId;
    }

    /**
     * 发布延迟消息
     */
    public String publishDelayMessage(EmailEvent event, long delaySeconds) {
        String messageId = capPublisher.publishDelay("email.sent", event, "email-service", delaySeconds);
        log.info("Published delay email message: {} (delay: {}s)", messageId, delaySeconds);
        return messageId;
    }

    /**
     * 异步发布延迟消息
     */
    public CompletableFuture<String> publishDelayMessageAsync(EmailEvent event, long delaySeconds) {
        return capPublisher.publishDelayAsync("email.sent", event, "email-service", delaySeconds)
                .thenApply(messageId -> {
                    log.info("Published delay email message async: {} (delay: {}s)", messageId, delaySeconds);
                    return messageId;
                });
    }

    /**
     * 发布延迟消息（带回调）
     */
    public String publishDelayMessageWithCallback(SmsEvent event, long delaySeconds) {
        String messageId = capPublisher.publishDelay("sms.sent", event, "sms.sent.callback", "sms-service",
                delaySeconds);
        log.info("Published delay SMS message with callback: {} (delay: {}s)", messageId, delaySeconds);
        return messageId;
    }

    /**
     * 发布延迟消息（带消息头）
     */
    public String publishDelayMessageWithHeaders(PaymentEvent event, long delaySeconds) {
        Map<String, String> headers = new HashMap<>();
        headers.put("payment-method", event.getPaymentMethod());
        headers.put("amount", String.valueOf(event.getAmount()));

        String messageId = capPublisher.publishDelay("payment.completed", event, headers, "payment-service",
                delaySeconds);
        log.info("Published delay payment message with headers: {} (delay: {}s)", messageId, delaySeconds);
        return messageId;
    }

    /**
     * 发布事务性消息
     */
    public String publishTransactionalMessage(TransactionEvent event) {
        String messageId = capPublisher.publishTransactional("transaction.completed", event, "transaction-service");
        log.info("Published transactional message: {}", messageId);
        return messageId;
    }

    /**
     * 异步发布事务性消息
     */
    public CompletableFuture<String> publishTransactionalMessageAsync(TransactionEvent event) {
        return capPublisher.publishTransactionalAsync("transaction.completed", event, "transaction-service")
                .thenApply(messageId -> {
                    log.info("Published transactional message async: {}", messageId);
                    return messageId;
                });
    }

    /**
     * 发布事务性消息（带回调）
     */
    public String publishTransactionalMessageWithCallback(TransactionEvent event) {
        String messageId = capPublisher.publishTransactional("transaction.completed", event,
                "transaction.completed.callback", "transaction-service");
        log.info("Published transactional message with callback: {}", messageId);
        return messageId;
    }

    /**
     * 发布事务性消息（带消息头）
     */
    public String publishTransactionalMessageWithHeaders(TransactionEvent event) {
        Map<String, String> headers = new HashMap<>();
        headers.put("transaction-type", event.getType());
        headers.put("account-id", event.getAccountId());

        String messageId = capPublisher.publishTransactional("transaction.completed", event, headers,
                "transaction-service");
        log.info("Published transactional message with headers: {}", messageId);
        return messageId;
    }

    // 事件类定义
    public static class UserRegisteredEvent {
        private String userId;
        private String username;
        private String email;

        // getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class OrderCreatedEvent {
        private String orderId;
        private String userId;
        private double amount;

        // getters and setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    public static class NotificationEvent {
        private String userId;
        private String message;
        private String type;

        // getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class EmailEvent {
        private String to;
        private String subject;
        private String content;

        // getters and setters
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class SmsEvent {
        private String phoneNumber;
        private String message;

        // getters and setters
        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class PaymentEvent {
        private String paymentId;
        private String paymentMethod;
        private double amount;

        // getters and setters
        public String getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    public static class TransactionEvent {
        private String transactionId;
        private String type;
        private String accountId;
        private double amount;

        // getters and setters
        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}