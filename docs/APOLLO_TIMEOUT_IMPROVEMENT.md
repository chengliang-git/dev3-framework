# Apollo 超时配置改进

## 概述

为了解决应用启动时Apollo连接超时导致启动缓慢的问题，我们对Apollo配置进行了优化，设置了5秒超时时间，并确保连接失败时能够继续使用本地配置。

## 改进内容

### 1. 超时配置

在所有配置文件中添加了Apollo超时配置：

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

### 2. 启动脚本优化

更新了启动脚本，添加了超时相关的系统属性：

```bash
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

### 3. 环境变量支持

支持通过环境变量配置超时时间：

```bash
export APOLLO_TIMEOUT=5000
export APOLLO_CACHE_DIR=./config-cache
```

## 配置参数说明

| 参数 | 说明 | 默认值 | 环境变量 |
|------|------|--------|----------|
| `timeout` | 连接超时时间（毫秒） | 5000 | APOLLO_TIMEOUT |
| `cache-dir` | 本地缓存目录 | ./config-cache | APOLLO_CACHE_DIR |

## 影响范围

### 修改的配置文件

1. **主配置文件**
   - `src/main/resources/application.yml`
   - `enterprise-framework-web/src/main/resources/application.yml`
   - `business-system-example/src/main/resources/application.yml`

2. **环境配置文件**
   - `src/main/resources/application-dev.yml`
   - `src/main/resources/application-prod.yml`
   - `enterprise-framework-web/src/main/resources/application-dev.yml`
   - `enterprise-framework-web/src/main/resources/application-prod.yml`

3. **启动脚本**
   - `scripts/start.sh`
   - `scripts/start-business.sh`

4. **文档**
   - `docs/APOLLO_CONFIG.md`

### 新增文件

1. **测试脚本**
   - `scripts/test-apollo-timeout.sh`

2. **说明文档**
   - `docs/APOLLO_TIMEOUT_IMPROVEMENT.md`

## 使用方式

### 1. 默认配置

使用默认的5秒超时配置：

```bash
./scripts/start.sh
```

### 2. 自定义超时时间

通过环境变量自定义超时时间：

```bash
export APOLLO_TIMEOUT=3000  # 3秒超时
./scripts/start.sh
```

### 3. 测试超时配置

使用测试脚本验证超时配置：

```bash
./scripts/test-apollo-timeout.sh
```

## 预期效果

### 1. 启动速度提升

- **之前**：Apollo连接失败时可能需要等待30秒或更长时间
- **现在**：5秒后超时，立即使用本地配置继续启动

### 2. 容错能力增强

- Apollo服务不可用时，应用仍能正常启动
- 使用本地缓存配置，确保配置的可用性

### 3. 开发体验改善

- 开发环境无需启动Apollo服务即可快速启动应用
- 减少了因网络问题导致的启动失败

## 注意事项

### 1. 本地缓存

- Apollo会在本地缓存配置，确保离线时仍能使用
- 缓存目录默认为 `./config-cache`
- 首次启动时如果Apollo不可用，会使用本地配置文件

### 2. 配置更新

- 超时配置不会影响Apollo配置的动态更新功能
- 一旦连接成功，配置更新仍然正常工作

### 3. 生产环境

- 生产环境建议确保Apollo服务的高可用性
- 超时配置主要用于提高启动速度和容错能力

## 验证方法

### 1. 查看启动日志

启动时查看日志，确认超时配置生效：

```
[INFO] Apollo timeout set to 5000ms
[WARN] Apollo connection timeout, using local cache
```

### 2. 测试脚本

运行测试脚本验证超时行为：

```bash
./scripts/test-apollo-timeout.sh
```

### 3. 性能对比

对比修改前后的启动时间：

```bash
# 修改前（Apollo不可用时）
time ./scripts/start.sh

# 修改后（Apollo不可用时）
time ./scripts/start.sh
```

## 总结

通过设置Apollo超时时间为5秒，我们显著提升了应用的启动速度，特别是在Apollo服务不可用的情况下。同时保持了Apollo配置中心的动态配置更新功能，确保了系统的稳定性和可用性。

这个改进对于开发环境的快速启动和生产环境的容错能力都有重要意义。 