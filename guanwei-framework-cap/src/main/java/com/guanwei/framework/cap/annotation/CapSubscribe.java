package com.guanwei.framework.cap.annotation;

import java.lang.annotation.*;

/**
 * CAP 订阅注解
 * 用于标记订阅方法，参考 .NET Core CAP 组件的 CapSubscribeAttribute
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CapSubscribe {

    /**
     * 消息名称/主题
     */
    String value() default "";

    /**
     * 消息组
     */
    String group() default "";

    /**
     * 是否启用
     */
    boolean enabled() default true;

    /**
     * 最大重试次数
     */
    int maxRetries() default 3;

    /**
     * 重试间隔（秒）
     */
    long retryInterval() default 60;

    /**
     * 是否异步处理
     */
    boolean async() default false;

    /**
     * 处理器名称
     */
    String handlerName() default "";

    /**
     * 消息类型
     */
    MessageType messageType() default MessageType.NORMAL;

    /**
     * 消息类型枚举
     */
    enum MessageType {
        NORMAL, // 普通消息
        DELAY, // 延迟消息
        TRANSACTIONAL // 事务性消息
    }
}