# Guanwei TLES Case Transfer

案件数据转存服务 - 接收CAP消息并转存案件数据到MongoDB

## 项目简介

本项目是一个基于Spring Boot 3的企业级案件数据转存服务，主要功能包括：

- 接收CAP消息（案件新增、修改、删除）
- 从Oracle数据库查询案件及其子表数据
- 将案件数据转存到MongoDB数据库
- 提供手动同步接口

## 技术栈

- **Spring Boot 3.2.12**
- **Java 17**
- **MongoDB** - 目标数据库
- **Oracle** - 源数据库
- **MyBatis Plus** - ORM框架
- **CAP** - 消息中间件
- **Maven** - 构建工具

## 项目结构

```
guanwei-tles-case-transfer/
├── src/main/java/com/guanwei/tles/casetransfer/
│   ├── CaseTransferApplication.java          # 启动类
│   ├── config/
│   │   └── OracleDataSourceConfig.java       # Oracle数据源配置
│   ├── controller/
│   │   └── CaseTransferController.java       # 控制器
│   ├── dto/
│   │   └── CaseMessage.java                  # 消息DTO
│   ├── entity/
│   │   ├── Case.java                         # MongoDB案件实体
│   │   ├── CaseParty.java                    # MongoDB当事人实体
│   │   └── CaseDocument.java                 # MongoDB文件实体
│   ├── entity/oracle/
│   │   ├── CaseEntity.java                   # Oracle案件实体
│   │   ├── CasePartyEntity.java              # Oracle当事人实体
│   │   └── CaseDocumentEntity.java           # Oracle文件实体
│   ├── handler/
│   │   └── CaseMessageHandler.java           # CAP消息处理器
│   ├── mapper/oracle/
│   │   └── CaseMapper.java                   # Oracle Mapper接口
│   ├── repository/
│   │   └── CaseRepository.java               # MongoDB Repository
│   └── service/
│       ├── CaseTransferService.java          # 服务接口
│       └── impl/
│           └── CaseTransferServiceImpl.java  # 服务实现
├── src/main/resources/
│   ├── mapper/oracle/
│   │   └── CaseMapper.xml                    # MyBatis映射文件
│   ├── application.yml                       # 主配置文件
│   ├── application-dev.yml                   # 开发环境配置
│   └── application-prod.yml                  # 生产环境配置
└── pom.xml                                   # Maven配置
```

## 功能特性

### 1. CAP消息处理
- 订阅案件新增消息：`case.created`
- 订阅案件修改消息：`case.updated`
- 订阅案件删除消息：`case.deleted`

### 2. 数据同步
- 自动从Oracle查询案件主表数据
- 自动从Oracle查询案件当事人数据
- 自动从Oracle查询案件文件数据
- 将完整案件数据保存到MongoDB

### 3. 手动同步
- 提供REST API接口进行手动数据同步
- 支持指定案件ID进行同步

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- MongoDB 4.4+
- Oracle 11g+

### 2. 配置数据库
修改 `application.yml` 中的数据库配置：

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: tles_case_db
      username: admin
      password: admin123
  
  datasource:
    oracle:
      url: jdbc:oracle:thin:@localhost:1521:ORCL
      username: tles_user
      password: tles_pass
```

### 3. 启动服务
```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 4. 访问接口
- 健康检查：`GET http://localhost:8083/case-transfer/api/case-transfer/health`
- 手动同步：`POST http://localhost:8083/case-transfer/api/case-transfer/sync/{caseId}`
- API文档：`http://localhost:8083/case-transfer/doc.html`

## 配置说明

### CAP消息配置
```yaml
cap:
  enabled: true
  message-queue:
    type: memory
  message-storage:
    type: memory
```

### 数据库表结构
Oracle源表结构：
- `T_CASE` - 案件主表
- `T_CASE_PARTY` - 案件当事人表
- `T_CASE_DOCUMENT` - 案件文件表

MongoDB集合：
- `cases` - 案件集合（包含完整的案件数据）

## 部署说明

### Docker部署
```bash
# 构建镜像
docker build -t guanwei-tles-case-transfer .

# 运行容器
docker run -d -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MONGODB_HOST=mongodb-host \
  -e ORACLE_URL=jdbc:oracle:thin:@oracle-host:1521:ORCL \
  guanwei-tles-case-transfer
```

### 环境变量
- `SPRING_PROFILES_ACTIVE` - Spring配置文件
- `MONGODB_HOST` - MongoDB主机地址
- `MONGODB_PORT` - MongoDB端口
- `MONGODB_DATABASE` - MongoDB数据库名
- `MONGODB_USERNAME` - MongoDB用户名
- `MONGODB_PASSWORD` - MongoDB密码
- `ORACLE_URL` - Oracle连接URL
- `ORACLE_USERNAME` - Oracle用户名
- `ORACLE_PASSWORD` - Oracle密码

## 开发指南

### 添加新的案件子表
1. 创建Oracle实体类
2. 创建MongoDB实体类
3. 在Mapper中添加查询方法
4. 在服务中集成数据转换逻辑

### 扩展消息处理
1. 在`CaseMessageHandler`中添加新的消息订阅
2. 在`CaseTransferService`中添加处理方法
3. 实现具体的业务逻辑

## 监控和日志

### 健康检查
- 端点：`/actuator/health`
- 检查数据库连接状态
- 检查服务运行状态

### 日志配置
- 日志文件：`logs/case-transfer.log`
- 日志级别：开发环境DEBUG，生产环境INFO
- 日志轮转：100MB/文件，保留30天

## 许可证

本项目采用 MIT 许可证。 