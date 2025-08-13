package com.guanwei.framework.config.monitor;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 监控配置
 * 提供健康检查、指标监控和链路追踪支持
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
public class MonitorConfig {

    /**
     * 定时器切面，用于方法执行时间监控
     */
    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnMissingBean
    public TimedAspect timedAspect(MeterRegistry registry) {
        log.info("Timed aspect initialized for metrics collection");
        return new TimedAspect(registry);
    }

    /**
     * 系统健康检查指示器
     * 暂时注释掉，等待Spring Boot 3.x兼容性修复
     */
    /*
    @Bean
    @ConditionalOnMissingBean(name = "systemHealthIndicator")
    public HealthIndicator systemHealthIndicator() {
        // 暂时返回null，等待修复
        return null;
    }
    */

    /**
     * 数据库健康检查指示器
     * 暂时注释掉，等待Spring Boot 3.x兼容性修复
     */
    /*
    @Bean
    @ConditionalOnMissingBean(name = "databaseHealthIndicator")
    public HealthIndicator databaseHealthIndicator() {
        // 暂时返回null，等待修复
        return null;
    }
    */

    /**
     * Redis健康检查指示器
     * 暂时注释掉，等待Spring Boot 3.x兼容性修复
     */
    /*
    @Bean
    @ConditionalOnClass(name = "org.springframework.data.redis.connection.RedisConnectionFactory")
    @ConditionalOnMissingBean(name = "redisHealthIndicator")
    public HealthIndicator redisHealthIndicator() {
        // 暂时返回null，等待修复
        return null;
    }
    */
} 