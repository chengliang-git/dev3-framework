package com.guanwei.framework.cap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * CAP 发布者接口
 * 负责消息的发布和事务性消息处理
 * 参考 .NET Core CAP 组件的发布者接口
 */
public interface CapPublisher {

        /**
         * 发布消息
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @return 消息ID
         */
        Long publish(String name, Object content);

        /**
         * 发布消息到指定组
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param group   消息组
         * @return 消息ID
         */
        Long publish(String name, Object content, String group);

        /**
         * 发布消息（带回调名称）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param callbackName 回调名称
         * @param group        消息组
         * @return 消息ID
         */
        Long publish(String name, Object content, String callbackName, String group);

        /**
         * 发布消息（带消息头）
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param headers 消息头
         * @return 消息ID
         */
        Long publish(String name, Object content, Map<String, String> headers);

        /**
         * 发布消息（带消息头和组）
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param headers 消息头
         * @param group   消息组
         * @return 消息ID
         */
        Long publish(String name, Object content, Map<String, String> headers, String group);

        /**
         * 异步发布消息
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishAsync(String name, Object content);

        /**
         * 异步发布消息到指定组
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param group   消息组
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishAsync(String name, Object content, String group);

        /**
         * 异步发布消息（带回调名称）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param callbackName 回调名称
         * @param group        消息组
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishAsync(String name, Object content, String callbackName, String group);

        /**
         * 异步发布消息（带消息头）
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param headers 消息头
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishAsync(String name, Object content, Map<String, String> headers);

        /**
         * 异步发布消息（带消息头和组）
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param headers 消息头
         * @param group   消息组
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishAsync(String name, Object content, Map<String, String> headers, String group);

        /**
         * 发布延迟消息
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param delaySeconds 延迟秒数
         * @return 消息ID
         */
        Long publishDelay(String name, Object content, long delaySeconds);

        /**
         * 发布延迟消息到指定组
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param group        消息组
         * @param delaySeconds 延迟秒数
         * @return 消息ID
         */
        Long publishDelay(String name, Object content, String group, long delaySeconds);

        /**
         * 发布延迟消息（带回调名称）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param callbackName 回调名称
         * @param group        消息组
         * @param delaySeconds 延迟秒数
         * @return 消息ID
         */
        Long publishDelay(String name, Object content, String callbackName, String group, long delaySeconds);

        /**
         * 发布延迟消息（带消息头）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param headers      消息头
         * @param delaySeconds 延迟秒数
         * @return 消息ID
         */
        Long publishDelay(String name, Object content, Map<String, String> headers, long delaySeconds);

        /**
         * 发布延迟消息（带消息头和组）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param headers      消息头
         * @param group        消息组
         * @param delaySeconds 延迟秒数
         * @return 消息ID
         */
        Long publishDelay(String name, Object content, Map<String, String> headers, String group, long delaySeconds);

        /**
         * 异步发布延迟消息
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param delaySeconds 延迟秒数
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishDelayAsync(String name, Object content, long delaySeconds);

        /**
         * 异步发布延迟消息到指定组
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param group        消息组
         * @param delaySeconds 延迟秒数
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishDelayAsync(String name, Object content, String group, long delaySeconds);

        /**
         * 异步发布延迟消息（带回调名称）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param callbackName 回调名称
         * @param group        消息组
         * @param delaySeconds 延迟秒数
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishDelayAsync(String name, Object content, String callbackName, String group,
                        long delaySeconds);

        /**
         * 异步发布延迟消息（带消息头）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param headers      消息头
         * @param delaySeconds 延迟秒数
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishDelayAsync(String name, Object content, Map<String, String> headers,
                        long delaySeconds);

        /**
         * 异步发布延迟消息（带消息头和组）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param headers      消息头
         * @param group        消息组
         * @param delaySeconds 延迟秒数
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishDelayAsync(String name, Object content, Map<String, String> headers,
                        String group,
                        long delaySeconds);

        /**
         * 发布事务性消息
         * 在数据库事务中发布消息，确保事务一致性
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @return 消息ID
         */
        Long publishTransactional(String name, Object content);

        /**
         * 发布事务性消息到指定组
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param group   消息组
         * @return 消息ID
         */
        Long publishTransactional(String name, Object content, String group);

        /**
         * 发布事务性消息（带回调名称）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param callbackName 回调名称
         * @param group        消息组
         * @return 消息ID
         */
        Long publishTransactional(String name, Object content, String callbackName, String group);

        /**
         * 发布事务性消息（带消息头）
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param headers 消息头
         * @return 消息ID
         */
        Long publishTransactional(String name, Object content, Map<String, String> headers);

        /**
         * 发布事务性消息（带消息头和组）
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param headers 消息头
         * @param group   消息组
         * @return 消息ID
         */
        Long publishTransactional(String name, Object content, Map<String, String> headers, String group);

        /**
         * 异步发布事务性消息
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishTransactionalAsync(String name, Object content);

        /**
         * 异步发布事务性消息到指定组
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param group   消息组
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishTransactionalAsync(String name, Object content, String group);

        /**
         * 异步发布事务性消息（带回调名称）
         * 
         * @param name         消息名称/主题
         * @param content      消息内容
         * @param callbackName 回调名称
         * @param group        消息组
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishTransactionalAsync(String name, Object content, String callbackName,
                        String group);

        /**
         * 异步发布事务性消息（带消息头）
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param headers 消息头
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishTransactionalAsync(String name, Object content, Map<String, String> headers);

        /**
         * 异步发布事务性消息（带消息头和组）
         * 
         * @param name    消息名称/主题
         * @param content 消息内容
         * @param headers 消息头
         * @param group   消息组
         * @return 消息ID的Future
         */
        CompletableFuture<Long> publishTransactionalAsync(String name, Object content, Map<String, String> headers,
                        String group);
}