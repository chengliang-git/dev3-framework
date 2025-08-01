# Java CAP 组件使用指南

## 概述

本项目实现了一个 Java 版本的 CAP（Consistency, Availability, Partition tolerance）组件，参考了 .NET Core CAP 组件的设计理念和功能特性。该组件提供了分布式事务和消息队列的功能，实现了最终一致性和事务性消息模式。

## 核心特性

### 1. 消息发布

- **普通消息发布**：立即发送消息到队列
- **延迟消息发布**：指定延迟时间后发送消息
- **事务性消息发布**：在数据库事务中发布消息，确保事务一致性
- **异步消息发布**：支持异步发布消息

### 2. 消息订阅

- **消息订阅**：订阅指定主题的消息
- **分组订阅**：支持消息分组，实现负载均衡
- **消息处理器**：支持普通处理器和带返回值的处理器
- **动态订阅/取消订阅**：运行时动态管理订阅关系

### 3. 消息存储

- **内存存储**：适用于开发和测试环境
- **可扩展存储**：支持 Redis、数据库等存储方式
- **消息持久化**：确保消息不丢失
- **过期清理**：自动清理过期消息

### 4. 消息队列

- **内存队列**：适用于开发和测试环境
- **RabbitMQ 队列**：生产环境推荐使用，支持持久化和集群
- **可扩展队列**：支持 Kafka 等消息队列
- **延迟队列**：支持延迟消息处理
- **批量处理**：支持批量消费消息

### 5. 重试机制

- **自动重试**：消息处理失败时自动重试
- **重试策略**：支持固定间隔和指数退避
- **最大重试次数**：可配置最大重试次数
- **失败处理**：超过重试次数后标记为失败

## 快速开始

### 1. 配置依赖

在 `pom.xml` 中已经包含了必要的依赖：

```xml
<!-- CAP 组件相关依赖 -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

<!-- RabbitMQ 依赖（当使用RabbitMQ时） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 2. 配置文件

#### 内存队列配置（开发环境）

在 `application.yml` 中添加 CAP 配置：

```yaml
cap:
  enabled: true
  default-group: "default"

  storage:
    type: "memory"
    message-expired: 86400
    cleanup-interval: 3600

  message-queue:
    type: "memory"
    consumer-threads: 10
    batch-size: 100
    poll-interval: 1000

  retry:
    max-retries: 3
    retry-interval: 60
    strategy: "fixed"
```

#### RabbitMQ 配置（生产环境）

```yaml
# RabbitMQ配置
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    connection-timeout: 15000
    listener:
      simple:
        acknowledge-mode: manual
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 3000

# CAP配置
cap:
  enabled: true
  default-group: "case-transfer-group"

  storage:
    type: "memory"
    message-expired: 86400
    cleanup-interval: 3600

  message-queue:
    type: "rabbitmq"
    exchange-name: "cap.exchange"
    queue-prefix: "cap_"
    consumer-threads: 10
    batch-size: 100
    poll-interval: 1000

  retry:
    max-retries: 3
    retry-interval: 60
    strategy: "fixed"
```

### 3. 基本使用

#### 发布消息

```java
@Service
public class UserService {

    @Autowired
    private CapPublisher capPublisher;

    public void registerUser(String username, String email) {
        // 业务逻辑
        // ...

        // 发布用户注册事件
        Map<String, Object> eventData = Map.of(
            "username", username,
            "email", email,
            "timestamp", System.currentTimeMillis()
        );

        String messageId = capPublisher.publish("user.registered", eventData, "user-service");
        log.info("Published user registration event: {}", messageId);
    }
}
```

#### 订阅消息

```java
@Service
public class NotificationService {

    @Autowired
    private CapSubscriber capSubscriber;

    @PostConstruct
    public void init() {
        // 订阅用户注册事件
        capSubscriber.subscribe("user.registered", "user-service", message -> {
            log.info("Processing user registration event: {}", message.getContent());
            handleUserRegistration(message);
        });
    }

    private void handleUserRegistration(CapMessage message) {
        // 处理用户注册事件
        // 发送欢迎邮件等
    }
}
```

### 4. 高级功能

#### 延迟消息

```java
// 发布延迟消息（5秒后发送）
String messageId = capPublisher.publishDelay(
    "notification.sent",
    eventData,
    "notification-service",
    5
);
```

#### 事务性消息

```java
@Transactional
public void processPayment(String orderId, double amount) {
    // 支付业务逻辑
    // ...

    // 发布事务性消息
    String messageId = capPublisher.publishTransactional(
        "payment.completed",
        eventData,
        "payment-service"
    );
}
```

#### 带返回值的消息处理器

```java
capSubscriber.subscribe("payment.completed", "payment-service", message -> {
    // 处理支付完成事件
    String result = processPayment(message);
    return result; // 返回处理结果
});
```

## API 参考

### CapPublisher 接口

```java
public interface CapPublisher {
    // 发布消息
    String publish(String name, Object content);
    String publish(String name, Object content, String group);

