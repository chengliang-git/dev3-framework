package com.guanwei.framework.config.resilience;

import com.guanwei.framework.config.FrameworkProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 熔断器配置
 * 提供基于Resilience4j的熔断器、限流、重试和超时功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.circuit-breaker", name = "enabled", havingValue = "true")
public class ResilienceConfig {

    private final FrameworkProperties frameworkProperties;

    public ResilienceConfig(FrameworkProperties frameworkProperties) {
        this.frameworkProperties = frameworkProperties;
    }

    /**
     * 熔断器注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(frameworkProperties.getCircuitBreaker().getFailureRateThreshold())
                .minimumNumberOfCalls(frameworkProperties.getCircuitBreaker().getMinimumNumberOfCalls())
                .slidingWindowSize(frameworkProperties.getCircuitBreaker().getSlidingWindowSize())
                .waitDurationInOpenState(Duration.ofMillis(frameworkProperties.getCircuitBreaker().getWaitDurationInOpenState()))
                .permittedNumberOfCallsInHalfOpenState(frameworkProperties.getCircuitBreaker().getPermittedNumberOfCallsInHalfOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(frameworkProperties.getCircuitBreaker().isAutomaticTransitionFromOpenToHalfOpenEnabled())
                .recordExceptions(Exception.class)
                .ignoreExceptions()
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        log.info("Circuit breaker registry initialized with failure rate threshold: {}%", 
                frameworkProperties.getCircuitBreaker().getFailureRateThreshold());
        return registry;
    }

    /**
     * 限流器注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        log.info("Rate limiter registry initialized");
        return registry;
    }

    /**
     * 重试注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .retryExceptions(Exception.class)
                .ignoreExceptions()
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        log.info("Retry registry initialized with max attempts: 3");
        return registry;
    }

    /**
     * 超时限制器注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);
        log.info("Time limiter registry initialized with timeout: 30s");
        return registry;
    }

    /**
     * 熔断器管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerManager circuitBreakerManager(CircuitBreakerRegistry registry) {
        log.info("Circuit breaker manager initialized");
        return new CircuitBreakerManager(registry);
    }

    /**
     * 限流器管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiterManager rateLimiterManager(RateLimiterRegistry registry) {
        log.info("Rate limiter manager initialized");
        return new RateLimiterManager(registry);
    }

    /**
     * 重试管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryManager retryManager(RetryRegistry registry) {
        log.info("Retry manager initialized");
        return new RetryManager(registry);
    }

    /**
     * 超时限制器管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public TimeLimiterManager timeLimiterManager(TimeLimiterRegistry registry) {
        log.info("Time limiter manager initialized");
        return new TimeLimiterManager(registry);
    }
}

/**
 * 熔断器管理器
 */
class CircuitBreakerManager {

    private final CircuitBreakerRegistry registry;

    public CircuitBreakerManager(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    /**
     * 获取熔断器
     */
    public CircuitBreaker getCircuitBreaker(String name) {
        return registry.circuitBreaker(name);
    }

    /**
     * 执行受保护的操作
     */
    public <T> T execute(String name, java.util.function.Supplier<T> supplier) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(name);
        return circuitBreaker.executeSupplier(supplier);
    }

    /**
     * 获取熔断器状态
     */
    public CircuitBreaker.State getState(String name) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(name);
        return circuitBreaker.getState();
    }
}

/**
 * 限流器管理器
 */
class RateLimiterManager {

    private final RateLimiterRegistry registry;

    public RateLimiterManager(RateLimiterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 获取限流器
     */
    public RateLimiter getRateLimiter(String name) {
        return registry.rateLimiter(name);
    }

    /**
     * 检查是否允许请求
     */
    public boolean isAllowed(String name) {
        RateLimiter rateLimiter = getRateLimiter(name);
        return rateLimiter.acquirePermission();
    }
}

/**
 * 重试管理器
 */
class RetryManager {

    private final RetryRegistry registry;

    public RetryManager(RetryRegistry registry) {
        this.registry = registry;
    }

    /**
     * 获取重试器
     */
    public Retry getRetry(String name) {
        return registry.retry(name);
    }

    /**
     * 执行重试操作
     */
    public <T> T execute(String name, java.util.function.Supplier<T> supplier) {
        Retry retry = getRetry(name);
        return retry.executeSupplier(supplier);
    }
}

/**
 * 超时限制器管理器
 */
class TimeLimiterManager {

    private final TimeLimiterRegistry registry;

    public TimeLimiterManager(TimeLimiterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 获取超时限制器
     */
    public TimeLimiter getTimeLimiter(String name) {
        return registry.timeLimiter(name);
    }

    /**
     * 执行超时操作
     */
    public <T> T execute(String name, java.util.concurrent.CompletableFuture<T> future) {
        TimeLimiter timeLimiter = getTimeLimiter(name);
        try {
            // 使用正确的API签名
            return timeLimiter.executeCompletionStage(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor(),
                () -> future
            ).toCompletableFuture().join();
        } catch (Exception e) {
            // 如果API不可用，回退到直接执行
            System.out.println("TimeLimiter executeCompletionStage not available, falling back to direct execution: " + e.getMessage());
            return future.join();
        }
    }
}
