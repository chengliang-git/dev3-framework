#!/bin/bash

# Apollo超时配置测试脚本

echo "===================================================="
echo "Apollo超时配置测试"
echo "===================================================="

# 设置测试环境变量
export APOLLO_APP_ID=test-app
export APOLLO_META=http://invalid-apollo-server:8080  # 故意设置为无效地址
export APOLLO_TIMEOUT=5000
export APOLLO_CACHE_DIR=./test-config-cache

echo "测试配置："
echo "  - Apollo App ID: $APOLLO_APP_ID"
echo "  - Apollo Meta: $APOLLO_META"
echo "  - Apollo Timeout: $APOLLO_TIMEOUT ms"
echo "  - Apollo Cache Dir: $APOLLO_CACHE_DIR"
echo ""

# 创建测试目录
mkdir -p $APOLLO_CACHE_DIR

# 记录开始时间
START_TIME=$(date +%s)

echo "开始测试Apollo连接超时..."
echo "预期结果：5秒后超时，使用本地配置继续启动"
echo ""

# 启动应用（仅用于测试超时）
mvn spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dapollo.app.id=$APOLLO_APP_ID \
  -Dapollo.meta=$APOLLO_META \
  -Dapollo.timeout=$APOLLO_TIMEOUT \
  -Dapollo.bootstrap.enabled=true \
  -Dapollo.bootstrap.eagerLoad.enabled=true \
  -Dapollo.cache-dir=$APOLLO_CACHE_DIR \
  -Dspring-boot.run.arguments="--server.port=0" \
  -q

# 记录结束时间
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "===================================================="
echo "测试结果："
echo "  - 启动耗时: ${DURATION}秒"
echo "  - 超时配置: ${APOLLO_TIMEOUT}ms"
echo "  - 本地缓存目录: $APOLLO_CACHE_DIR"
echo "===================================================="

# 清理测试目录
rm -rf $APOLLO_CACHE_DIR

echo "测试完成！" 