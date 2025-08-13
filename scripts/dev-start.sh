#!/bin/bash

# 开发环境启动脚本
# 用于快速启动开发环境

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  企业级框架开发环境启动脚本${NC}"
    echo -e "${BLUE}================================${NC}"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi

    print_message "Docker环境检查通过"
}

# 检查端口占用
check_ports() {
    local ports=("1521" "6379" "5672" "15672" "9000" "9001" "9090" "3000" "16686" "9200" "5601" "8848" "3306")
    
    for port in "${ports[@]}"; do
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            print_warning "端口 $port 已被占用，请检查是否有其他服务在使用"
        fi
    done
}

# 创建必要的目录
create_directories() {
    print_message "创建必要的目录..."
    
    mkdir -p config/redis
    mkdir -p config/prometheus
    mkdir -p config/grafana/dashboards
    mkdir -p config/grafana/datasources
    mkdir -p config/logstash/pipeline
    mkdir -p scripts
    mkdir -p logs
    
    print_message "目录创建完成"
}

# 生成配置文件
generate_configs() {
    print_message "生成配置文件..."
    
    # Redis配置
    cat > config/redis.conf << EOF
# Redis配置文件
bind 0.0.0.0
port 6379
timeout 0
tcp-keepalive 300
daemonize no
supervised no
pidfile /var/run/redis_6379.pid
loglevel notice
logfile ""
databases 16
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb
dir ./
maxmemory 256mb
maxmemory-policy allkeys-lru
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
EOF

    # Prometheus配置
    cat > config/prometheus.yml << EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
    scrape_interval: 5s

  - job_name: 'redis'
    static_configs:
      - targets: ['redis:6379']

  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['rabbitmq:15692']
EOF

    # Grafana数据源配置
    mkdir -p config/grafana/datasources
    cat > config/grafana/datasources/prometheus.yml << EOF
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
EOF

    # Logstash配置
    cat > config/logstash/logstash.yml << EOF
http.host: "0.0.0.0"
xpack.monitoring.elasticsearch.hosts: [ "http://elasticsearch:9200" ]
EOF

    cat > config/logstash/pipeline/logstash.conf << EOF
input {
  beats {
    port => 5044
  }
}

filter {
  if [fields][service] == "spring-boot" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} %{GREEDYDATA:message}" }
    }
    date {
      match => [ "timestamp", "yyyy-MM-dd HH:mm:ss.SSS" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "spring-boot-logs-%{+YYYY.MM.dd}"
  }
}
EOF

    print_message "配置文件生成完成"
}

# 启动开发环境
start_dev_environment() {
    print_message "启动开发环境..."
    
    # 启动基础服务
    docker-compose up -d oracle redis rabbitmq minio
    
    print_message "等待基础服务启动..."
    sleep 30
    
    # 启动监控服务
    docker-compose up -d prometheus grafana jaeger
    
    print_message "等待监控服务启动..."
    sleep 20
    
    # 启动日志服务
    docker-compose up -d elasticsearch kibana logstash
    
    print_message "等待日志服务启动..."
    sleep 30
    
    # 启动配置中心
    docker-compose up -d apollo-mysql apollo-configservice apollo-adminservice apollo-portal
    
    print_message "开发环境启动完成！"
}

# 显示服务状态
show_status() {
    print_message "服务状态："
    docker-compose ps
    
    echo ""
    print_message "服务访问地址："
    echo -e "${BLUE}Oracle数据库:${NC} localhost:1521 (system/framework123)"
    echo -e "${BLUE}Redis:${NC} localhost:6379"
    echo -e "${BLUE}RabbitMQ管理界面:${NC} http://localhost:15672 (guest/guest)"
    echo -e "${BLUE}MinIO控制台:${NC} http://localhost:9001 (minioadmin/minioadmin123)"
    echo -e "${BLUE}Prometheus:${NC} http://localhost:9090"
    echo -e "${BLUE}Grafana:${NC} http://localhost:3000 (admin/admin123)"
    echo -e "${BLUE}Jaeger链路追踪:${NC} http://localhost:16686"
    echo -e "${BLUE}Elasticsearch:${NC} http://localhost:9200"
    echo -e "${BLUE}Kibana:${NC} http://localhost:5601"
    echo -e "${BLUE}Apollo配置中心:${NC} http://localhost:8080"
    echo -e "${BLUE}Apollo管理服务:${NC} http://localhost:8090"
    echo -e "${BLUE}Apollo门户:${NC} http://localhost:8070 (apollo/admin)"
}

# 主函数
main() {
    print_header
    
    check_docker
    check_ports
    create_directories
    generate_configs
    start_dev_environment
    show_status
    
    print_message "开发环境启动完成！"
    print_message "可以使用以下命令查看日志："
    echo "  docker-compose logs -f [服务名]"
    print_message "可以使用以下命令停止服务："
    echo "  docker-compose down"
}

# 执行主函数
main "$@"
