# 企业级Spring Boot框架 - 模块结构说明

## 项目概述

本项目是一个企业级Spring Boot框架，采用多模块Maven项目结构，提供完整的微服务开发解决方案。

## 模块结构

```
enterprise-framework-parent/
├── enterprise-framework-common/          # 公共模块
├── enterprise-framework-security/        # 安全模块
├── enterprise-framework-cap/             # CAP消息组件模块
├── enterprise-framework-generator/       # 代码生成器模块
├── enterprise-framework-starter/         # 自动配置模块
├── enterprise-framework-web/             # Web示例模块
└── business-system-example/              # 业务系统示例
```

## 模块详细说明

### 1. enterprise-framework-common (公共模块)
**功能**: 提供框架的基础公共功能
- 统一返回结果封装 (`Result`, `ResultCode`)
- 全局异常处理 (`BusinessException`, `GlobalExceptionHandler`)
- 基础实体类 (`BaseEntity`)
- 通用工具类和常量

**依赖**: 无内部依赖，可独立使用

### 2. enterprise-framework-security (安全模块)
**功能**: 提供JWT认证和授权功能
- JWT令牌工具 (`JwtTokenUtil`)
- JWT认证过滤器 (`JwtAuthenticationFilter`)
- 用户详情服务 (`UserDetailsServiceImpl`)
- 安全配置 (`SecurityConfig`)

**依赖**: 
- enterprise-framework-common

### 3. enterprise-framework-cap (CAP消息组件模块)
**功能**: 实现.NET Core CAP组件的Java版本
- 消息发布订阅 (`CapPublisher`, `CapSubscriber`)
- 消息存储 (`MessageStorage`)
- 消息队列 (`MessageQueue`)
- 事务消息和延迟消息支持

**依赖**: 
- enterprise-framework-common
- Spring Boot Starter
- Jackson (JSON序列化)

### 4. enterprise-framework-generator (代码生成器模块)
**功能**: 提供代码生成功能
- 代码生成器 (`CodeGenerator`)
- 模板引擎支持
- 数据库表结构分析

**依赖**: 
- enterprise-framework-common
- MyBatis Plus

### 5. enterprise-framework-starter (自动配置模块)
**功能**: 提供Spring Boot自动配置
- 整合所有子模块
- 自动配置类
- 依赖管理

**依赖**: 
- 所有子模块
- Spring Boot Starters
- MyBatis Plus
- Oracle JDBC
- Apollo配置中心
- Knife4j API文档

### 6. enterprise-framework-web (Web示例模块)
**功能**: 展示框架功能的Web应用
- 主启动类 (`FrameworkApplication`)
- 示例控制器
- 配置文件

**依赖**: 
- enterprise-framework-starter
- Spring Boot Web

### 7. business-system-example (业务系统示例)
**功能**: 展示如何在业务系统中使用框架
- 业务系统启动类
- 用户管理示例
- 消息发布订阅示例

**依赖**: 
- enterprise-framework-starter
- Spring Boot Web

## 使用方式

### 方式一：使用完整框架 (推荐)
在业务项目中引入 `enterprise-framework-starter`：

```xml
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>enterprise-framework-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 方式二：按需引入模块
根据业务需求选择性引入特定模块：

```xml
<!-- 只需要公共功能 -->
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>enterprise-framework-common</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- 需要安全功能 -->
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>enterprise-framework-security</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- 需要消息功能 -->
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>enterprise-framework-cap</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 版本管理

所有模块使用统一的版本管理，在父pom.xml中定义：

```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
    <mybatis-plus.version>3.5.4.1</mybatis-plus.version>
    <apollo.version>2.1.0</apollo.version>
    <knife4j.version>4.3.0</knife4j.version>
    <jackson.version>2.15.3</jackson.version>
    <lombok.version>1.18.30</lombok.version>
</properties>
```

## 构建和运行

### 构建整个项目
```bash
mvn clean install
```

### 运行Web示例
```bash
cd enterprise-framework-web
mvn spring-boot:run
```

### 运行业务系统示例
```bash
cd business-system-example
mvn spring-boot:run
```

## 配置说明

### 必需配置
1. **数据库配置**: Oracle数据库连接信息
2. **Apollo配置**: 配置中心连接信息
3. **Redis配置**: 缓存和会话存储
4. **RabbitMQ配置**: 消息队列

### 可选配置
1. **Knife4j配置**: API文档配置
2. **日志配置**: 日志级别和输出格式
3. **安全配置**: JWT密钥和过期时间

## 扩展开发

### 添加新模块
1. 在父pom.xml中添加模块声明
2. 创建模块目录和pom.xml
3. 实现模块功能
4. 在starter模块中添加依赖

### 自定义配置
1. 创建配置类继承`@ConfigurationProperties`
2. 在starter模块的spring.factories中注册
3. 在业务项目中配置相关属性

## 最佳实践

1. **模块职责单一**: 每个模块只负责特定功能
2. **依赖最小化**: 避免循环依赖，合理设计依赖关系
3. **版本统一**: 使用父pom统一管理版本
4. **配置外部化**: 使用Apollo配置中心管理配置
5. **文档完善**: 为每个模块提供详细的使用文档 