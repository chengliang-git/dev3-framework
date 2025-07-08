package com.enterprise.framework.cap.storage;

import com.enterprise.framework.cap.CapMessage;

import java.util.List;
import java.util.Optional;

/**
 * 消息存储接口
 * 负责消息的持久化存储和查询
 */
public interface MessageStorage {

    /**
     * 存储消息
     * 
     * @param message 消息对象
     * @return 是否存储成功
     */
    boolean store(CapMessage message);

    /**
     * 批量存储消息
     * 
     * @param messages 消息列表
     * @return 存储成功的消息数量
     */
    int storeBatch(List<CapMessage> messages);

    /**
     * 根据ID获取消息
     * 
     * @param id 消息ID
     * @return 消息对象
     */
    Optional<CapMessage> getById(String id);

    /**
     * 根据名称和组获取待处理消息
     * 
     * @param name  消息名称
     * @param group 消息组
     * @param limit 限制数量
     * @return 消息列表
     */
    List<CapMessage> getPendingMessages(String name, String group, int limit);

    /**
     * 更新消息状态
     * 
     * @param id     消息ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateStatus(String id, CapMessage.MessageStatus status);

    /**
     * 增加重试次数
     * 
     * @param id 消息ID
     * @return 是否更新成功
     */
    boolean incrementRetries(String id);

    /**
     * 删除过期消息
     * 
     * @param expiredTime 过期时间
     * @return 删除的消息数量
     */
    int deleteExpiredMessages(long expiredTime);

    /**
     * 删除消息
     * 
     * @param id 消息ID
     * @return 是否删除成功
     */
    boolean delete(String id);

    /**
     * 获取失败消息数量
     * 
     * @param name  消息名称
     * @param group 消息组
     * @return 失败消息数量
     */
    long getFailedMessageCount(String name, String group);

    /**
     * 获取待处理消息数量
     * 
     * @param name  消息名称
     * @param group 消息组
     * @return 待处理消息数量
     */
    long getPendingMessageCount(String name, String group);
}