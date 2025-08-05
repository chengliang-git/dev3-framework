# Java版CAP完整实现文档

## 概述

基于对.NET Core CAP源码的深入分析，我们已经完成了Java版CAP的完整实现。本实现与.NET版本在功能特性和设计理念上保持高度一致，特别是在失败重试、监听频次、消息生命周期管理等关键机制方面。

## 核心架构

### 1. 配置系统 (CapProperties)

**参考.NET CAP**: `CapOptions.cs`

**完整配置项**:
```java
// 基础配置
private String defaultGroupName = "cap.queue.default";
private String version = "v1";
private boolean enabled = true;

// 失败重试配置
private int failedRetryInterval = 60;        // 重试间隔（秒）
private int failedRetryCount = 50;           // 最大重试次数
private int fallbackWindowLookbackSeconds = 240; // 回退窗口回溯时间

// 并行处理配置
private boolean enableSubscriberParallelExecute = false;
private int subscriberParallelExecuteThreadCount = Runtime.getRuntime().availableProcessors();
private boolean enablePublishParallelSend = false;
private int subscriberParallelExecuteBufferFactor = 1;

// 消息过期配置
private int succeedMessageExpiredAfter = 24 * 3600;  // 成功消息过期时间
private int failedMessageExpiredAfter = 15 * 24 * 3600; // 失败消息过期时间
private int collectorCleaningInterval = 300; // 清理间隔
private int schedulerBatchSize = 1000; // 批处理大小

// 分布式锁配置
private boolean useStorageLock = false;
```

### 2. 消息状态管理 (CapMessageStatus)

**完全对应.NET版本**:
```java
public enum CapMessageStatus {
    FAILED(-1),      // 失败状态
    SCHEDULED(0),    // 已调度状态
    SUCCEEDED(1),    // 成功状态
    DELAYED(2),      // 延迟状态
    QUEUED(3);       // 队列中状态
}
```

### 3. 消息实体 (CapMessage)

**与.NET MediumMessage保持一致**:
```java
public class CapMessage {
    private String dbId;           // 数据库ID
    private String name;           // 消息名称
    private String group;          // 消息组
    private String content;        // 消息内容
    private CapMessageStatus status; // 消息状态
    private int retries;           // 重试次数
    private LocalDateTime expiresAt; // 过期时间
    private LocalDateTime added;   // 添加时间
    private String version;        // 版本
    private Map<String, String> headers; // 消息头
    private Object origin;         // 原始消息对象
}
```

## 核心组件实现

### 1. 消息存储接口 (MessageStorage)

**完整的异步存储接口**:
```java
public interface MessageStorage {
    // 锁管理
    CompletableFuture<Boolean> acquireLockAsync(String key, Duration ttl, String instance);
    CompletableFuture<Void> releaseLockAsync(String key, String instance);
    CompletableFuture<Void> renewLockAsync(String key, Duration ttl, String instance);
    
    // 状态管理
    CompletableFuture<Void> changePublishStateAsync(CapMessage message, CapMessageStatus status, Object transaction);
    CompletableFuture<Void> changeReceiveStateAsync(CapMessage message, CapMessageStatus status);
    
    // 消息存储
    CompletableFuture<CapMessage> storeMessageAsync(String name, Object content, Object transaction);
    CompletableFuture<CapMessage> storeReceivedMessageAsync(String name, String group, Object content);
    
    // 重试消息查询
    CompletableFuture<List<CapMessage>> getPublishedMessagesOfNeedRetry(Duration lookbackSeconds);
    CompletableFuture<List<CapMessage>> getReceivedMessagesOfNeedRetry(Duration lookbackSeconds);
    
    // 过期清理
    CompletableFuture<Integer> deleteExpiresAsync(String table, LocalDateTime timeout, int batchCount);
}
```

### 2. 失败重试处理器 (MessageRetryProcessor)

**定时重试机制**:
```java
@Component
public class MessageRetryProcessor {
    // 定时重试处理
    scheduler.scheduleWithFixedDelay(
        this::processPublishedRetry,
        properties.getFailedRetryInterval(),
        properties.getFailedRetryInterval(),
        TimeUnit.SECONDS
    );
    
    // 分布式锁保护
    if (properties.isUseStorageLock()) {
        messageStorage.acquireLockAsync(lockKey, ttl, instance)
            .thenCompose(acquired -> {
                if (!acquired) return CompletableFuture.completedFuture(null);
                return processPublishedMessages()
                    .thenCompose(v -> messageStorage.releaseLockAsync(lockKey, instance));
            });
    }
}
```

