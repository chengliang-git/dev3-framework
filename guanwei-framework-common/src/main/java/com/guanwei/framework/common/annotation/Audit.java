package com.guanwei.framework.common.annotation;

import java.lang.annotation.*;

/**
 * 审计注解
 * 用于标记需要审计的方法
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    /**
     * 操作类型
     */
    String operation() default "";

    /**
     * 资源类型
     */
    String resource() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否记录请求参数
     */
    boolean logRequest() default true;

    /**
     * 是否记录响应结果
     */
    boolean logResponse() default false;

    /**
     * 是否记录异常信息
     */
    boolean logException() default true;

    /**
     * 审计级别
     */
    AuditLevel level() default AuditLevel.INFO;

    /**
     * 是否异步记录
     */
    boolean async() default true;

    /**
     * 审计组
     */
    String group() default "default";
}

/**
 * 审计级别枚举
 */
enum AuditLevel {
    DEBUG,      // 调试级别
    INFO,       // 信息级别
    WARN,       // 警告级别
    ERROR       // 错误级别
}
