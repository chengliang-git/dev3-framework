package com.guanwei.framework.common.annotation;

import java.lang.annotation.*;

/**
 * API版本注解
 * 用于控制器和方法级别的API版本控制
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * API版本号
     */
    String value();

    /**
     * 版本描述
     */
    String description() default "";

    /**
     * 是否废弃
     */
    boolean deprecated() default false;

    /**
     * 废弃说明
     */
    String deprecationReason() default "";

    /**
     * 版本兼容性
     */
    Compatibility compatibility() default Compatibility.BACKWARD_COMPATIBLE;

    /**
     * 兼容性枚举
     */
    enum Compatibility {
        /**
         * 向后兼容
         */
        BACKWARD_COMPATIBLE,
        /**
         * 向前兼容
         */
        FORWARD_COMPATIBLE,
        /**
         * 双向兼容
         */
        BIDIRECTIONAL_COMPATIBLE,
        /**
         * 不兼容
         */
        INCOMPATIBLE
    }
} 