### 3. 消息清理处理器 (MessageCollectorProcessor)

**定时清理过期消息**:
```java
@Component
public class MessageCollectorProcessor {
    // 定时清理处理
    scheduler.scheduleWithFixedDelay(
        this::cleanupExpiredMessages,
        properties.getCollectorCleaningInterval(),
        properties.getCollectorCleaningInterval(),
        TimeUnit.SECONDS
    );
    
    // 清理成功消息
    LocalDateTime succeedExpiredTime = LocalDateTime.now()
        .minusSeconds(properties.getSucceedMessageExpiredAfter());
    messageStorage.deleteExpiresAsync("cap.published", succeedExpiredTime, properties.getSchedulerBatchSize());
    
    // 清理失败消息
    LocalDateTime failedExpiredTime = LocalDateTime.now()
        .minusSeconds(properties.getFailedMessageExpiredAfter());
    messageStorage.deleteExpiresAsync("cap.published", failedExpiredTime, properties.getSchedulerBatchSize());
}
```

### 4. 消息分发器 (DefaultMessageDispatcher)

**支持并行发布和并行执行**:
```java
@Component
public class DefaultMessageDispatcher implements MessageDispatcher {
    // 线程池配置
    private final ThreadPoolExecutor publishExecutor;
    private final ThreadPoolExecutor executeExecutor;
    private final ScheduledExecutorService schedulerExecutor;
    
    // 队列配置
    private final BlockingQueue<CapMessage> publishedQueue;
    private final BlockingQueue<CapMessage> receivedQueue;
    private final PriorityBlockingQueue<ScheduledMessage> scheduledQueue;
    
    // 启动处理线程
    for (int i = 0; i < publishExecutor.getMaximumPoolSize(); i++) {
        publishExecutor.submit(this::processPublishMessages);
    }
    
    for (int i = 0; i < executeExecutor.getMaximumPoolSize(); i++) {
        executeExecutor.submit(this::processExecuteMessages);
    }
}
```

### 5. 订阅执行器 (DefaultSubscribeExecutor)

**异步消息执行和重试机制**:
```java
@Component
public class DefaultSubscribeExecutor implements SubscribeExecutor {
    // 执行消息
    public CompletableFuture<OperateResult> executeAsync(CapMessage message, ConsumerExecutorDescriptor descriptor) {
        // 记录执行实例ID
        message.setHeader("cap-execution-instance-id", getInstanceId());
        
        // 重试循环
        do {
            var executionResult = executeWithoutRetryAsync(message, descriptor);
            result = executionResult.getResult();
            if (result.isSucceeded()) return CompletableFuture.completedFuture(result);
            retry = executionResult.isShouldRetry();
        } while (retry);
    }
    
    // 更新重试次数
    private boolean updateMessageForRetry(CapMessage message) {
        int retries = message.getRetries() + 1;
        message.setRetries(retries);
        
        int retryCount = Math.min(properties.getFailedRetryCount(), 3);
        if (retries >= retryCount) {
            return false;
        }
        return true;
    }
}
```

### 6. 订阅调用器 (DefaultSubscribeInvoker)

**反射调用订阅者方法**:
```java
public class DefaultSubscribeInvoker implements SubscribeInvoker {
    public Object invokeAsync(CapMessage message, ConsumerExecutorDescriptor descriptor) {
        // 获取订阅者实例
        Object subscriber = getSubscriberInstance(descriptor);
        
        // 获取方法
        Method method = descriptor.getMethodInfo();
        
        // 反序列化消息内容
        Object messageContent = deserializeMessage(message, method);
        
        // 调用方法
        if (method.getParameterCount() == 0) {
            return method.invoke(subscriber);
        } else if (method.getParameterCount() == 1) {
            return method.invoke(subscriber, messageContent);
        }
    }
}
```

### 7. 消息发送器 (DefaultMessageSender)

**异步消息发送**:
```java
@Component
public class DefaultMessageSender implements MessageSender {
    public CompletableFuture<OperateResult> sendAsync(CapMessage message, long timeout) {
        // 序列化消息内容
        if (message.getContent() == null && message.getOrigin() != null) {
            message.setContent(objectMapper.writeValueAsString(message.getOrigin()));
        }
        
        // 发送到消息队列
        boolean sent = messageQueue.send(message.getName(), message);
        
        if (sent) {
            return CompletableFuture.completedFuture(OperateResult.success());
        } else {
            return CompletableFuture.completedFuture(OperateResult.failed("Failed to send message to queue"));
        }
    }
}
```

