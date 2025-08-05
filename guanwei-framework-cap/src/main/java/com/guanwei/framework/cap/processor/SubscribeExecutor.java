package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;

import java.util.concurrent.CompletableFuture;

/**
 * CAP 订阅执行器接口
 * 参考 .NET Core CAP 的 ISubscribeExecutor 接口
 * 负责执行订阅者的消息处理逻辑
 */
public interface SubscribeExecutor {

    /**
     * 异步执行消息
     *
     * @param message 消息
     * @return 执行结果
     */
    CompletableFuture<OperateResult> executeAsync(CapMessage message);

    /**
     * 异步执行消息（带描述符）
     *
     * @param message     消息
     * @param descriptor  消费者执行描述符
     * @return 执行结果
     */
    CompletableFuture<OperateResult> executeAsync(CapMessage message, ConsumerExecutorDescriptor descriptor);
} 