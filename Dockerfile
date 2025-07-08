# 使用官方OpenJDK 17作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Maven构建的jar文件到容器中
COPY target/spring-boot-framework-1.0.0.jar app.jar

# 创建logs目录
RUN mkdir -p /app/logs

# 设置环境变量
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=prod

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar"]