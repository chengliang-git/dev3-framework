# 企业级微服务开发框架

## 概述

这是一个基于Spring Boot的企业级微服务开发框架，提供了完整的微服务开发基础设施和常用功能组件。

## 主要功能特性

### 1. 核心框架功能
- **Spring Boot 3.x** - 现代化的Spring Boot框架
- **Maven** - 项目构建和依赖管理
- **Lombok** - 代码生成和简化

### 2. 安全认证
- **JWT认证** - 基于Token的身份认证
- **Spring Security** - 安全框架集成
- **跨域配置** - 可配置的CORS支持
- **权限控制** - 基于角色的访问控制

### 3. 数据访问
- **MyBatis Plus** - 增强的MyBatis ORM框架
- **MongoDB支持** - NoSQL数据库支持
- **Oracle支持** - 企业级数据库支持
- **Redis缓存** - 分布式缓存支持

### 4. 消息队列
- **RabbitMQ** - 消息队列集成
- **CAP框架** - 分布式事务和事件总线

### 5. 监控和文档
- **Knife4j** - API文档生成（Swagger/OpenAPI）
- **Apollo配置中心** - 分布式配置管理
- **健康检查** - 应用健康状态监控

### 6. 新增功能（最新优化）

#### 6.1 异步任务处理
- **异步执行** - 支持@Async注解的异步方法执行
- **定时任务** - 支持@Scheduled注解的定时任务
- **线程池管理** - 可配置的线程池参数
- **任务监控** - 异步任务执行状态监控

#### 6.2 缓存管理
- **多级缓存** - Redis + Caffeine + 内存缓存
- **缓存策略** - 可配置的缓存策略和过期时间
- **缓存注解** - 支持@Cache注解的方法级缓存
- **缓存统计** - 缓存命中率和性能统计

#### 6.3 监控和可观测性
- **Micrometer** - 应用指标收集
- **健康检查** - 系统、数据库、Redis健康状态
- **链路追踪** - 分布式请求链路追踪
- **性能监控** - 方法执行时间监控

#### 6.4 数据验证
- **统一验证** - 基于Bean Validation的数据验证
- **自定义验证器** - 扩展的验证规则
- **验证注解** - 丰富的验证注解支持

#### 6.5 国际化支持
- **多语言** - 支持中文和英文
- **消息缓存** - 国际化消息缓存机制
- **动态切换** - 运行时语言切换

#### 6.6 API版本管理
- **版本控制** - 支持API版本管理
- **向后兼容** - 版本兼容性检查
- **版本注解** - @ApiVersion注解支持

#### 6.7 限流和熔断
- **限流控制** - 基于Bucket4j的令牌桶限流
- **熔断器** - 基于Resilience4j的熔断器
- **重试机制** - 可配置的重试策略
- **超时控制** - 方法执行超时控制

#### 6.8 文件管理（新增）
- **统一存储** - 支持本地存储和S3存储
- **S3集成** - 完整的Amazon S3兼容存储支持
- **文件处理** - 文件压缩、缩略图、水印等
- **安全验证** - 文件类型和大小验证
- **预签名URL** - S3预签名下载URL生成
- **批量操作** - 支持批量文件上传下载

#### 6.9 分布式锁（新增）
- **Redis分布式锁** - 基于Redis的分布式锁实现
- **锁管理** - 锁的获取、释放、续期管理
- **重试机制** - 锁获取失败时的重试策略
- **看门狗机制** - 防止死锁的自动释放机制

## 项目结构

```
dev3-framework/
├── business-system-example/          # 业务系统示例
├── guanwei-auth-service/             # 认证服务
├── guanwei-framework-cap/            # CAP框架模块
├── guanwei-framework-common/         # 通用模块
├── guanwei-framework-generator/      # 代码生成器
├── guanwei-framework-security/       # 安全模块
├── guanwei-framework-starter/        # 框架启动器
├── guanwei-framework-web/            # Web模块
└── guanwei-tles-case-transfer/       # 案例转移服务
```

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- Redis 6.0+
- RabbitMQ 3.8+

### 2. 配置说明

#### 2.1 S3文件存储配置
```yaml
framework:
  file-management:
    enabled: true
    storage-type: s3
    s3:
      bucket-name: "your-bucket"
      service-url: "http://your-s3-endpoint"
      access-key: "your-access-key"
      secret-key: "your-secret-key"
      region: "your-region"
      signature-version: 2
      pre-signed-url: true
      pre-signed-expiry: 24
```

#### 2.2 分布式锁配置
```yaml
framework:
  distributed-lock:
    enabled: true
    lock-prefix: "lock:"
    default-timeout: 30000
    max-retries: 3
    retry-delay: 1000
    enable-watchdog: true
```

#### 2.3 限流配置
```yaml
framework:
  rate-limit:
    enabled: true
    default-capacity: 100
    default-refill-tokens: 10
    default-refill-period: 1000
    strategy: "token-bucket"
```

#### 2.4 熔断器配置
```yaml
framework:
  circuit-breaker:
    enabled: true
    failure-rate-threshold: 50
    minimum-number-of-calls: 10
    wait-duration-in-open-state: 60000
```

### 3. 使用示例

#### 3.1 文件上传
```java
@PostMapping("/upload")
public Result<FileUploadResult> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
        FileUploadResult result = fileUploadService.uploadFile(file);
        return Result.success(result);
    } catch (Exception e) {
        return Result.error("文件上传失败: " + e.getMessage());
    }
}
```

#### 3.2 分布式锁使用
```java
@Autowired
private DistributedLockService distributedLockService;

public void executeWithLock() {
    distributedLockService.executeWithLock(
        "business-lock",
        30,
        TimeUnit.SECONDS,
        () -> {
            // 需要加锁的业务逻辑
            return "success";
        }
    );
}
```

#### 3.3 缓存使用
```java
@Cache(key = "user", prefix = "user:", expire = 3600)
public User getUserById(Long id) {
    return userMapper.selectById(id);
}
```

#### 3.4 异步任务
```java
@AsyncTask(name = "dataProcessing", executor = "ioTaskExecutor")
public void processDataAsync() {
    // 异步处理逻辑
}
```

## 配置热更新

框架支持配置热更新，通过Apollo配置中心可以动态修改配置参数，无需重启应用。

## 监控和运维

- **健康检查端点**: `/actuator/health`
- **指标端点**: `/actuator/metrics`
- **API文档**: `/doc.html`
- **文件管理**: `/api/v1/files/**`

## 最佳实践

1. **配置管理**: 使用Apollo配置中心管理配置
2. **缓存策略**: 合理使用多级缓存提升性能
3. **异步处理**: 对于耗时操作使用异步处理
4. **限流保护**: 为关键接口配置限流保护
5. **熔断器**: 为外部服务调用配置熔断器
6. **文件存储**: 生产环境建议使用S3等云存储
7. **分布式锁**: 合理使用分布式锁避免并发问题

## 扩展开发

框架采用模块化设计，可以方便地扩展新功能：

1. 在`guanwei-framework-starter`中添加新的配置类
2. 在`guanwei-framework-common`中添加通用工具类
3. 在相应的模块中添加业务逻辑

## 版本历史

- **v1.0.0** - 基础框架功能
- **v1.1.0** - 新增异步任务、缓存管理、监控功能
- **v1.2.0** - 新增文件管理、分布式锁、限流熔断功能

## 贡献指南

欢迎提交Issue和Pull Request来改进框架。

## 许可证

本项目采用MIT许可证。
