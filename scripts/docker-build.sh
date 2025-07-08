#!/bin/bash

# Docker构建脚本

echo "===================================================="
echo "企业级Spring Boot框架 Docker构建脚本"
echo "===================================================="

# 项目配置
PROJECT_NAME="enterprise-framework"
VERSION="1.0.0"
IMAGE_NAME="$PROJECT_NAME:$VERSION"

# 清理并打包项目
echo "正在清理并打包项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "错误: 项目打包失败"
    exit 1
fi

echo "项目打包成功!"

# 构建Docker镜像
echo "正在构建Docker镜像: $IMAGE_NAME"
docker build -t $IMAGE_NAME .

if [ $? -ne 0 ]; then
    echo "错误: Docker镜像构建失败"
    exit 1
fi

echo "Docker镜像构建成功!"

# 显示镜像信息
echo "镜像信息:"
docker images | grep $PROJECT_NAME

echo ""
echo "运行容器命令:"
echo "docker run -d --name $PROJECT_NAME -p 8080:8080 $IMAGE_NAME"

echo ""
echo "构建完成!"