    // 异步发布消息
    CompletableFuture<String> publishAsync(String name, Object content);
    CompletableFuture<String> publishAsync(String name, Object content, String group);

    // 发布延迟消息
    String publishDelay(String name, Object content, long delaySeconds);
    String publishDelay(String name, Object content, String group, long delaySeconds);

    // 发布事务性消息
    String publishTransactional(String name, Object content);
    String publishTransactional(String name, Object content, String group);
}
```

### CapSubscriber 接口

```java
public interface CapSubscriber {
    // 订阅消息
    void subscribe(String name, Consumer<CapMessage> handler);
    void subscribe(String name, String group, Consumer<CapMessage> handler);

    // 订阅消息（带返回值）
    <T> void subscribe(String name, MessageHandler<T> handler);
    <T> void subscribe(String name, String group, MessageHandler<T> handler);

    // 取消订阅
    void unsubscribe(String name);
    void unsubscribe(String name, String group);

    // 消息处理器接口
    @FunctionalInterface
    interface MessageHandler<T> {
        T handle(CapMessage message);
    }
}
```

### CapMessage 实体

```java
@Data
@Builder
public class CapMessage {
    private String id;                    // 消息ID
    private String name;                  // 消息名称/主题
    private String content;               // 消息内容
    private String group;                 // 消息组
    private MessageStatus status;         // 消息状态
    private Integer retries;              // 重试次数
    private Integer maxRetries;           // 最大重试次数
    private LocalDateTime expiresAt;      // 过期时间
    private LocalDateTime createdAt;      // 创建时间
    private LocalDateTime updatedAt;      // 更新时间
    private String headers;               // 消息头信息

    public enum MessageStatus {
        PENDING,    // 待处理
        SUCCEEDED,  // 成功
        FAILED,     // 失败
        RETRYING    // 重试中
    }
}
```

## 配置说明

### 存储配置

```yaml
cap:
  storage:
    type: "memory" # 存储类型：memory, redis, database
    table-prefix: "cap_" # 数据库表前缀
    message-expired: 86400 # 消息过期时间（秒）
    cleanup-interval: 3600 # 清理间隔（秒）
```

### 队列配置

```yaml
cap:
  message-queue:
    type: "rabbitmq" # 队列类型：memory, rabbitmq, kafka
    exchange-name: "cap.exchange" # 交换机名称（RabbitMQ专用）
    queue-prefix: "cap_" # 队列名称前缀
    consumer-threads: 10 # 消费者线程数
    batch-size: 100 # 批处理大小
    poll-interval: 1000 # 轮询间隔（毫秒）
```

### 重试配置

```yaml
cap:
  retry:
    max-retries: 3 # 最大重试次数
    retry-interval: 60 # 重试间隔（秒）
    strategy: "fixed" # 重试策略：fixed, exponential
    exponential-base: 2.0 # 指数退避基数
```

## 队列命名规则

### 参考 .NET Core CAP 的命名规则

本实现参考了 .NET Core CAP 组件的队列命名规则：

1. **队列名称**：使用 `group` 作为队列名

   - 例如：`case-transfer-group`、`user-service`、`payment-service`

2. **路由键**：使用 `exchange + "." + group` 作为路由键

   - 例如：`cap.exchange.case-transfer-group`

3. **交换机名称**：可配置，默认为 `cap.exchange`

### 命名示例

```java
// 发布消息
capPublisher.publish("user.registered", eventData, "user-service");
// 队列名：user-service
// 路由键：cap.exchange.user-service

