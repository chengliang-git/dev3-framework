package com.guanwei.framework.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务注解
 * 用于标记异步执行的方法
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncTask {

    /**
     * 任务名称
     */
    String name() default "";

    /**
     * 任务描述
     */
    String description() default "";

    /**
     * 执行器名称
     */
    String executor() default "taskExecutor";

    /**
     * 超时时间
     */
    long timeout() default 0L;

    /**
     * 超时时间单位
     */
    TimeUnit timeoutUnit() default TimeUnit.SECONDS;

    /**
     * 是否启用重试
     */
    boolean enableRetry() default false;

    /**
     * 最大重试次数
     */
    int maxRetries() default 3;

    /**
     * 重试间隔（毫秒）
     */
    long retryDelay() default 1000L;

    /**
     * 任务优先级
     */
    TaskPriority priority() default TaskPriority.NORMAL;

    /**
     * 任务类型
     */
    TaskType type() default TaskType.COMPUTATION;

    /**
     * 任务优先级枚举
     */
    enum TaskPriority {
        /**
         * 高优先级
         */
        HIGH(1),
        /**
         * 普通优先级
         */
        NORMAL(5),
        /**
         * 低优先级
         */
        LOW(10);

        private final int value;

        TaskPriority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 任务类型枚举
     */
    enum TaskType {
        /**
         * 计算密集型
         */
        COMPUTATION,
        /**
         * IO密集型
         */
        IO,
        /**
         * 混合型
         */
        MIXED
    }
} 