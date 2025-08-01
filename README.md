# Guanwei Framework

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.12-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 概述

Guanwei Framework 是一个企业级 Spring Boot 框架，提供了完整的开发基础设施，包括安全认证、数据访问、代码生成、API 文档等功能。

## ✨ 特性

- 🚀 **快速开发**: 基于 Spring Boot 3.2.12，开箱即用
- 🔐 **安全认证**: JWT 无状态认证，支持角色权限控制
- 🗄️ **数据访问**: 支持 Oracle 和 MongoDB 双数据库
- 📝 **代码生成**: 自动生成 CRUD 代码，提高开发效率
- 📚 **API 文档**: 集成 Knife4j，自动生成 API 文档
- 🎯 **统一规范**: 统一的返回格式、异常处理、日志记录
- 🔧 **配置管理**: 支持 Apollo 配置中心，环境隔离
- 📦 **模块化**: 清晰的模块划分，便于维护和扩展

## 🏗️ 架构设计

```
guanwei-framework/
├── guanwei-framework-common/     # 公共模块
├── guanwei-framework-security/   # 安全模块
├── guanwei-framework-cap/        # CAP模块
├── guanwei-framework-generator/  # 代码生成器
├── guanwei-framework-starter/    # 自动配置模块
├── guanwei-framework-web/        # Web示例模块
└── business-system-example/      # 业务系统示例
```

## 🚀 快速开始

### 环境要求

- **Java**: 17 或更高版本
- **Maven**: 3.6 或更高版本
- **数据库**: Oracle 19c+ / MongoDB 4.4+
- **Redis**: 6.0+ (可选)
- **RabbitMQ**: 3.8+ (可选)

### 1. 部署框架到本地仓库

```bash
# 克隆项目
git clone <repository-url>
cd guanwei-framework

# 部署到本地仓库
chmod +x scripts/deploy-to-local-repo.sh
./scripts/deploy-to-local-repo.sh
```

### 2. 创建新项目

#### 2.1 添加依赖

```xml
<dependency>
    <groupId>com.guanwei</groupId>
    <artifactId>guanwei-framework-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 2.2 创建实体类

```java
@TableName("t_user")
public class User extends BaseEntity {
    private String username;
    private String realName;
    // getter/setter
}
```

#### 2.3 创建 Controller

```java
@RestController
@RequestMapping("/users")
public class UserController extends BaseController<UserService, User> {
}
```

#### 2.4 运行项目

```bash
mvn spring-boot:run
```

访问：http://localhost:8080/doc.html

## 📖 详细文档

- [快速开始指南](./docs/QUICK_START.md)
- [企业级优化说明](./docs/ENTERPRISE_OPTIMIZATION.md)
- [架构优化说明](./docs/ARCHITECTURE_OPTIMIZATION.md)

## 🛠️ 技术栈

- **核心框架**: Spring Boot 3.2.12
- **安全框架**: Spring Security + JWT
- **数据访问**: MyBatis Plus + Oracle/MongoDB
- **缓存**: Redis + Caffeine
- **消息队列**: RabbitMQ
- **API 文档**: Knife4j (Swagger)
- **配置中心**: Apollo
- **代码生成**: MyBatis Plus Generator

## 🎯 核心功能

### 1. 统一数据访问层

```java
// 继承基础Repository
public interface UserRepository extends BaseRepository<UserMapper, User> {
    // 自动获得CRUD操作
}

// 继承基础Service
public interface UserService extends BaseService<User> {
    // 自动获得业务操作
}

// 继承基础Controller
public class UserController extends BaseController<UserService, User> {
    // 自动获得REST API
}
```

### 2. 统一返回结果

```java
// 成功返回
return Result.success(data);

// 失败返回
return Result.error("错误信息");
```

### 3. 全局异常处理

```java
// 抛出业务异常
throw new BusinessException("用户不存在");

// 自动被全局异常处理器捕获
```

### 4. 代码生成器

```java
// 生成代码
codeGenerator.generateCode("t_user", "t_role", "t_permission");
```

## 🔧 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: your_username
    password: your_password
    driver-class-name: oracle.jdbc.OracleDriver
```

### 安全配置

```yaml
framework:
  security:
    permit-all-paths:
      - /api/auth/login
      - /doc.html
```

## 📦 模块说明

### guanwei-framework-common

公共模块，包含：

- 基础实体类 (BaseEntity, BaseMongoEntity)
- 统一返回结果 (Result)
- 异常处理 (BusinessException, GlobalExceptionHandler)
- 基础服务接口 (BaseService, BaseMongoService)
- 基础控制器 (BaseController, BaseMongoController)

### guanwei-framework-security

安全模块，包含：

- JWT 认证
- 权限控制
- 安全配置

### guanwei-framework-starter

自动配置模块，包含：

- 数据库配置 (Oracle, MongoDB)
- 缓存配置 (Redis)
- 消息队列配置 (RabbitMQ)
- API 文档配置 (Knife4j)

### guanwei-framework-generator

代码生成器，包含：

- 实体类模板
- 服务类模板
- 控制器模板
- Mapper 模板

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

- 项目主页: [GitHub Repository](https://github.com/your-org/guanwei-framework)
- 问题反馈: [Issues](https://github.com/your-org/guanwei-framework/issues)
- 文档地址: [Documentation](./docs/)

## ⭐ 如果这个项目对你有帮助，请给我们一个星标！
