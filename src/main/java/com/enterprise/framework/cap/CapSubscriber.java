package com.enterprise.framework.cap;

import java.util.function.Consumer;

/**
 * CAP 订阅者接口
 * 负责消息的订阅和消费处理
 */
public interface CapSubscriber {

    /**
     * 订阅消息
     * 
     * @param name    消息名称/主题
     * @param handler 消息处理器
     */
    void subscribe(String name, Consumer<CapMessage> handler);

    /**
     * 订阅消息到指定组
     * 
     * @param name    消息名称/主题
     * @param group   消息组
     * @param handler 消息处理器
     */
    void subscribe(String name, String group, Consumer<CapMessage> handler);

    /**
     * 订阅消息（带返回值的处理器）
     * 
     * @param name    消息名称/主题
     * @param handler 消息处理器
     * @param <T>     返回值类型
     */
    <T> void subscribe(String name, MessageHandler<T> handler);

    /**
     * 订阅消息到指定组（带返回值的处理器）
     * 
     * @param name    消息名称/主题
     * @param group   消息组
     * @param handler 消息处理器
     * @param <T>     返回值类型
     */
    <T> void subscribe(String name, String group, MessageHandler<T> handler);

    /**
     * 取消订阅
     * 
     * @param name 消息名称/主题
     */
    void unsubscribe(String name);

    /**
     * 取消订阅指定组的消息
     * 
     * @param name  消息名称/主题
     * @param group 消息组
     */
    void unsubscribe(String name, String group);

    /**
     * 消息处理器接口
     * 
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    interface MessageHandler<T> {
        /**
         * 处理消息
         * 
         * @param message 消息对象
         * @return 处理结果
         */
        T handle(CapMessage message);
    }
}