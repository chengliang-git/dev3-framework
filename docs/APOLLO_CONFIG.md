# Apollo 配置中心配置说明

## 概述

Apollo 是携程开源的分布式配置中心，本项目已集成 Apollo 配置中心来管理配置。

## 配置说明

### 1. 应用程序标识

Apollo 需要应用程序标识来区分不同的应用。我们通过以下方式配置：

#### 方式一：META-INF/app.properties 文件
```properties
app.id=enterprise-framework
app.label=Enterprise Framework
```

#### 方式二：系统属性
```bash
-Dapollo.app.id=enterprise-framework
-Dapollo.meta=http://localhost:8080
```

#### 方式三：环境变量
```bash
export APOLLO_APP_ID=enterprise-framework
export APOLLO_META=http://localhost:8080
```

#### 方式四：配置文件
```yaml
apollo:
  app-id: ${APOLLO_APP_ID:enterprise-framework}
  meta: ${APOLLO_META:http://localhost:8080}
```

### 2. 配置文件结构

#### 主配置文件 (application.yml)
```yaml
# Apollo配置中心
apollo:
  app-id: ${APOLLO_APP_ID:enterprise-framework}
  meta: ${APOLLO_META:http://localhost:8080}
  bootstrap:
    enabled: true
    eagerLoad:
      enabled: true
    namespaces: application,database,redis,rabbitmq
  # 超时配置
  timeout: 5000  # 5秒超时
  # 连接失败时继续使用本地配置
  cache-dir: ${APOLLO_CACHE_DIR:./config-cache}
```

#### 开发环境 (application-dev.yml)
```yaml
# Apollo配置中心
apollo:
  app-id: ${APOLLO_APP_ID:enterprise-framework}
  meta: ${APOLLO_META:http://localhost:8080}
```

#### 生产环境 (application-prod.yml)
```yaml
# Apollo配置中心
apollo:
  app-id: ${APOLLO_APP_ID:enterprise-framework}
  meta: ${APOLLO_META:http://prod-apollo:8080}
```

### 3. 启动脚本配置

启动脚本 `scripts/start.sh` 已包含 Apollo 配置：

```bash
# 设置Apollo配置
export APOLLO_APP_ID=${APOLLO_APP_ID:-enterprise-framework}
export APOLLO_META=${APOLLO_META:-http://localhost:8080}

# 启动应用
mvn spring-boot:run \
  -Dspring-boot.run.profiles=$SPRING_PROFILES_ACTIVE \
  -Dapollo.app.id=$APOLLO_APP_ID \
  -Dapollo.meta=$APOLLO_META \
  -Dapollo.timeout=5000 \
  -Dapollo.bootstrap.enabled=true \
  -Dapollo.bootstrap.eagerLoad.enabled=true \
  -Dapollo.cache-dir=./config-cache
```

## 配置参数说明

| 参数 | 说明 | 默认值 | 环境变量 |
|------|------|--------|----------|
| `app-id` | 应用程序ID | enterprise-framework | APOLLO_APP_ID |
| `meta` | Apollo Meta Server地址 | http://localhost:8080 | APOLLO_META |
| `bootstrap.enabled` | 是否启用Apollo | true | - |
| `bootstrap.eagerLoad.enabled` | 是否启用预加载 | true | - |
| `bootstrap.namespaces` | 配置命名空间 | application,database,redis,rabbitmq | - |
| `timeout` | 连接超时时间（毫秒） | 5000 | APOLLO_TIMEOUT |
| `cache-dir` | 本地缓存目录 | ./config-cache | APOLLO_CACHE_DIR |

## 命名空间说明

项目配置了以下命名空间：

- `application`：应用基础配置
- `database`：数据库相关配置
- `redis`：Redis相关配置
- `rabbitmq`：RabbitMQ相关配置

## 环境配置

### 开发环境
- Apollo Meta Server: http://localhost:8080
- 日志级别: warn（减少Apollo相关日志）

### 生产环境
- Apollo Meta Server: http://prod-apollo:8080
- 日志级别: warn（减少Apollo相关日志）

## 故障排除

### 1. app.id 警告
如果看到以下警告：
```
app.id is not available from System Property and /META-INF/app.properties. It is set to null
```

解决方案：
1. 确保 `META-INF/app.properties` 文件存在
2. 检查系统属性设置
3. 检查环境变量配置

### 2. 连接失败
如果 Apollo 连接失败，检查：
1. Meta Server 地址是否正确
2. 网络连接是否正常
3. Apollo 服务是否启动

**超时配置说明：**
- 默认超时时间为5秒（5000毫秒）
- 如果连接超时，Apollo会自动使用本地缓存配置
- 可以通过系统属性 `-Dapollo.timeout=5000` 调整超时时间
- 本地缓存目录默认为 `./config-cache`

### 3. 配置不生效
如果配置不生效，检查：
1. 命名空间是否正确
2. 配置是否正确发布
3. 应用是否正确订阅

## 最佳实践

### 1. 配置优先级
Apollo 配置的优先级（从高到低）：
1. 系统属性
2. 环境变量
3. 配置文件
4. META-INF/app.properties

### 2. 环境隔离
- 开发环境使用本地 Apollo 服务
- 生产环境使用专门的 Apollo 服务
- 通过环境变量区分不同环境

### 3. 配置管理
- 敏感配置（如密码）通过 Apollo 管理
- 环境相关配置通过 Apollo 管理
- 应用固定配置保留在本地配置文件

### 4. 监控和日志
- 设置适当的日志级别
- 监控 Apollo 连接状态
- 记录配置变更日志

## 示例配置

### 数据库配置
在 Apollo 中创建 `database` 命名空间：
```properties
# 数据源配置
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=framework
spring.datasource.password=framework123
spring.datasource.hikari.maximum-pool-size=20
```

### Redis配置
在 Apollo 中创建 `redis` 命名空间：
```properties
# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
```

### RabbitMQ配置
在 Apollo 中创建 `rabbitmq` 命名空间：
```properties
# RabbitMQ配置
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## 总结

通过以上配置，Apollo 配置中心已正确集成到项目中，可以：

1. **统一配置管理**：集中管理所有环境配置
2. **动态配置更新**：支持配置热更新
3. **环境隔离**：不同环境使用不同配置
4. **配置审计**：记录配置变更历史
5. **高可用**：支持配置中心高可用部署

这样可以有效解决配置管理的问题，提高系统的可维护性和可扩展性。 