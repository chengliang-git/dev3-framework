package com.guanwei.framework.cap;

import java.util.concurrent.CompletableFuture;

/**
 * CAP 发布者接口
 * 负责消息的发布和事务性消息处理
 */
public interface CapPublisher {

    /**
     * 发布消息
     * 
     * @param name    消息名称/主题
     * @param content 消息内容
     * @return 消息ID
     */
    String publish(String name, Object content);

    /**
     * 异步发布消息
     * 
     * @param name    消息名称/主题
     * @param content 消息内容
     * @return CompletableFuture<String> 消息ID
     */
    CompletableFuture<String> publishAsync(String name, Object content);

    /**
     * 发布消息到指定组
     * 
     * @param name    消息名称/主题
     * @param content 消息内容
     * @param group   消息组
     * @return 消息ID
     */
    String publish(String name, Object content, String group);

    /**
     * 异步发布消息到指定组
     * 
     * @param name    消息名称/主题
     * @param content 消息内容
     * @param group   消息组
     * @return CompletableFuture<String> 消息ID
     */
    CompletableFuture<String> publishAsync(String name, Object content, String group);

    /**
     * 发布延迟消息
     * 
     * @param name         消息名称/主题
     * @param content      消息内容
     * @param delaySeconds 延迟秒数
     * @return 消息ID
     */
    String publishDelay(String name, Object content, long delaySeconds);

    /**
     * 发布延迟消息到指定组
     * 
     * @param name         消息名称/主题
     * @param content      消息内容
     * @param group        消息组
     * @param delaySeconds 延迟秒数
     * @return 消息ID
     */
    String publishDelay(String name, Object content, String group, long delaySeconds);

    /**
     * 发布事务性消息
     * 在数据库事务中发布消息，确保事务一致性
     * 
     * @param name    消息名称/主题
     * @param content 消息内容
     * @return 消息ID
     */
    String publishTransactional(String name, Object content);

    /**
     * 发布事务性消息到指定组
     * 
     * @param name    消息名称/主题
     * @param content 消息内容
     * @param group   消息组
     * @return 消息ID
     */
    String publishTransactional(String name, Object content, String group);
}