package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;

import java.util.concurrent.CompletableFuture;

/**
 * CAP 消息发送器接口
 * 参考 .NET Core CAP 的 IMessageSender 接口
 * 负责将消息发送到消息队列
 */
public interface MessageSender {

    /**
     * 异步发送消息
     *
     * @param message 消息
     * @return 发送结果
     */
    CompletableFuture<OperateResult> sendAsync(CapMessage message);

    /**
     * 异步发送消息（带超时）
     *
     * @param message 消息
     * @param timeout 超时时间（毫秒）
     * @return 发送结果
     */
    CompletableFuture<OperateResult> sendAsync(CapMessage message, long timeout);
} 