# Guanwei Framework 快速开始指南

## 概述

Guanwei Framework 是一个企业级 Spring Boot 框架，提供了完整的开发基础设施。

## 环境要求

- **Java**: 17+
- **Maven**: 3.6+
- **数据库**: Oracle 19c+ / MongoDB 4.4+

## 快速开始

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

#### 2.1 创建 pom.xml

```xml
<dependency>
    <groupId>com.guanwei</groupId>
    <artifactId>guanwei-framework-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 2.2 创建启动类

```java
@SpringBootApplication
public class MyProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyProjectApplication.class, args);
    }
}
```

#### 2.3 创建实体类

```java
@TableName("t_user")
public class User extends BaseEntity {
    private String username;
    private String realName;
    // getter/setter
}
```

#### 2.4 创建 Controller

```java
@RestController
@RequestMapping("/users")
public class UserController extends BaseController<UserService, User> {
}
```

### 3. 运行项目

```bash
mvn spring-boot:run
```

访问：http://localhost:8080/doc.html

## 更多信息

查看完整文档：[ENTERPRISE_OPTIMIZATION.md](./ENTERPRISE_OPTIMIZATION.md)
