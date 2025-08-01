# 企业级Spring Boot框架优化总结

## 优化概述

本次优化主要针对企业级开发需求，对框架进行了全面调整，包括企业名空间、数据库字段命名规范、主键类型、审计字段等。

## 主要优化内容

### 1. 企业名空间调整

#### 1.1 包名规范
- **原包名**: `com.guanwei.framework`
- **新包名**: `com.guanwei.framework`
- **优势**: 
  - 符合企业命名规范
  - 避免与通用框架冲突
  - 便于企业内部分发和管理

#### 1.2 模块命名
- **原模块名**: `enterprise-framework-*`
- **新模块名**: `guanwei-framework-*`
- **影响模块**:
  - guanwei-framework-common
  - guanwei-framework-security
  - guanwei-framework-cap
  - guanwei-framework-generator
  - guanwei-framework-starter
  - guanwei-framework-web

### 2. 数据库字段命名优化

#### 2.1 驼峰式命名
- **原字段名**: 下划线命名（如：`create_time`, `modify_time`, `del_flag`）
- **新字段名**: 驼峰命名（如：`createTime`, `modifyTime`, `delFlag`）
- **优势**:
  - 符合Java命名规范
  - 减少字段映射配置
  - 提高代码可读性

#### 2.2 字段映射调整
```java
// 基础实体字段
@TableField(value = "createTime", fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(value = "modifyTime", fill = FieldFill.INSERT_UPDATE)
private LocalDateTime modifyTime;

@TableField(value = "creator", fill = FieldFill.INSERT)
private String creator;

@TableField(value = "modifier", fill = FieldFill.INSERT_UPDATE)
private String modifier;

@TableLogic
@TableField(value = "delFlag")
private Integer delFlag;

@TableField(value = "orderNo")
private Integer orderNo;
```

### 3. 主键类型优化

#### 3.1 32位GUID主键
- **原主键**: Long类型自增ID
- **新主键**: String类型32位GUID
- **优势**:
  - 分布式友好
  - 避免ID冲突
  - 提高安全性
  - 便于数据迁移

#### 3.2 主键生成策略
```java
@TableId(value = "id", type = IdType.ASSIGN_UUID)
private String id;
```

### 4. 顺序号处理优化

#### 4.1 前端提供顺序号
- **原策略**: 后端自动生成顺序号
- **新策略**: 前端提供顺序号
- **优势**:
  - 提高前端控制能力
  - 减少后端计算负担
  - 支持拖拽排序等场景

#### 4.2 自动填充调整
```java
// 移除顺序号自动填充
// this.strictInsertFill(metaObject, "orderNo", Integer.class, 1);
```

### 5. 数据库支持优化

#### 5.1 Oracle数据库支持
- **分页插件**: 配置Oracle分页插件
- **字段类型**: 适配Oracle数据类型
- **建表脚本**: 提供完整的Oracle建表脚本

#### 5.2 MongoDB支持
- **基础实体**: `BaseMongoEntity` 支持MongoDB审计
- **Repository**: `BaseMongoRepository` 提供MongoDB操作
- **Service**: `BaseMongoService` 定义MongoDB业务接口
- **Controller**: `BaseMongoController` 提供MongoDB REST API

### 6. 代码生成器优化

#### 6.1 模板更新
- **实体模板**: 支持新的字段命名和主键类型
- **配置更新**: 调整包名、作者等配置
- **排除字段**: 更新排除字段列表

#### 6.2 生成配置
```java
// 代码生成器配置
private String author = "Guanwei Framework";
private String packageName = "com.guanwei.framework";
private List<String> excludeColumns = Arrays.asList(
    "id", "createTime", "modifyTime", "creator", 
    "modifier", "delFlag", "orderNo"
);
```

### 7. 数据库建表脚本

#### 7.1 完整表结构
- **用户表**: t_user
- **部门表**: t_dept
- **角色表**: t_role
- **权限表**: t_permission
- **关联表**: t_user_role, t_role_permission
- **配置表**: t_config
- **日志表**: t_operation_log, t_login_log

#### 7.2 索引优化
- 为常用查询字段创建索引
- 支持复合索引
- 优化查询性能

## 技术特性

### 1. 数据库支持
- **Oracle**: 主要数据库，支持分页、审计
- **MongoDB**: 文档数据库，支持日志存储
- **MySQL**: 兼容支持（通过配置调整）

### 2. 主键策略
- **GUID**: 32位字符串主键
- **分布式**: 支持分布式部署
- **安全**: 避免ID泄露风险

### 3. 审计功能
- **创建时间**: 自动填充创建时间
- **修改时间**: 自动更新修改时间
- **创建人**: 记录数据创建人
- **修改人**: 记录数据修改人
- **删除标记**: 逻辑删除支持

### 4. 字段规范
- **驼峰命名**: 所有字段使用驼峰命名
- **类型安全**: 强类型字段定义
- **注释完整**: 完整的字段注释

## 使用指南

### 1. 创建新实体

```java
@Entity
@TableName("t_example")
public class Example extends BaseEntity {
    
    @Schema(description = "示例字段")
    @TableField("exampleField")
    private String exampleField;
    
    // getter/setter
}
```

### 2. 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: guanwei
    password: guanwei123
    driver-class-name: oracle.jdbc.OracleDriver
```

### 3. MongoDB配置

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: guanwei_framework
```

### 4. 代码生成

```java
// 生成代码
codeGenerator.generateCode("t_user", "t_role", "t_permission");
```

## 最佳实践

### 1. 命名规范
- **包名**: `com.guanwei.{module}`
- **类名**: 使用大驼峰命名
- **字段名**: 使用小驼峰命名
- **表名**: 使用 `t_` 前缀

### 2. 数据库设计
- **主键**: 使用32位GUID
- **字段**: 使用驼峰命名
- **索引**: 为查询字段创建索引
- **约束**: 添加必要的约束

### 3. 代码开发
- **继承基础类**: 实体继承BaseEntity
- **使用注解**: 添加完整的注解
- **异常处理**: 使用统一异常处理
- **日志记录**: 记录关键操作日志

### 4. 配置管理
- **环境配置**: 区分开发、测试、生产环境
- **敏感信息**: 使用环境变量管理
- **配置验证**: 启动时验证配置

## 总结

通过本次优化，框架具备了以下企业级特性：

1. **标准化**: 统一的企业命名规范
2. **规范化**: 标准的数据库设计
3. **高性能**: 优化的查询和索引
4. **可扩展**: 支持多种数据库
5. **易维护**: 清晰的代码结构
6. **安全性**: 安全的ID生成策略

这些优化使得框架更适合企业级应用开发，提高了开发效率和代码质量，为企业的数字化转型提供了强有力的技术支撑。 