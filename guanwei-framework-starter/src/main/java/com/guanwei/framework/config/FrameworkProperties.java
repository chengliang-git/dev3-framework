package com.guanwei.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 框架统一配置管理类
 * 集中管理所有配置属性
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "framework")
public class FrameworkProperties {

    /**
     * 安全配置
     */
    private Security security = new Security();

    /**
     * 代码生成器配置
     */
    private Generator generator = new Generator();

    /**
     * 文件上传配置
     */
    private Upload upload = new Upload();

    /**
     * 缓存配置
     */
    private Cache cache = new Cache();

    /**
     * 日志配置
     */
    private Log log = new Log();

    /**
     * 安全配置
     */
    @Data
    public static class Security {
        /**
         * 不需要认证的路径
         */
        private List<String> permitAllPaths = List.of(
                "/auth/login",
                "/auth/register",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/actuator/**"
        );

        /**
         * 跨域配置
         */
        private Cors cors = new Cors();

        /**
         * JWT配置
         */
        private Jwt jwt = new Jwt();
    }

    /**
     * 跨域配置
     */
    @Data
    public static class Cors {
        // 更安全的默认：不放开，需通过配置白名单注入
        private List<String> allowedOrigins = List.of();
        private List<String> allowedMethods = List.of("GET", "POST");
        private List<String> allowedHeaders = List.of("Authorization", "Content-Type");
        private boolean allowCredentials = false;
        private long maxAge = 600L;
    }

    /**
     * JWT配置
     */
    @Data
    public static class Jwt {
        private String secret = "enterprise-framework-jwt-secret-key-2024";
        private long expiration = 86400000L; // 24小时
        private String header = "Authorization";
        private String prefix = "Bearer ";
    }

    /**
     * 代码生成器配置
     */
    @Data
    public static class Generator {
        private String author = "Guanwei Framework";
        private String packageName = "com.guanwei.framework";
        private String tablePrefix = "t_";
        private List<String> excludeColumns = List.of("id", "createTime", "modifyTime", "creator", "modifier", "delFlag", "orderNo");
        private String outputDir = System.getProperty("user.dir") + "/src/main/java";
        private boolean enableSwagger = true;
        private boolean enableLombok = true;
    }

    /**
     * 文件上传配置
     */
    @Data
    public static class Upload {
        private String path = "./uploads";
        private String maxFileSize = "10MB";
        private String maxRequestSize = "100MB";
        private List<String> allowedExtensions = List.of(
                "jpg", "jpeg", "png", "gif", "bmp", "pdf", 
                "doc", "docx", "xls", "xlsx", "ppt", "pptx", 
                "txt", "zip", "rar"
        );
    }

    /**
     * 缓存配置
     */
    @Data
    public static class Cache {
        private String type = "redis"; // redis, caffeine, memory
        private long defaultTtl = 3600L; // 默认过期时间（秒）
        private int maxSize = 1000; // 最大缓存条目数
    }

    /**
     * 日志配置
     */
    @Data
    public static class Log {
        private String level = "INFO";
        private String pattern = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n";
        private String file = "logs/enterprise-framework.log";
        private long maxFileSize = 10 * 1024 * 1024L; // 10MB
        private int maxHistory = 30;
        private boolean enableConsole = true;
        private boolean enableFile = true;
    }
} 