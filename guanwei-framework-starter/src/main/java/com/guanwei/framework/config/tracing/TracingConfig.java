package com.guanwei.framework.config.tracing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * 链路追踪配置
 * 提供分布式链路追踪支持
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.monitor", name = "enableTracing", havingValue = "true")
public class TracingConfig {

    /**
     * 链路追踪上下文
     */
    @Bean
    public TracingContext tracingContext() {
        log.info("Tracing context initialized");
        return new TracingContext();
    }

    /**
     * 链路追踪拦截器
     */
    @Bean
    public TracingInterceptor tracingInterceptor() {
        log.info("Tracing interceptor initialized");
        return new TracingInterceptor();
    }
}

/**
 * 链路追踪上下文
 */
class TracingContext {
    
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> SPAN_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> PARENT_SPAN_ID = new ThreadLocal<>();
    
    /**
     * 设置追踪ID
     */
    public void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }
    
    /**
     * 获取追踪ID
     */
    public String getTraceId() {
        String traceId = TRACE_ID.get();
        if (traceId == null) {
            traceId = generateTraceId();
            setTraceId(traceId);
        }
        return traceId;
    }
    
    /**
     * 设置Span ID
     */
    public void setSpanId(String spanId) {
        SPAN_ID.set(spanId);
    }
    
    /**
     * 获取Span ID
     */
    public String getSpanId() {
        String spanId = SPAN_ID.get();
        if (spanId == null) {
            spanId = generateSpanId();
            setSpanId(spanId);
        }
        return spanId;
    }
    
    /**
     * 设置父Span ID
     */
    public void setParentSpanId(String parentSpanId) {
        PARENT_SPAN_ID.set(parentSpanId);
    }
    
    /**
     * 获取父Span ID
     */
    public String getParentSpanId() {
        return PARENT_SPAN_ID.get();
    }
    
    /**
     * 清除上下文
     */
    public void clear() {
        TRACE_ID.remove();
        SPAN_ID.remove();
        PARENT_SPAN_ID.remove();
    }
    
    /**
     * 生成追踪ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成Span ID
     */
    private String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}

/**
 * 链路追踪拦截器
 */
class TracingInterceptor {
    
    /**
     * 前置处理
     */
    public void preHandle(String operation) {
        // 这里可以添加链路追踪的前置处理逻辑
        // 例如记录开始时间、设置追踪信息等
    }
    
    /**
     * 后置处理
     */
    public void postHandle(String operation, long startTime) {
        // 这里可以添加链路追踪的后置处理逻辑
        // 例如记录执行时间、设置追踪信息等
        long duration = System.currentTimeMillis() - startTime;
        // 可以记录到日志或发送到追踪系统
    }
    
    /**
     * 异常处理
     */
    public void afterThrowing(String operation, Exception exception) {
        // 这里可以添加链路追踪的异常处理逻辑
        // 例如记录异常信息、设置错误状态等
    }
} 