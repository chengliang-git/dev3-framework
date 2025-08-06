# CAP 分布式事务框架使用手册

## 概述

CAP (Consistency, Availability, Partition tolerance) 是一个基于 .NET Core 的分布式事务解决方案，本项目提供了 Java 版本的实现。CAP 框架提供了分布式事务、消息队列、事件总线等功能，帮助开发者构建可靠的分布式应用。

**重要说明**: CAP 框架会自动创建和管理所有必要的交换机和队列，业务代码无需手动配置这些基础设施组件。

## 核心特性

### 1. 分布式事务

- **本地事务 + 消息队列** 的分布式事务模式
- **最终一致性** 保证
- **事务补偿** 机制
- **幂等性** 处理

### 2. 消息队列

- **自动队列创建** - 无需手动创建队列
- **消息持久化** - 支持多种存储方式
- **消息重试** - 失败自动重试机制
- **死信队列** - 处理无法消费的消息

### 3. 事件总线

- **发布订阅模式** - 松耦合的事件驱动架构
- **异步处理** - 非阻塞的事件处理
- **事件溯源** - 完整的事件历史记录

## 快速开始

### 1. 添加依赖

在 `pom.xml` 中添加 CAP 框架依赖：

```xml
<dependency>
    <groupId>com.guanwei</groupId>
    <artifactId>guanwei-framework-cap</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 基础配置

在 `application.yml` 中添加基础配置：

```yaml
cap:
  enabled: true
  default-group: "default"
  version: "v1"

  # 消息存储配置
  storage:
    type: "oracle" # 可选：memory, redis, oracle
    database:
      schema: "CAP"
      table-prefix: "CAP_"

  # 消息队列配置
  message-queue:
    type: "rabbitmq" # 可选：memory, rabbitmq
    # 注意：CAP框架会自动创建和管理队列，无需手动配置交换机
```

### 3. 发布消息

使用 `CapPublisher` 发布消息：

```java
@Service
public class OrderService {

    @Autowired
    private CapPublisher capPublisher;

    public void createOrder(Order order) {
        // 1. 保存订单到数据库
        orderRepository.save(order);

        // 2. 发布订单创建事件
        capPublisher.publish("order.created", order);
    }
}
```

### 4. 订阅消息

使用 `@CapSubscribe` 注解订阅消息：

```java
@Component
public class OrderEventHandler {

    @CapSubscribe(value = "order.created", group = "order-service")
    public void handleOrderCreated(CapMessage message) {
        Order order = objectMapper.readValue(message.getContent(), Order.class);
        // 处理订单创建事件
        log.info("收到订单创建事件: {}", order.getId());
    }
}
```

## 配置详解

### 1. 基础配置

```yaml
cap:
  enabled: true # 是否启用CAP框架
  default-group: "default" # 默认消息组
  version: "v1" # 版本号
```

### 2. 存储配置

#### Oracle 存储

```yaml
cap:
  storage:
    type: "oracle"
    database:
      # 连接配置继承自spring.datasource
      schema: "CAP" # 数据库Schema
      table-prefix: "CAP_" # 表前缀
    message-expired: 86400 # 消息过期时间（秒）
    cleanup-interval: 3600 # 清理间隔（秒）
```

#### Redis 存储

```yaml
cap:
  storage:
    type: "redis"
    redis:
      host: "localhost"
      port: 6379
      password: ""
      database: 0
      timeout: 2000
```

#### 内存存储

```yaml
cap:
  storage:
    type: "memory"
```

### 3. 消息队列配置

#### RabbitMQ 队列

```yaml
cap:
  message-queue:
    type: "rabbitmq"
    # 注意：CAP框架会自动创建和管理队列，无需手动配置交换机
    # 连接配置继承自spring.rabbitmq
```

#### 内存队列

```yaml
cap:
  message-queue:
    type: "memory"
