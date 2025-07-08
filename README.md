# 企业级Spring Boot 3开发框架

## 📖 项目简介

这是一个基于Spring Boot 3.2.0构建的企业级开发框架，集成了当前主流的技术栈，提供了完整的基础功能模块，开箱即用，帮助开发者快速构建企业级应用。

## ✨ 框架特性

### 🏗️ 技术栈

- **基础框架**: Spring Boot 3.2.0 + Spring Security 6
- **数据库**: Oracle 23c + MyBatis Plus 3.5.4
- **连接池**: Druid 1.2.20
- **认证授权**: JWT 4.4.0
- **缓存**: Redis + Spring Cache
- **消息队列**: RabbitMQ
- **配置中心**: Apollo 2.1.0
- **API文档**: Knife4j 4.3.0 (基于OpenAPI 3)
- **代码生成**: MyBatis Plus Generator
- **工具类**: Hutool 5.8.22

### 🚀 核心功能

1. **统一响应处理**
   - 统一API响应格式
   - 全局异常处理
   - 标准状态码定义

2. **JWT认证授权**
   - 无状态JWT认证
   - 灵活的权限控制
   - 安全配置

3. **数据库集成**
   - Oracle数据库支持
   - MyBatis Plus增强
   - 代码生成器
   - 分页、逻辑删除等功能

4. **缓存支持**
   - Redis缓存
   - Spring Cache注解
   - 自定义序列化

5. **消息队列**
   - RabbitMQ集成
   - 死信队列处理
   - 消息确认机制

6. **配置管理**
   - Apollo配置中心
   - 多环境配置
   - 动态配置刷新

7. **API文档**
   - Knife4j文档生成
   - 在线调试
   - 接口测试

## 🗂️ 项目结构

```
enterprise-spring-boot-framework/
├── src/main/java/com/enterprise/framework/
│   ├── common/               # 公共模块
│   │   ├── exception/       # 异常处理
│   │   └── result/          # 响应结果
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java      # 安全配置
│   │   ├── RedisConfig.java         # Redis配置
│   │   ├── RabbitMQConfig.java      # RabbitMQ配置
│   │   ├── MyBatisPlusConfig.java   # MyBatis Plus配置
│   │   └── Knife4jConfig.java       # API文档配置
│   ├── controller/          # 控制器
│   │   ├── AuthController.java      # 认证接口
│   │   └── GeneratorController.java # 代码生成接口
│   ├── entity/              # 实体类
│   │   ├── BaseEntity.java         # 基础实体
│   │   └── User.java               # 用户实体
│   ├── generator/           # 代码生成器
│   │   └── CodeGenerator.java      # 代码生成器
│   ├── mapper/              # 数据访问层
│   │   └── UserMapper.java         # 用户Mapper
│   ├── security/            # 安全模块
│   │   ├── JwtProperties.java      # JWT配置
│   │   ├── JwtTokenUtil.java       # JWT工具类
│   │   └── JwtAuthenticationFilter.java # JWT过滤器
│   ├── service/             # 服务层
│   │   └── UserDetailsServiceImpl.java # 用户详情服务
│   └── FrameworkApplication.java    # 启动类
├── src/main/resources/
│   ├── application.yml              # 主配置文件
│   ├── application-dev.yml          # 开发环境配置
│   └── application-prod.yml         # 生产环境配置
└── pom.xml                         # Maven配置
```

