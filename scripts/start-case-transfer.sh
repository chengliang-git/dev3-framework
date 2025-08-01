#!/bin/bash

# 案件转存服务启动脚本

# 设置项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CASE_TRANSFER_DIR="$PROJECT_ROOT/guanwei-tles-case-transfer"

# 设置Java选项
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 设置Spring配置文件
SPRING_PROFILES=${1:-dev}

echo "启动案件转存服务..."
echo "项目目录: $CASE_TRANSFER_DIR"
echo "Spring配置文件: $SPRING_PROFILES"
echo "Java选项: $JAVA_OPTS"

# 切换到项目目录
cd "$CASE_TRANSFER_DIR"

# 启动服务
mvn spring-boot:run \
    -Dspring-boot.run.jvmArguments="$JAVA_OPTS" \
    -Dspring-boot.run.profiles="$SPRING_PROFILES" 