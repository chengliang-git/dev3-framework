#!/bin/bash

# 企业级Spring Boot框架测试脚本

echo "===================================================="
echo "企业级Spring Boot框架API测试脚本"
echo "===================================================="

# 配置
BASE_URL="http://localhost:8080/api"
USERNAME="admin"
PASSWORD="admin"

# 检查服务是否启动
echo "检查服务状态..."
curl -s -f "$BASE_URL/actuator/health" > /dev/null

if [ $? -ne 0 ]; then
    echo "错误: 服务未启动或无法访问"
    echo "请确保应用已启动并监听8080端口"
    exit 1
fi

echo "✓ 服务正常运行"

# 测试登录接口
echo ""
echo "测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

if [ $? -ne 0 ]; then
    echo "✗ 登录请求失败"
    exit 1
fi

echo "登录响应: $LOGIN_RESPONSE"

# 提取Token
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "✗ 无法获取Token"
    exit 1
fi

echo "✓ 登录成功，Token: ${TOKEN:0:20}..."

# 测试获取用户信息接口
echo ""
echo "测试获取用户信息..."
USER_INFO_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/info" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN")

if [ $? -ne 0 ]; then
    echo "✗ 获取用户信息失败"
    exit 1
fi

echo "用户信息响应: $USER_INFO_RESPONSE"
echo "✓ 获取用户信息成功"

# 测试退出接口
echo ""
echo "测试用户退出..."
LOGOUT_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/logout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN")

if [ $? -ne 0 ]; then
    echo "✗ 退出请求失败"
    exit 1
fi

echo "退出响应: $LOGOUT_RESPONSE"
echo "✓ 用户退出成功"

echo ""
echo "===================================================="
echo "所有API测试通过! ✓"
echo "===================================================="
echo ""
echo "访问链接:"
echo "  - API文档: http://localhost:8080/doc.html"
echo "  - 数据库监控: http://localhost:8080/druid (admin/admin123)"
echo "  - 健康检查: http://localhost:8080/api/actuator/health"