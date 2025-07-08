# 配置变更说明

## 概述

本项目已进行以下重要配置变更：

1. **数据库连接池**：从 Druid 替换为 HikariCP
2. **JSON 处理库**：从 FastJSON2 替换为 Jackson

## 变更详情

### 1. 数据库连接池变更

#### 变更前（Druid）
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-3-starter</artifactId>
    <version>1.2.20</version>
</dependency>
```

```yaml
# application.yml
spring:
  datasource:
    druid:
      url: jdbc:oracle:thin:@localhost:1521:XE
      username: framework
      password: framework123
      web-stat-filter:
        enabled: true
      stat-view-servlet:
        enabled: true
        login-username: admin
        login-password: admin123
```

#### 变更后（HikariCP）
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: framework
    password: framework123
    driver-class-name: oracle.jdbc.OracleDriver
    
    hikari:
      pool-name: HikariCP
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1 FROM DUAL
```

### 2. JSON 处理库变更

#### 变更前（FastJSON2）
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.43</version>
</dependency>
```

```java
// 代码中使用
import com.alibaba.fastjson2.JSON;

// 序列化
String json = JSON.toJSONString(object);

// 反序列化
Map<String, Object> map = JSON.parseObject(json, Map.class);
```

#### 变更后（Jackson）
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

```java
// 代码中使用
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

private final ObjectMapper objectMapper = new ObjectMapper();

// 序列化
String json = objectMapper.writeValueAsString(object);

// 反序列化
Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
```

## 变更原因

### 1. 为什么选择 HikariCP？

- **性能优势**：HikariCP 是目前最快的数据库连接池
- **Spring Boot 默认**：Spring Boot 2.x 及以上版本默认使用 HikariCP
- **轻量级**：相比 Druid，HikariCP 更加轻量级
- **稳定性**：经过大量生产环境验证

### 2. 为什么选择 Jackson？

- **Spring Boot 默认**：Spring Boot 默认使用 Jackson
- **生态系统**：与 Spring 生态系统完美集成
- **安全性**：相比 FastJSON，Jackson 在安全方面更加可靠
- **标准化**：Jackson 是 Java 生态系统中事实上的标准 JSON 库

## 配置参数说明

### HikariCP 配置参数

| 参数 | 说明 | 默认值 | 建议值 |
|------|------|--------|--------|
| `pool-name` | 连接池名称 | HikariPool-1 | HikariCP |
| `minimum-idle` | 最小空闲连接数 | 10 | 5-10 |
| `maximum-pool-size` | 最大连接池大小 | 10 | 20-50 |
| `connection-timeout` | 连接超时时间（毫秒） | 30000 | 30000 |
| `idle-timeout` | 空闲连接超时时间（毫秒） | 600000 | 600000 |
| `max-lifetime` | 连接最大存活时间（毫秒） | 1800000 | 1800000 |
| `connection-test-query` | 连接测试查询 | - | SELECT 1 FROM DUAL |

### Jackson 配置

Spring Boot 自动配置 Jackson，支持以下常用配置：

```yaml
spring:
  jackson:
    # 日期格式
    date-format: yyyy-MM-dd HH:mm:ss
    # 时区
    time-zone: GMT+8
    # 序列化配置
    serialization:
      # 忽略空值
      write-nulls-as-empty: true
      # 美化输出
      indent-output: true
    # 反序列化配置
    deserialization:
      # 忽略未知属性
      fail-on-unknown-properties: false
```

## 迁移指南

### 1. 数据库连接池迁移

1. **更新依赖**：在 `pom.xml` 中替换依赖
2. **更新配置**：修改 `application.yml` 中的数据源配置
3. **测试连接**：确保数据库连接正常
4. **性能测试**：验证连接池性能

### 2. JSON 库迁移

1. **更新依赖**：在 `pom.xml` 中替换依赖
2. **更新代码**：替换所有 FastJSON2 的使用
3. **测试功能**：确保 JSON 序列化/反序列化正常
4. **性能测试**：验证 JSON 处理性能

### 3. 代码迁移示例

#### 序列化迁移
```java
// 旧代码（FastJSON2）
String json = JSON.toJSONString(user);

// 新代码（Jackson）
String json = objectMapper.writeValueAsString(user);
```

#### 反序列化迁移
```java
// 旧代码（FastJSON2）
User user = JSON.parseObject(json, User.class);

// 新代码（Jackson）
User user = objectMapper.readValue(json, User.class);
```

#### 复杂类型反序列化
```java
// 旧代码（FastJSON2）
Map<String, Object> map = JSON.parseObject(json, Map.class);

// 新代码（Jackson）
Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
```

## 注意事项

### 1. HikariCP 注意事项

- **连接测试查询**：Oracle 数据库使用 `SELECT 1 FROM DUAL`
- **连接超时**：生产环境建议适当增加超时时间
- **连接池大小**：根据数据库性能和业务需求调整

### 2. Jackson 注意事项

- **异常处理**：Jackson 抛出 `JsonProcessingException`
- **类型引用**：复杂泛型需要使用 `TypeReference`
- **日期处理**：需要配置适当的日期格式和时区

## 性能对比

### 连接池性能

| 指标 | Druid | HikariCP |
|------|-------|----------|
| 连接获取速度 | 中等 | 最快 |
| 内存占用 | 较高 | 较低 |
| 功能丰富度 | 高 | 中等 |
| 配置复杂度 | 高 | 低 |

### JSON 处理性能

| 指标 | FastJSON2 | Jackson |
|------|-----------|---------|
| 序列化速度 | 快 | 中等 |
| 反序列化速度 | 快 | 中等 |
| 内存占用 | 较低 | 中等 |
| 安全性 | 中等 | 高 |

## 总结

通过将 Druid 替换为 HikariCP，FastJSON2 替换为 Jackson，项目获得了：

1. **更好的性能**：HikariCP 提供更快的数据库连接
2. **更高的安全性**：Jackson 在安全方面更加可靠
3. **更好的集成**：与 Spring Boot 生态系统完美集成
4. **更简单的配置**：减少了配置复杂度
5. **更稳定的运行**：经过大量生产环境验证

这些变更使项目更加现代化、安全化和标准化。 