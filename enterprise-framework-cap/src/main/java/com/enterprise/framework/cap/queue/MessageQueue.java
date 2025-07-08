package com.enterprise.framework.cap.queue;

import com.enterprise.framework.cap.CapMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 消息队列接口
 * 负责消息的入队、出队和队列管理
 */
public interface MessageQueue {

    /**
     * 发送消息到队列
     * 
     * @param queueName 队列名称
     * @param message   消息对象
     * @return 是否发送成功
     */
    boolean send(String queueName, CapMessage message);

    /**
     * 异步发送消息到队列
     * 
     * @param queueName 队列名称
     * @param message   消息对象
     * @return CompletableFuture<Boolean> 发送结果
     */
    CompletableFuture<Boolean> sendAsync(String queueName, CapMessage message);

    /**
     * 发送延迟消息到队列
     * 
     * @param queueName    队列名称
     * @param message      消息对象
     * @param delaySeconds 延迟秒数
     * @return 是否发送成功
     */
    boolean sendDelay(String queueName, CapMessage message, long delaySeconds);

    /**
     * 从队列接收消息
     * 
     * @param queueName 队列名称
     * @param timeout   超时时间（毫秒）
     * @return 消息对象，如果没有消息则返回null
     */
    CapMessage receive(String queueName, long timeout);

    /**
     * 批量接收消息
     * 
     * @param queueName 队列名称
     * @param maxCount  最大接收数量
     * @param timeout   超时时间（毫秒）
     * @return 消息列表
     */
    List<CapMessage> receiveBatch(String queueName, int maxCount, long timeout);

    /**
     * 确认消息已处理
     * 
     * @param queueName 队列名称
     * @param messageId 消息ID
     * @return 是否确认成功
     */
    boolean acknowledge(String queueName, String messageId);

    /**
     * 拒绝消息（重新入队）
     * 
     * @param queueName 队列名称
     * @param messageId 消息ID
     * @param requeue   是否重新入队
     * @return 是否拒绝成功
     */
    boolean reject(String queueName, String messageId, boolean requeue);

    /**
     * 获取队列长度
     * 
     * @param queueName 队列名称
     * @return 队列长度
     */
    long getQueueLength(String queueName);

    /**
     * 清空队列
     * 
     * @param queueName 队列名称
     * @return 是否清空成功
     */
    boolean clearQueue(String queueName);

    /**
     * 删除队列
     * 
     * @param queueName 队列名称
     * @return 是否删除成功
     */
    boolean deleteQueue(String queueName);

    /**
     * 检查队列是否存在
     * 
     * @param queueName 队列名称
     * @return 是否存在
     */
    boolean queueExists(String queueName);
}