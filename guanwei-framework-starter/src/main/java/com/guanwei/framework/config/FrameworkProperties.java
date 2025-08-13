package com.guanwei.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

        /**
         * API安全配置
         */
        private Api api = new Api();
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
     * API安全配置
     */
    @Data
    public static class Api {
        private boolean enabled = true;
        private long timeWindow = 300L; // 时间窗口（秒）
        private int maxRequestsPerMinute = 100; // 每分钟最大请求数
        private String defaultAppSecret = "default-app-secret-key";
        private List<String> skipPaths = List.of(
                "/auth/login",
                "/auth/register",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/actuator/**"
        );
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

    /**
     * 异步任务配置
     */
    @Data
    public static class Async {
        private int corePoolSize = 10;
        private int maxPoolSize = 20;
        private int queueCapacity = 500;
        private int keepAliveSeconds = 60;
        private int schedulerPoolSize = 5;
        private boolean enableAsync = true;
        private boolean enableScheduling = true;
    }

    /**
     * 监控配置
     */
    @Data
    public static class Monitor {
        private boolean enableMetrics = true;
        private boolean enableHealthCheck = true;
        private boolean enableTracing = false;
        private String metricsEndpoint = "/actuator/metrics";
        private String healthEndpoint = "/actuator/health";
        private long metricsCollectionInterval = 60000L; // 1分钟
    }

    /**
     * 国际化配置
     */
    @Data
    public static class I18n {
        private String defaultLocale = "zh_CN";
        private String defaultTimezone = "Asia/Shanghai";
        private List<String> supportedLocales = List.of("zh_CN", "en_US");
        private boolean enableI18n = true;
        private int messageCacheSeconds = 3600;
    }

    /**
     * API版本配置
     */
    @Data
    public static class ApiVersion {
        private String defaultVersion = "v1";
        private List<String> supportedVersions = List.of("v1", "v2");
        private boolean enableVersioning = true;
        private String versionHeader = "X-API-Version";
        private String versionParam = "version";
    }

    /**
     * 限流配置
     */
    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int defaultCapacity = 100;
        private int defaultRefillTokens = 10;
        private long defaultRefillPeriod = 1000L; // 毫秒
        private String strategy = "token-bucket"; // token-bucket, leaky-bucket
        private boolean enableRedis = true;
        private String redisKeyPrefix = "rate_limit:";
        private int redisExpireSeconds = 3600;
    }

    /**
     * 熔断器配置
     */
    @Data
    public static class CircuitBreaker {
        private boolean enabled = true;
        private int failureRateThreshold = 50; // 失败率阈值百分比
        private int minimumNumberOfCalls = 10; // 最小调用次数
        private int slidingWindowSize = 100; // 滑动窗口大小
        private long waitDurationInOpenState = 60000L; // 熔断器开启状态等待时间（毫秒）
        private int permittedNumberOfCallsInHalfOpenState = 3; // 半开状态允许的调用次数
        private boolean automaticTransitionFromOpenToHalfOpenEnabled = true;
        private boolean recordExceptions = true;
        private boolean ignoreExceptions = false;
    }

    /**
     * 文件管理配置
     */
    @Data
    public static class FileManagement {
        private String storageType = "local"; // local, s3, minio, oss
        private String localPath = "./uploads";
        private String tempPath = "./temp";
        private long maxFileSize = 100 * 1024 * 1024L; // 100MB
        private long maxRequestSize = 1000 * 1024 * 1024L; // 1GB
        private List<String> allowedExtensions = List.of(
                "jpg", "jpeg", "png", "gif", "bmp", "webp",
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                "txt", "zip", "rar", "7z", "tar", "gz"
        );
        private boolean enableVirusScan = false;
        private boolean enableCompression = true;
        private String compressionQuality = "0.8";
        private boolean enableThumbnail = true;
        private int thumbnailWidth = 200;
        private int thumbnailHeight = 200;
        private boolean enableWatermark = false;
        private String watermarkText = "Enterprise Framework";
        private String watermarkPosition = "bottom-right";
        
        /**
         * S3配置
         */
        private S3 s3 = new S3();
    }
    
    /**
     * S3配置
     */
    @Data
    public static class S3 {
        private String bucketName = "test";
        private String serviceURL = "http://localhost:9000";
        private String accessKey = "";
        private String secretKey = "";
        private String region = "us-east-1";
        private Integer signatureVersion = 2; // 2或4，默认2
        private boolean encodeKey = false;
        private String httpServer = "/files";
        private String allowedFileTypes = "jpg,jpeg,png,bmp,doc,docx,xls,xlsx,pdf";
        private String urlPrefix = "";
        private boolean preSignedURL = true;
        private int preSignedExpiry = 24; // 小时
    }
    
    /**
     * 分布式锁配置
     */
    @Data
    public static class DistributedLock {
        private boolean enabled = true;
        private String lockPrefix = "lock:";
        private long defaultTimeout = 30000L; // 默认超时时间（毫秒）
        private TimeUnit defaultTimeUnit = TimeUnit.MILLISECONDS;
        private int maxRetries = 3;
        private long retryDelay = 1000L; // 重试延迟（毫秒）
        private boolean enableWatchdog = true; // 看门狗机制
        private long watchdogInterval = 10000L; // 看门狗检查间隔（毫秒）
        private boolean enableLockRenewal = true; // 锁续期
        private long lockRenewalThreshold = 10000L; // 锁续期阈值（毫秒）
    }
    
    /**
     * 审计配置
     */
    @Data
    public static class Audit {
        private boolean enabled = true;
        private String storageType = "database"; // database, mongo, elasticsearch
        private boolean enableLoginAudit = true;
        private boolean enableOperationAudit = true;
        private boolean enableDataChangeAudit = true;
        private List<String> excludeOperations = List.of("QUERY", "READ");
        private long retentionDays = 365L; // 审计日志保留天数
        private boolean enableRealTimeAudit = true;
        private String auditQueue = "audit-queue"; // 审计消息队列
    }
    
    /**
     * API网关配置
     */
    @Data
    public static class Gateway {
        private boolean enabled = false;
        private String defaultRoute = "/api/**";
        private boolean enableLoadBalancing = true;
        private boolean enableRateLimiting = true;
        private boolean enableCircuitBreaker = true;
        private boolean enableMetrics = true;
    }
    
    /**
     * 服务发现配置
     */
    @Data
    public static class ServiceDiscovery {
        private boolean enabled = false;
        private String registryType = "memory"; // memory, redis, consul, eureka
        private String registryUrl = "http://localhost:8500";
        private long heartbeatInterval = 30000L; // 心跳间隔（毫秒）
        private boolean enableAutoRegistration = true;
        private boolean enableHealthCheck = true;
        private String serviceName;
        private String serviceVersion = "1.0.0";
        private String serviceGroup = "default";
    }

    /**
     * 文件管理配置
     */
    private FileManagement fileManagement = new FileManagement();

    /**
     * 熔断器配置
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();
} 