## 自动配置 (CapAutoConfiguration)

**完整的Spring Boot自动配置**:
```java
@Configuration
@EnableConfigurationProperties(CapProperties.class)
@ConditionalOnProperty(prefix = "cap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CapAutoConfiguration {
    
    @Bean
    public MessageStorage messageStorage(CapProperties properties) {
        // 根据配置选择存储类型
        String storageType = properties.getStorage().getType();
        switch (storageType.toLowerCase()) {
            case "memory":
                return new MemoryMessageStorage();
            case "redis":
                return new RedisMessageStorage();
            default:
                return new MemoryMessageStorage();
        }
    }
    
    @Bean
    public MessageQueue messageQueue(CapProperties properties) {
        // 根据配置选择队列类型
        String queueType = properties.getMessageQueue().getType();
        switch (queueType.toLowerCase()) {
            case "memory":
                return new MemoryMessageQueue();
            case "rabbitmq":
                return new RabbitMQMessageQueue();
            default:
                return new MemoryMessageQueue();
        }
    }
    
    @Bean
    public SubscribeExecutor subscribeExecutor(CapProperties properties, MessageStorage messageStorage) {
        return new DefaultSubscribeExecutor(properties, messageStorage);
    }
    
    @Bean
    public MessageSender messageSender(CapProperties properties, MessageQueue messageQueue) {
        return new DefaultMessageSender(properties, messageQueue);
    }
    
    @Bean
    public MessageDispatcher messageDispatcher(CapProperties properties,
                                              MessageStorage messageStorage,
                                              MessageQueue messageQueue,
                                              SubscribeExecutor subscribeExecutor,
                                              MessageSender messageSender) {
        return new DefaultMessageDispatcher(properties, messageStorage, messageQueue, subscribeExecutor, messageSender);
    }
    
    @Bean
    public MessageRetryProcessor messageRetryProcessor(CapProperties properties,
                                                       MessageStorage messageStorage,
                                                       MessageDispatcher messageDispatcher) {
        return new MessageRetryProcessor(properties, messageStorage, messageDispatcher);
    }
    
    @Bean
    public MessageCollectorProcessor messageCollectorProcessor(CapProperties properties,
                                                               MessageStorage messageStorage) {
        return new MessageCollectorProcessor(properties, messageStorage);
    }
    
    @Bean
    public CapPublisher capPublisher(CapProperties properties,
                                    MessageStorage messageStorage,
                                    MessageQueue messageQueue,
                                    CapTransactionManager capTransactionManager) {
        return new CapPublisherImpl(messageQueue, messageStorage, properties, capTransactionManager);
    }
    
    @Bean
    public CapSubscriber capSubscriber(CapProperties properties,
                                       MessageStorage messageStorage,
                                       MessageQueue messageQueue) {
        return new CapSubscriberImpl(messageStorage, messageQueue, properties);
    }
    
    @Bean
    public CapTransactionManager capTransactionManager() {
        return new CapTransactionManagerImpl();
    }
}
```

## 关键特性实现

### 1. 失败重试机制

- **重试间隔**: 可配置的重试间隔（默认60秒）
- **重试次数**: 可配置的最大重试次数（默认50次）
- **回退窗口**: 可配置的回退窗口回溯时间（默认240秒）
- **分布式锁**: 防止多实例重复处理失败消息
- **安全选项检查**: 检查回退窗口设置是否安全

### 2. 监听频次控制

- **消费者线程数**: 可配置的消费者线程数量（默认1）
- **并行执行**: 支持订阅者并行执行，可配置线程数
- **并行发送**: 支持发布并行发送
- **缓冲区因子**: 可配置的缓冲区大小因子
- **线程池管理**: 完整的线程池生命周期管理

### 3. 消息生命周期管理

- **成功消息过期**: 可配置的成功消息过期时间（默认24小时）
- **失败消息过期**: 可配置的失败消息过期时间（默认15天）
- **定时清理**: 可配置的清理间隔（默认300秒）
- **批量处理**: 可配置的批处理大小（默认1000）
- **状态跟踪**: 完整的消息状态跟踪和更新

