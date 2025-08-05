package com.guanwei.framework.cap.storage;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapMessageStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * CAP 消息存储接口
 * 参考 .NET Core CAP 的 IDataStorage 接口
 */
public interface MessageStorage {

    /**
     * 获取锁
     *
     * @param key      锁键
     * @param ttl      锁过期时间
     * @param instance 实例标识
     * @return 是否获取成功
     */
    CompletableFuture<Boolean> acquireLockAsync(String key, Duration ttl, String instance);

    /**
     * 释放锁
     *
     * @param key      锁键
     * @param instance 实例标识
     * @return 操作结果
     */
    CompletableFuture<Void> releaseLockAsync(String key, String instance);

    /**
     * 续期锁
     *
     * @param key      锁键
     * @param ttl      锁过期时间
     * @param instance 实例标识
     * @return 操作结果
     */
    CompletableFuture<Void> renewLockAsync(String key, Duration ttl, String instance);

    /**
     * 将发布消息状态改为延迟
     *
     * @param ids 消息ID列表
     * @return 操作结果
     */
    CompletableFuture<Void> changePublishStateToDelayedAsync(List<String> ids);

    /**
     * 更改发布消息状态
     *
     * @param message 消息
     * @param status  状态
     * @param transaction 事务对象（可选）
     * @return 操作结果
     */
    CompletableFuture<Void> changePublishStateAsync(CapMessage message, CapMessageStatus status, Object transaction);

    /**
     * 更改接收消息状态
     *
     * @param message 消息
     * @param status  状态
     * @return 操作结果
     */
    CompletableFuture<Void> changeReceiveStateAsync(CapMessage message, CapMessageStatus status);

    /**
     * 存储消息
     *
     * @param name      消息名称
     * @param content   消息内容
     * @param transaction 事务对象（可选）
     * @return 存储的消息
     */
    CompletableFuture<CapMessage> storeMessageAsync(String name, Object content, Object transaction);

    /**
     * 存储接收异常消息
     *
     * @param name    消息名称
     * @param group   消息组
     * @param content 异常内容
     * @return 操作结果
     */
    CompletableFuture<Void> storeReceivedExceptionMessageAsync(String name, String group, String content);

    /**
     * 存储接收消息
     *
     * @param name    消息名称
     * @param group   消息组
     * @param content 消息内容
     * @return 存储的消息
     */
    CompletableFuture<CapMessage> storeReceivedMessageAsync(String name, String group, Object content);

    /**
     * 删除过期消息
     *
     * @param table      表名
     * @param timeout    过期时间
     * @param batchCount 批处理大小
     * @return 删除的消息数量
     */
    CompletableFuture<Integer> deleteExpiresAsync(String table, LocalDateTime timeout, int batchCount);

    /**
     * 获取需要重试的发布消息
     *
     * @param lookbackSeconds 回溯时间（秒）
     * @return 需要重试的消息列表
     */
    CompletableFuture<List<CapMessage>> getPublishedMessagesOfNeedRetry(Duration lookbackSeconds);

    /**
     * 获取需要重试的接收消息
     *
     * @param lookbackSeconds 回溯时间（秒）
     * @return 需要重试的消息列表
     */
    CompletableFuture<List<CapMessage>> getReceivedMessagesOfNeedRetry(Duration lookbackSeconds);

    /**
     * 删除接收消息
     *
     * @param id 消息ID
     * @return 删除的消息数量
     */
    CompletableFuture<Integer> deleteReceivedMessageAsync(String id);

    /**
     * 删除发布消息
     *
     * @param id 消息ID
     * @return 删除的消息数量
     */
    CompletableFuture<Integer> deletePublishedMessageAsync(String id);

    /**
     * 调度延迟消息
     *
     * @param scheduleTask 调度任务
     * @return 操作结果
     */
    CompletableFuture<Void> scheduleMessagesOfDelayedAsync(DelayedMessageScheduler scheduleTask);

    /**
     * 延迟消息调度器接口
     */
    @FunctionalInterface
    interface DelayedMessageScheduler {
        /**
         * 执行调度任务
         *
         * @param transaction 事务对象
         * @param messages    消息列表
         * @return 操作结果
         */
        CompletableFuture<Void> schedule(Object transaction, List<CapMessage> messages);
    }
}