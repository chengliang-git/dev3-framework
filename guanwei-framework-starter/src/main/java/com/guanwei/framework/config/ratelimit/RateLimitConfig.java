package com.guanwei.framework.config.ratelimit;

import com.guanwei.framework.config.FrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流配置
 * 提供基于Bucket4j的限流功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.rate-limit", name = "enabled", havingValue = "true")
public class RateLimitConfig {

    private final FrameworkProperties frameworkProperties;
    private final ConcurrentHashMap<String, AtomicInteger> localCounters = new ConcurrentHashMap<>();

    public RateLimitConfig(FrameworkProperties frameworkProperties) {
        this.frameworkProperties = frameworkProperties;
    }

    /**
     * 限流管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitManager rateLimitManager() {
        log.info("Rate limit manager initialized");
        return new RateLimitManager(frameworkProperties, localCounters);
    }

    /**
     * 限流拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitInterceptor rateLimitInterceptor(RateLimitManager rateLimitManager) {
        log.info("Rate limit interceptor initialized");
        return new RateLimitInterceptor(rateLimitManager);
    }
}

/**
 * 限流管理器
 */
class RateLimitManager {

    private final FrameworkProperties.RateLimit config;
    private final ConcurrentHashMap<String, AtomicInteger> localCounters;

    public RateLimitManager(FrameworkProperties frameworkProperties, ConcurrentHashMap<String, AtomicInteger> localCounters) {
        this.config = frameworkProperties.getRateLimit();
        this.localCounters = localCounters;
    }

    /**
     * 检查是否允许请求
     */
    public boolean isAllowed(String key) {
        AtomicInteger counter = localCounters.computeIfAbsent(key, k -> new AtomicInteger(0));
        int current = counter.get();
        if (current < config.getDefaultCapacity()) {
            counter.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * 重置计数器
     */
    public void resetCounter(String key) {
        localCounters.remove(key);
    }

    /**
     * 尝试消费令牌
     */
    public boolean tryConsume(String key, int tokens) {
        AtomicInteger counter = localCounters.computeIfAbsent(key, k -> new AtomicInteger(0));
        int current = counter.get();
        if (current + tokens <= config.getDefaultCapacity()) {
            counter.addAndGet(tokens);
            return true;
        }
        return false;
    }

    /**
     * 获取剩余令牌数
     */
    public long getAvailableTokens(String key) {
        AtomicInteger counter = localCounters.get(key);
        if (counter == null) {
            return config.getDefaultCapacity();
        }
        return Math.max(0, config.getDefaultCapacity() - counter.get());
    }
}

/**
 * 限流拦截器
 */
class RateLimitInterceptor {

    private final RateLimitManager rateLimitManager;

    public RateLimitInterceptor(RateLimitManager rateLimitManager) {
        this.rateLimitManager = rateLimitManager;
    }

    /**
     * 前置处理
     */
    public boolean preHandle(String key) {
        return rateLimitManager.isAllowed(key);
    }

    /**
     * 获取限流信息
     */
    public RateLimitInfo getRateLimitInfo(String key) {
        return new RateLimitInfo(
                key,
                rateLimitManager.getAvailableTokens(key),
                System.currentTimeMillis()
        );
    }
}

/**
 * 限流信息
 */
class RateLimitInfo {
    private final String key;
    private final long availableTokens;
    private final long timestamp;

    public RateLimitInfo(String key, long availableTokens, long timestamp) {
        this.key = key;
        this.availableTokens = availableTokens;
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public long getAvailableTokens() {
        return availableTokens;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
