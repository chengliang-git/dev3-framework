package com.guanwei.framework.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解
 * 用于方法级别的缓存操作
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    /**
     * 缓存键，支持SpEL表达式
     */
    String key() default "";

    /**
     * 缓存键前缀
     */
    String prefix() default "";

    /**
     * 过期时间
     */
    long expire() default 3600L;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 是否缓存空值
     */
    boolean cacheNull() default false;

    /**
     * 缓存条件，支持SpEL表达式
     */
    String condition() default "";

    /**
     * 缓存类型
     */
    CacheType type() default CacheType.AUTO;

    /**
     * 缓存操作类型
     */
    CacheOperation operation() default CacheOperation.READ;

    /**
     * 缓存类型枚举
     */
    enum CacheType {
        /**
         * 自动选择（优先Redis，其次Caffeine，最后内存）
         */
        AUTO,
        /**
         * Redis缓存
         */
        REDIS,
        /**
         * Caffeine本地缓存
         */
        CAFFEINE,
        /**
         * 内存缓存
         */
        MEMORY
    }

    /**
     * 缓存操作类型枚举
     */
    enum CacheOperation {
        /**
         * 读取缓存
         */
        READ,
        /**
         * 写入缓存
         */
        WRITE,
        /**
         * 删除缓存
         */
        DELETE,
        /**
         * 更新缓存
         */
        UPDATE
    }
} 