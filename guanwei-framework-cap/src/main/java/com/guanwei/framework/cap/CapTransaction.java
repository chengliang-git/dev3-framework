package com.guanwei.framework.cap;

import java.util.concurrent.CompletableFuture;

/**
 * CAP 事务包装器
 * 用于包装数据库事务，提供一致的用户接口
 * 参考 .NET Core CAP 组件的 ICapTransaction 接口
 */
public interface CapTransaction extends AutoCloseable {

    /**
     * 是否在消息发布后自动提交事务
     */
    boolean isAutoCommit();

    /**
     * 设置是否自动提交
     */
    void setAutoCommit(boolean autoCommit);

    /**
     * 获取数据库事务对象
     * 可以转换为具体的数据库事务对象或 IDBTransaction
     */
    Object getDbTransaction();

    /**
     * 设置数据库事务对象
     */
    void setDbTransaction(Object dbTransaction);

    /**
     * 提交 CAP 的事务上下文
     * 在提交时我们会将消息发送到消息队列
     */
    void commit();

    /**
     * 异步提交 CAP 的事务上下文
     * 在提交时我们会将消息发送到消息队列
     */
    CompletableFuture<Void> commitAsync();

    /**
     * 回滚 CAP 的事务上下文
     * 我们会删除当前事务上下文中未存储的缓冲区数据
     */
    void rollback();

    /**
     * 异步回滚 CAP 的事务上下文
     * 我们会删除当前事务上下文中未存储的缓冲区数据
     */
    CompletableFuture<Void> rollbackAsync();

    /**
     * 获取事务ID
     */
    String getTransactionId();

    /**
     * 检查事务是否已提交
     */
    boolean isCommitted();

    /**
     * 检查事务是否已回滚
     */
    boolean isRolledBack();

    /**
     * 检查事务是否处于活动状态
     */
    boolean isActive();

    /**
     * 获取事务开始时间
     */
    long getStartTime();

    /**
     * 获取事务超时时间（毫秒）
     */
    long getTimeout();

    /**
     * 设置事务超时时间（毫秒）
     */
    void setTimeout(long timeout);

    /**
     * 添加事务监听器
     */
    void addTransactionListener(TransactionListener listener);

    /**
     * 移除事务监听器
     */
    void removeTransactionListener(TransactionListener listener);

    /**
     * 事务监听器接口
     */
    interface TransactionListener {
        /**
         * 事务提交前调用
         */
        void beforeCommit(CapTransaction transaction);

        /**
         * 事务提交后调用
         */
        void afterCommit(CapTransaction transaction);

        /**
         * 事务回滚前调用
         */
        void beforeRollback(CapTransaction transaction);

        /**
         * 事务回滚后调用
         */
        void afterRollback(CapTransaction transaction);
    }
}