```

### 4. 消费者配置

```yaml
cap:
  # 消费者配置
  consumer-thread-count: 10
  enable-subscriber-parallel-execute: true
  subscriber-parallel-execute-thread-count: 5
  subscriber-parallel-execute-buffer-factor: 1
```

### 5. 发布者配置

```yaml
cap:
  # 发布者配置
  enable-publish-parallel-send: true
```

### 6. 重试配置

```yaml
cap:
  # 重试配置
  failed-retry-interval: 60 # 失败重试间隔（秒）
  failed-retry-count: 3 # 最大重试次数
```

### 7. 消息过期配置

```yaml
cap:
  # 消息过期配置
  succeed-message-expired-after: 86400 # 成功消息过期时间（秒）
  failed-message-expired-after: 259200 # 失败消息过期时间（秒）
```

### 8. 清理配置

```yaml
cap:
  # 清理配置
  collector-cleaning-interval: 3600 # 清理间隔（秒）
  scheduler-batch-size: 100 # 调度批处理大小
```

## 使用指南

### 1. 消息发布

#### 基础发布

```java
@Autowired
private CapPublisher capPublisher;

// 发布简单消息
capPublisher.publish("user.created", user);

// 发布带组名的消息
capPublisher.publish("user.created", "user-service", user);
```

#### 事务发布

```java
@Transactional
public void createUser(User user) {
    // 保存用户
    userRepository.save(user);

    // 发布用户创建事件（在事务中）
    capPublisher.publish("user.created", user);
}
```

#### 延迟发布

```java
// 延迟5秒发布
capPublisher.publish("user.created", user, Duration.ofSeconds(5));
```

### 2. 消息订阅

#### 基础订阅

```java
@Component
public class UserEventHandler {

    @CapSubscribe(value = "user.created", group = "user-service")
    public void handleUserCreated(CapMessage message) {
        User user = objectMapper.readValue(message.getContent(), User.class);
        // 处理用户创建事件
    }
}
```

#### 异步订阅

```java
@CapSubscribe(value = "user.created", group = "user-service", async = true)
public void handleUserCreatedAsync(CapMessage message) {
    // 异步处理
}
```

#### 带重试的订阅

```java
@CapSubscribe(value = "user.created", group = "user-service", maxRetries = 3)
public void handleUserCreatedWithRetry(CapMessage message) {
    // 带重试的处理
}
```

### 3. 分布式事务

#### Saga 模式

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(Order order) {
        // 1. 创建订单
        orderRepository.save(order);

        // 2. 发布订单创建事件
        capPublisher.publish("order.created", order);

        // 3. 发布库存扣减事件
        capPublisher.publish("inventory.deducted", order);
    }
}

@Component
public class InventoryHandler {

    @CapSubscribe(value = "inventory.deducted", group = "inventory-service")
    public void handleInventoryDeducted(CapMessage message) {
        Order order = objectMapper.readValue(message.getContent(), Order.class);

        // 扣减库存
        inventoryService.deduct(order.getProductId(), order.getQuantity());

        // 发布库存扣减成功事件
        capPublisher.publish("inventory.deducted.success", order);
    }
}
```

### 4. 事件溯源

```java
@Component
public class OrderEventStore {

    @CapSubscribe(value = "order.*", group = "event-store")
    public void storeOrderEvent(CapMessage message) {
        // 存储事件到事件存储
        eventRepository.save(new Event(
            message.getId(),
            message.getName(),
            message.getContent(),
            message.getCreatedAt()
        ));
    }
}
```

## 队列管理

### 1. 自动队列创建

CAP 框架会自动创建和管理队列，无需手动配置：

- **队列命名规则**: `messageName + "." + groupName`
- **路由键**: `messageName`
- **自动创建**: 框架启动时自动创建必要的交换机和队列

#### 示例

```java
@CapSubscribe(value = "order.created", group = "order-service")
public void handleOrderCreated(CapMessage message) {
    // 自动创建队列：order.created.order-service
}
```