## 🛠️ 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Oracle 19c+
- Redis 6.0+
- RabbitMQ 3.8+
- Apollo配置中心 (可选)

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/your-org/enterprise-spring-boot-framework.git
cd enterprise-spring-boot-framework
```

2. **配置数据库**
```sql
-- 创建用户表
CREATE TABLE t_user (
    id NUMBER(19) PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(100) NOT NULL,
    real_name VARCHAR2(50),
    email VARCHAR2(100),
    phone VARCHAR2(20),
    status NUMBER(1) DEFAULT 1,
    avatar VARCHAR2(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0
);

-- 插入测试用户 (密码是 admin)
INSERT INTO t_user (id, username, password, real_name, status)
VALUES (1, 'admin', '$2a$10$7JB720yubVSOMV0H5nnZP.IhbU6B3SrEP1KcxqvjTM1YRKDC/T3bC', '管理员', 1);
```

3. **修改配置**
```yaml
# 修改 application-dev.yml 中的数据库连接信息
spring:
  datasource:
    druid:
      url: jdbc:oracle:thin:@localhost:1521:XE
      username: your_username
      password: your_password
```

4. **启动应用**
```bash
mvn spring-boot:run
```

5. **访问应用**
- 应用地址: http://localhost:8080/api
- API文档: http://localhost:8080/doc.html
- 数据库监控: http://localhost:8080/druid (admin/admin123)

## 📚 使用说明

### 认证接口

#### 用户登录
```bash
POST /api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin"
}
```

#### 获取用户信息
```bash
POST /api/auth/info
Authorization: Bearer <token>
```

### 代码生成器

#### 生成代码
```bash
POST /api/generator/generate
Content-Type: application/json

{
    "tableNames": ["t_user", "t_role"]
}
```

### 缓存使用

```java
@Service
public class UserService {
    
    @Cacheable(value = "user", key = "#id")
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    @CacheEvict(value = "user", key = "#user.id")
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
}
```

### 消息队列使用

```java
@Component
public class MessageProducer {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void sendMessage(Object message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.FRAMEWORK_EXCHANGE,
            RabbitMQConfig.FRAMEWORK_ROUTING_KEY,
            message
        );
    }
}

@RabbitListener(queues = RabbitMQConfig.FRAMEWORK_QUEUE)
public void handleMessage(String message, Channel channel, 
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
    try {
        // 处理消息
        System.out.println("收到消息: " + message);
        channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
        channel.basicNack(deliveryTag, false, true);
    }
}
```

## 🔧 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    druid:
      url: jdbc:oracle:thin:@localhost:1521:XE
      username: framework
      password: framework123
      initial-size: 5
      min-idle: 5
      max-active: 20
```

### JWT配置
```yaml
jwt:
  secret: enterprise-framework-jwt-secret-key-2024
  expiration: 86400000  # 24小时
  header: Authorization
  prefix: Bearer
```

### Redis配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 5000ms
```

### RabbitMQ配置
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
```

## 📋 开发规范

### 代码规范

1. **命名规范**
   - 类名使用大驼峰命名法
   - 方法名和变量名使用小驼峰命名法
   - 常量使用大写字母和下划线

2. **注释规范**
   - 类和方法必须添加JavaDoc注释
   - 复杂业务逻辑需要添加行内注释

3. **异常处理**
   - 使用统一的异常处理机制
   - 业务异常使用BusinessException
   - 避免捕获Exception

### 数据库规范

1. **表设计规范**
   - 表名使用t_开头
   - 字段名使用下划线命名法
   - 必须包含id、create_time、update_time、deleted字段

2. **SQL规范**
   - 禁止使用SELECT *
   - 必须使用参数化查询
   - 复杂查询建议使用XML配置

## 🚀 部署说明

### Docker部署

1. **构建镜像**
```bash
mvn clean package
docker build -t enterprise-framework:1.0.0 .
```

2. **运行容器**
```bash
docker run -d \
  --name enterprise-framework \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:oracle:thin:@oracle:1521:PROD \
  -e DB_USERNAME=framework \
  -e DB_PASSWORD=framework123 \
  enterprise-framework:1.0.0
```

### Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: enterprise-framework
spec:
  replicas: 3
  selector:
    matchLabels:
      app: enterprise-framework
  template:
    metadata:
      labels:
        app: enterprise-framework
    spec:
      containers:
      - name: enterprise-framework
        image: enterprise-framework:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_URL
          value: "jdbc:oracle:thin:@oracle:1521:PROD"
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目基于 Apache 2.0 许可证开源 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系我们

- 项目主页: https://github.com/enterprise/framework
- 问题反馈: https://github.com/enterprise/framework/issues
- 邮箱: framework@enterprise.com

## 🙏 致谢

感谢以下开源项目的支持：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [Knife4j](https://doc.xiaominfo.com/)
- [Hutool](https://hutool.cn/)
- [Apollo](https://www.apolloconfig.com/)
