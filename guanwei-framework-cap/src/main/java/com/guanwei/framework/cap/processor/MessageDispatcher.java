package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;

import java.util.concurrent.CompletableFuture;

/**
 * CAP 消息分发器接口
 * 参考 .NET Core CAP 的 IDispatcher 接口
 * 负责消息的分发和调度
 */
public interface MessageDispatcher {

    /**
     * 启动分发器
     *
     * @return 启动结果
     */
    CompletableFuture<Void> start();

    /**
     * 停止分发器
     *
     * @return 停止结果
     */
    CompletableFuture<Void> stop();

    /**
     * 将消息加入发布队列
     *
     * @param message 消息
     * @return 操作结果
     */
    CompletableFuture<Void> enqueueToPublish(CapMessage message);

    /**
     * 将消息加入执行队列
     *
     * @param message 消息
     * @return 操作结果
     */
    CompletableFuture<Void> enqueueToExecute(CapMessage message);

    /**
     * 将消息加入调度队列
     *
     * @param message     消息
     * @param publishTime 发布时间
     * @param transaction 事务对象（可选）
     * @return 操作结果
     */
    CompletableFuture<Void> enqueueToScheduler(CapMessage message, java.time.LocalDateTime publishTime, Object transaction);
} 