### 2. 队列管理（高级用法）

```java
@Autowired
private CapQueueManager capQueueManager;

// 检查队列是否存在
boolean exists = capQueueManager.queueExists("order.created.order-service");

// 清空队列（谨慎使用）
capQueueManager.purgeQueue("order.created.order-service");
```

## 监控和日志

### 1. 日志配置

```yaml
logging:
  level:
    com.guanwei.framework.cap: DEBUG
    org.springframework.amqp: INFO
```

### 2. 监控指标

CAP 框架提供以下监控指标：

- **消息发布数量**
- **消息消费数量**
- **消息重试次数**
- **队列长度**
- **处理延迟**

### 3. 健康检查

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,cap
  endpoint:
    health:
      show-details: always
```

## 最佳实践

### 1. 消息设计

- **消息名称**: 使用动词+名词的格式，如 `user.created`、`order.cancelled`
- **消息组**: 按业务服务划分，如 `user-service`、`order-service`
- **消息内容**: 包含必要的业务数据，避免过大的消息体

### 2. 错误处理

```java
@CapSubscribe(value = "order.created", group = "order-service")
public void handleOrderCreated(CapMessage message) {
    try {
        // 业务处理
        processOrder(message);
    } catch (Exception e) {
        log.error("处理订单创建事件失败: {}", message.getId(), e);
        // 抛出异常会触发重试
        throw new RuntimeException("处理失败", e);
    }
}
```

### 3. 幂等性处理

```java
@CapSubscribe(value = "order.created", group = "order-service")
public void handleOrderCreated(CapMessage message) {
    // 检查是否已处理
    if (isProcessed(message.getId())) {
        log.info("消息已处理，跳过: {}", message.getId());
        return;
    }

    // 处理业务逻辑
    processOrder(message);

    // 标记为已处理
    markAsProcessed(message.getId());
}
```

### 4. 性能优化

- **批量处理**: 使用批处理提高性能
- **异步处理**: 对于非关键路径使用异步处理
- **连接池**: 合理配置数据库和消息队列连接池
- **缓存**: 使用缓存减少数据库访问
- **队列管理**: 依赖 CAP 框架自动管理队列，避免手动干预

### 5. 安全考虑

- **消息加密**: 敏感数据加密传输
- **访问控制**: 限制消息队列访问权限
- **审计日志**: 记录关键操作日志

## 故障排除

### 1. 常见问题

#### 消息丢失

- 检查消息存储配置
- 验证队列绑定状态
- 查看错误日志

#### 消息重复

- 实现幂等性处理
- 检查重试配置
- 验证消息 ID 唯一性

#### 性能问题

- 调整消费者线程数
- 优化批处理大小
- 检查网络延迟

### 2. 调试技巧

```yaml
logging:
  level:
    com.guanwei.framework.cap: DEBUG
    org.springframework.amqp: DEBUG
    com.rabbitmq: DEBUG
```

### 3. 监控工具

- **RabbitMQ Management**: 监控队列状态
- **Spring Boot Actuator**: 应用健康检查
- **Micrometer**: 性能指标监控

## 版本兼容性

### 1. Spring Boot 版本

- **Spring Boot 3.x**: 完全支持
- **Spring Boot 2.x**: 需要适配

### 2. Java 版本

- **Java 17+**: 推荐使用
- **Java 11**: 支持
- **Java 8**: 需要适配

### 3. 数据库版本

- **Oracle 19c+**: 完全支持
- **Oracle 12c**: 支持
- **MySQL 8.0+**: 需要适配
- **PostgreSQL 12+**: 需要适配

## 相关文档

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [RabbitMQ 官方文档](https://www.rabbitmq.com/documentation.html)
- [Oracle 官方文档](https://docs.oracle.com/en/database/oracle/oracle-database/)
- [.NET CAP 官方文档](https://cap.dotnetcore.xyz/)
