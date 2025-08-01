#!/bin/bash

# 部署Guanwei Framework到本地仓库脚本
# 使用方法: ./deploy-to-local-repo.sh [version]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Maven是否安装
check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_error "Maven未安装，请先安装Maven"
        exit 1
    fi
    print_info "Maven版本: $(mvn -version | head -n 1)"
}

# 检查Java版本
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java未安装，请先安装Java 17"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java版本过低，需要Java 17或更高版本"
        exit 1
    fi
    print_info "Java版本: $(java -version 2>&1 | head -n 1)"
}

# 清理项目
clean_project() {
    print_info "清理项目..."
    mvn clean
}

# 编译项目
compile_project() {
    print_info "编译项目..."
    mvn compile -DskipTests
}

# 运行测试
run_tests() {
    print_info "运行测试..."
    mvn test
}

# 打包项目
package_project() {
    print_info "打包项目..."
    mvn package -DskipTests
}

# 安装到本地仓库
install_to_local() {
    print_info "安装到本地Maven仓库..."
    mvn install -DskipTests
}

# 部署到远程仓库
deploy_to_remote() {
    print_info "部署到远程仓库..."
    mvn deploy -DskipTests
}

# 生成文档
generate_docs() {
    print_info "生成API文档..."
    mvn javadoc:javadoc -DskipTests
}

# 主函数
main() {
    print_info "开始部署Guanwei Framework..."
    
    # 检查环境
    check_maven
    check_java
    
    # 获取版本号
    VERSION=${1:-"1.0.0"}
    print_info "部署版本: $VERSION"
    
    # 执行部署步骤
    clean_project
    compile_project
    run_tests
    package_project
    install_to_local
    
    # 如果提供了远程仓库配置，则部署到远程
    if [ -f "pom.xml" ] && grep -q "distributionManagement" pom.xml; then
        print_warn "检测到远程仓库配置，是否部署到远程仓库？(y/N)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            deploy_to_remote
        fi
    fi
    
    generate_docs
    
    print_info "部署完成！"
    print_info "版本: $VERSION"
    print_info "本地仓库路径: ~/.m2/repository/com/guanwei/"
}

# 执行主函数
main "$@" 