// 订阅消息
capSubscriber.subscribe("user.registered", "user-service", handler);
// 监听队列：user-service
// 绑定路由键：cap.exchange.user-service
```

## 最佳实践

### 1. 消息命名规范

- 使用点分隔的命名方式：`domain.event`
- 例如：`user.registered`、`order.created`、`payment.completed`

### 2. 消息分组

- 使用服务名作为分组：`user-service`、`order-service`
- 实现负载均衡和故障隔离

### 3. 错误处理

- 在消息处理器中妥善处理异常
- 利用重试机制处理临时性错误
- 记录详细的错误日志

### 4. 性能优化

- 合理配置消费者线程数
- 使用批量处理提高吞吐量
- 定期清理过期消息

### 5. 监控和运维

- 监控消息处理状态
- 关注失败消息数量
- 定期检查队列长度

### 6. RabbitMQ 生产环境配置

```yaml
spring:
  rabbitmq:
    host: rabbitmq-cluster.example.com
    port: 5672
    username: cap-user
    password: secure-password
    virtual-host: /cap
    connection-timeout: 15000
    # 集群配置
    addresses: rabbitmq1:5672,rabbitmq2:5672,rabbitmq3:5672
    # 连接池配置
    cache:
      connection:
        mode: channel
        size: 25
      channel:
        size: 25
        checkout-timeout: 0
    # 监听器配置
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 1.0
          max-interval: 10000
```

## 示例项目

项目包含了完整的示例代码：

- `CapDemoController`：REST API 演示
- `CapExampleService`：业务场景示例
- `application-cap.yml`：配置文件示例
- `CaseMessageHandler`：案件转存消息处理示例

## 扩展开发

### 自定义存储实现

实现 `MessageStorage` 接口：

```java
@Component("redisMessageStorage")
public class RedisMessageStorage implements MessageStorage {
    // 实现存储方法
}
```

### 自定义队列实现

实现 `MessageQueue` 接口：

```java
@Component("rabbitMQMessageQueue")
public class RabbitMQMessageQueue implements MessageQueue {
    // 实现队列方法
}
```

## 与 .NET Core CAP 的对比

| 特性          | .NET Core CAP | Java CAP |
| ------------- | ------------- | -------- |
| 消息发布      | ✅            | ✅       |
| 消息订阅      | ✅            | ✅       |
| 延迟消息      | ✅            | ✅       |
| 事务性消息    | ✅            | ✅       |
| 重试机制      | ✅            | ✅       |
| 消息存储      | ✅            | ✅       |
| 消息队列      | ✅            | ✅       |
| 分组订阅      | ✅            | ✅       |
| 异步处理      | ✅            | ✅       |
| RabbitMQ 支持 | ✅            | ✅       |
| 队列命名规则  | ✅            | ✅       |

## 故障排除

### 常见问题

1. **RabbitMQ 连接失败**

   - 检查 RabbitMQ 服务是否启动
   - 验证连接参数（主机、端口、用户名、密码、virtual-host）
   - 确认网络连接正常

2. **队列创建失败**

   - 检查 RabbitMQ 用户权限
   - 确认 virtual-host 存在且有权限
   - 查看 RabbitMQ 日志

3. **消息发送失败**

   - 检查交换机是否存在
   - 确认队列绑定正确
   - 验证路由键配置

4. **消息消费失败**
   - 检查消费者线程池配置
   - 确认消息处理器异常处理
   - 查看应用日志

### 调试工具

使用提供的测试脚本验证配置：

```bash
# 测试CAP RabbitMQ配置
./scripts/test-cap-rabbitmq.sh
```

## RabbitMQ 配置说明

### 队列命名规则

本实现参考了 .NET Core CAP 组件的队列命名规则：

1. **队列名称**：使用 `group` 作为队列名

   - 例如：`case-transfer-group`、`user-service`、`payment-service`

2. **路由键**：使用 `exchange + "." + group` 作为路由键

   - 例如：`cap.exchange.case-transfer-group`

3. **交换机名称**：可配置，默认为 `cap.exchange`

### 生产环境配置

```yaml
spring:
  rabbitmq:
    host: rabbitmq-cluster.example.com
    port: 5672
    username: cap-user
    password: secure-password
    virtual-host: /cap
    connection-timeout: 15000

cap:
  message-queue:
    type: "rabbitmq"
    exchange-name: "cap.exchange"
```

## 总结

Java CAP 组件提供了与 .NET Core CAP 组件相似的功能，支持分布式事务和消息队列，实现了最终一致性。该组件设计灵活，易于扩展，可以满足各种业务场景的需求。

**主要改进**：

- 支持 RabbitMQ 作为消息队列
- 参考.NET Core CAP 的队列命名规则
- 完善的配置和错误处理
- 生产环境就绪的配置选项

**主要改进**：

- 支持 RabbitMQ 作为消息队列
- 参考.NET Core CAP 的队列命名规则
- 完善的配置和错误处理
- 生产环境就绪的配置选项
