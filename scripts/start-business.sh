#!/bin/bash

# 业务系统示例启动脚本

# 设置Apollo配置中心
export APOLLO_APP_ID=business-system-example
export APOLLO_META=http://localhost:8080

# 设置数据库连接
export ORACLE_USERNAME=business
export ORACLE_PASSWORD=business123

# 设置Redis连接
export REDIS_HOST=localhost
export REDIS_PORT=6379

# 设置RabbitMQ连接
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest

# 设置Java系统属性
JAVA_OPTS="-Dapollo.app.id=$APOLLO_APP_ID"
JAVA_OPTS="$JAVA_OPTS -Dapollo.meta=$APOLLO_META"
JAVA_OPTS="$JAVA_OPTS -Dapollo.bootstrap.enabled=true"
JAVA_OPTS="$JAVA_OPTS -Dapollo.bootstrap.eagerLoad.enabled=true"

echo "启动业务系统示例..."
echo "Apollo配置: $APOLLO_APP_ID -> $APOLLO_META"
echo "数据库: $ORACLE_USERNAME@localhost:1521:XE"
echo "Redis: $REDIS_HOST:$REDIS_PORT"
echo "RabbitMQ: $RABBITMQ_HOST:$RABBITMQ_PORT"

cd business-system-example
mvn spring-boot:run -Dspring-boot.run.jvmArguments="$JAVA_OPTS" 