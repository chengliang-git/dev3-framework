package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;

import java.util.concurrent.CompletableFuture;

/**
 * CAP 订阅调用器接口
 * 参考 .NET Core CAP 的 ISubscribeInvoker 接口
 * 负责调用订阅者的方法
 */
public interface SubscribeInvoker {

    /**
     * 异步调用订阅者方法
     *
     * @param message     消息
     * @param descriptor  消费者执行描述符
     * @return 调用结果
     */
    Object invokeAsync(CapMessage message, ConsumerExecutorDescriptor descriptor);

    /**
     * 异步调用订阅者方法（带取消令牌）
     *
     * @param message     消息
     * @param descriptor  消费者执行描述符
     * @param cancellationToken 取消令牌
     * @return 调用结果
     */
    CompletableFuture<Object> invokeAsync(CapMessage message, ConsumerExecutorDescriptor descriptor, Object cancellationToken);
} 