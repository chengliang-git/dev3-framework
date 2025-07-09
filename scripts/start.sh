#!/bin/bash

# 企业级Spring Boot框架启动脚本

echo "===================================================="
echo "企业级Spring Boot框架启动脚本"
echo "===================================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: Java 17或更高版本未安装"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven未安装"
    exit 1
fi

# 设置环境变量
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}
export SERVER_PORT=${SERVER_PORT:-8080}

# 设置Apollo配置
export APOLLO_APP_ID=${APOLLO_APP_ID:-enterprise-framework}
export APOLLO_META=${APOLLO_META:-http://localhost:8080}

echo "当前环境: $SPRING_PROFILES_ACTIVE"
echo "服务端口: $SERVER_PORT"

# 清理并编译项目
echo "正在编译项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi

echo "项目编译成功!"

# 启动应用
echo "正在启动应用..."
mvn spring-boot:run \
  -Dspring-boot.run.profiles=$SPRING_PROFILES_ACTIVE \
  -Dapollo.app.id=$APOLLO_APP_ID \
  -Dapollo.meta=$APOLLO_META \
  -Dapollo.timeout=5000 \
  -Dapollo.bootstrap.enabled=true \
  -Dapollo.bootstrap.eagerLoad.enabled=true \
  -Dapollo.cache-dir=./config-cache

echo "应用启动完成!"
echo "访问地址:"
echo "  - 应用首页: http://localhost:$SERVER_PORT/api"
echo "  - API文档: http://localhost:$SERVER_PORT/doc.html"
echo "  - 健康检查: http://localhost:$SERVER_PORT/actuator/health"