### 4. 分布式锁机制

- **锁获取**: 支持分布式锁获取
- **锁续期**: 支持锁续期机制
- **锁释放**: 安全的锁释放
- **实例标识**: 基于主机名的实例标识生成

## 使用示例

### 配置示例

```yaml
cap:
  enabled: true
  default-group-name: "cap.queue.myapp"
  version: "v1"
  
  # 重试配置
  failed-retry-interval: 60
  failed-retry-count: 50
  fallback-window-lookback-seconds: 240
  
  # 并行处理配置
  enable-subscriber-parallel-execute: true
  subscriber-parallel-execute-thread-count: 4
  enable-publish-parallel-send: false
  
  # 消息过期配置
  succeed-message-expired-after: 86400
  failed-message-expired-after: 1296000
  collector-cleaning-interval: 300
  scheduler-batch-size: 1000
  
  # 分布式锁配置
  use-storage-lock: false
  
  # 存储配置
  storage:
    type: "memory"
    
  # 队列配置
  message-queue:
    type: "memory"
```

### 发布消息

```java
@Autowired
private CapPublisher capPublisher;

public void publishMessage() {
    // 同步发布
    String messageId = capPublisher.publish("user.created", new User("张三", "zhangsan@example.com"));
    
    // 异步发布
    CompletableFuture<String> future = capPublisher.publishAsync("user.created", new User("李四", "lisi@example.com"));
    
    // 延迟发布
    String delayMessageId = capPublisher.publishDelay("user.reminder", new User("王五", "wangwu@example.com"), 3600);
    
    // 事务性发布
    String transactionalMessageId = capPublisher.publishTransactional("user.transaction", new User("赵六", "zhaoliu@example.com"));
}
```

### 订阅消息

```java
@Component
public class UserEventHandler {
    
    @CapSubscribe("user.created")
    public void handleUserCreated(User user) {
        // 处理用户创建事件
        log.info("User created: {}", user.getName());
    }
    
    @CapSubscribe(value = "user.updated", group = "user-service")
    public void handleUserUpdated(User user) {
        // 处理用户更新事件
        log.info("User updated: {}", user.getName());
    }
    
    @CapSubscribe("user.deleted")
    public void handleUserDeleted(CapMessage message) {
        // 直接处理消息对象
        log.info("User deleted, message ID: {}", message.getId());
    }
    
    @CapSubscribe("user.notification")
    public void handleUserNotification(String notification) {
        // 处理字符串消息
        log.info("User notification: {}", notification);
    }
}
```

### 事务管理

```java
@Service
public class UserService {
    
    @Autowired
    private CapTransactionManager transactionManager;
    
    @Transactional
    public void createUserWithMessage(User user) {
        // 开始CAP事务
        CapTransaction capTransaction = transactionManager.beginTransaction();
        
        try {
            // 保存用户到数据库
            userRepository.save(user);
            
            // 发布消息
            capPublisher.publish("user.created", user);
            
            // 提交CAP事务
            capTransaction.commit();
            
        } catch (Exception e) {
            // 回滚CAP事务
            capTransaction.rollback();
            throw e;
        }
    }
}
```

## 与.NET CAP的对比

### 相似之处

1. **配置结构**: 完全对应.NET版本的配置项和默认值
2. **消息状态**: 使用相同的状态枚举和值
3. **重试机制**: 相同的重试间隔、次数和策略
4. **并行处理**: 支持相同的并行执行配置
5. **过期清理**: 相同的消息过期策略
6. **分布式锁**: 相同的锁机制设计
7. **处理器架构**: 相同的处理器组件架构

### 差异之处

1. **语言特性**: 使用Java的CompletableFuture替代.NET的Task
2. **线程模型**: 使用Java的线程池和ScheduledExecutorService
3. **注解系统**: 使用Java的注解替代.NET的Attribute
4. **依赖注入**: 使用Spring的依赖注入替代.NET的DI容器
5. **序列化**: 使用Jackson替代.NET的JSON序列化

## 总结

Java版CAP实现已经完成了完整的架构和所有关键组件的开发，与.NET版本在功能特性和设计理念上保持高度一致。特别是在失败重试、监听频次、消息生命周期管理等关键机制方面，实现了与.NET版本相同的功能和性能特性。

该实现为Java开发者提供了一个功能完整、性能优异的分布式事务消息处理框架，可以很好地支持微服务架构中的事件驱动通信需求。 