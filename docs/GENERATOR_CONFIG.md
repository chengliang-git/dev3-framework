### 代码生成器配置说明

生成器支持通过配置注入基础实体与主键策略，确保与项目规范一致。

#### 配置项（application.yml）

```yaml
framework:
  generator:
    author: Guanwei Framework
    package-name: com.guanwei.project
    table-prefix: t_
    # 新增：基础实体与主键策略
    base-entity-import: com.guanwei.framework.entity.BaseEntity
    id-type: ASSIGN_ID   # 参考 MyBatis-Plus IdType 枚举：ASSIGN_ID, ASSIGN_UUID, AUTO, NONE, ...
```

#### 模板变量
- `${baseEntityImport}`: 生成实体文件时导入的 BaseEntity 路径
- `${idType}`: 主键策略（IdType 枚举名）

#### 示例生成的实体片段

```java
import ${baseEntityImport};
...
@TableId(value = "id", type = IdType.${idType})
private Long id;
```

#### 建议
- 若项目不继承 `BaseEntity`，可定制模板或将 `base-entity-import` 指向业务自定义基类。
- 对接不同数据库的主键策略时，优先采用全局统一策略，减少多样性带来的维护成本。


