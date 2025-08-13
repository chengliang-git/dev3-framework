package com.guanwei.framework.config;

import com.guanwei.framework.config.async.AsyncConfig;
import com.guanwei.framework.config.cache.CacheConfig;
import com.guanwei.framework.config.monitor.MonitorConfig;
import com.guanwei.framework.config.validation.ValidationConfig;
import com.guanwei.framework.config.i18n.I18nConfig;
import com.guanwei.framework.config.version.ApiVersionConfig;
import com.guanwei.framework.config.tracing.TracingConfig;
import com.guanwei.framework.config.ratelimit.RateLimitConfig;
import com.guanwei.framework.config.resilience.ResilienceConfig;
import com.guanwei.framework.config.file.FileManagementConfig;
import com.guanwei.framework.config.lock.DistributedLockConfig;
import com.guanwei.framework.config.security.UnifiedSecurityConfig;
import com.guanwei.framework.config.audit.AuditConfig;
import com.guanwei.framework.config.gateway.ApiGatewayConfig;
import com.guanwei.framework.config.discovery.ServiceDiscoveryConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 框架自动配置类
 * 启用框架相关的配置属性和功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(FrameworkProperties.class)
@EnableAsync
@EnableScheduling
@Import({
    AsyncConfig.class,
    CacheConfig.class,
    MonitorConfig.class,
    ValidationConfig.class,
    I18nConfig.class,
    ApiVersionConfig.class,
    TracingConfig.class,
    RateLimitConfig.class,
    ResilienceConfig.class,
    FileManagementConfig.class,
    DistributedLockConfig.class,
    UnifiedSecurityConfig.class,
    AuditConfig.class,
    ApiGatewayConfig.class,
    ServiceDiscoveryConfig.class
})
public class FrameworkAutoConfiguration {

    /**
     * 框架配置属性
     */
    @Bean
    @ConditionalOnMissingBean
    public FrameworkProperties frameworkProperties() {
        return new FrameworkProperties();
    }
} 