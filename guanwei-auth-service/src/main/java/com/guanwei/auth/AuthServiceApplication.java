package com.guanwei.auth;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * 认证授权服务启动类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.guanwei.auth.mapper")
public class AuthServiceApplication implements ApplicationRunner {

    private final Environment environment;

    public AuthServiceApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 获取服务器端口和上下文路径
        String port = environment.getProperty("server.port", "8084");
        String contextPath = environment.getProperty("server.servlet.context-path", "/auth");

        // 构建基础URL
        String baseUrl = "http://localhost:" + port + contextPath;

        // 输出服务信息
        log.info("==================================================");
        log.info("🔐 认证授权服务启动成功!");
        log.info("📖 API文档地址: {}/doc.html", baseUrl);
        log.info("📋 OpenAPI JSON: {}/v3/api-docs", baseUrl);
        log.info("🔍 健康检查: {}/actuator/health", baseUrl);
        log.info("🔐 认证相关:");
        log.info("   - JWT登录: {}/api/auth/login", baseUrl);
        log.info("   - OAuth2授权: {}/oauth2/authorize", baseUrl);
        log.info("   - OAuth2令牌: {}/oauth2/token", baseUrl);
        log.info("   - 用户注册: {}/api/auth/register", baseUrl);
        log.info("   - 默认用户: admin/admin123");
        log.info("==================================================");

        // 同时输出到控制台
        System.out.println("==================================================");
        System.out.println("🔐 认证授权服务启动成功!");
        System.out.println("📖 API文档地址: " + baseUrl + "/doc.html");
        System.out.println("📋 OpenAPI JSON: " + baseUrl + "/v3/api-docs");
        System.out.println("🔍 健康检查: " + baseUrl + "/actuator/health");
        System.out.println("🔐 认证相关:");
        System.out.println("   - JWT登录: " + baseUrl + "/api/auth/login");
        System.out.println("   - OAuth2授权: " + baseUrl + "/oauth2/authorize");
        System.out.println("   - OAuth2令牌: " + baseUrl + "/oauth2/token");
        System.out.println("   - 用户注册: " + baseUrl + "/api/auth/register");
        System.out.println("   - 默认用户: admin/admin123");
        System.out.println("==================================================");
    }
}
