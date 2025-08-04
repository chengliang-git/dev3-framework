package com.guanwei.framework.cap;

import java.util.concurrent.CompletableFuture;

/**
 * CAP 事务管理器
 * 负责管理 CAP 事务的生命周期
 * 参考 .NET Core CAP 组件的事务管理机制
 */
public interface CapTransactionManager {

    /**
     * 开始一个新的事务
     * 
     * @return CAP 事务对象
     */
    CapTransaction beginTransaction();

    /**
     * 开始一个新的事务（带超时）
     * 
     * @param timeout 超时时间（毫秒）
     * @return CAP 事务对象
     */
    CapTransaction beginTransaction(long timeout);

    /**
     * 开始一个新的事务（带数据库事务）
     * 
     * @param dbTransaction 数据库事务对象
     * @return CAP 事务对象
     */
    CapTransaction beginTransaction(Object dbTransaction);

    /**
     * 开始一个新的事务（带数据库事务和超时）
     * 
     * @param dbTransaction 数据库事务对象
     * @param timeout       超时时间（毫秒）
     * @return CAP 事务对象
     */
    CapTransaction beginTransaction(Object dbTransaction, long timeout);

    /**
     * 异步开始一个新的事务
     * 
     * @return CAP 事务对象的Future
     */
    CompletableFuture<CapTransaction> beginTransactionAsync();

    /**
     * 异步开始一个新的事务（带超时）
     * 
     * @param timeout 超时时间（毫秒）
     * @return CAP 事务对象的Future
     */
    CompletableFuture<CapTransaction> beginTransactionAsync(long timeout);

    /**
     * 异步开始一个新的事务（带数据库事务）
     * 
     * @param dbTransaction 数据库事务对象
     * @return CAP 事务对象的Future
     */
    CompletableFuture<CapTransaction> beginTransactionAsync(Object dbTransaction);

    /**
     * 异步开始一个新的事务（带数据库事务和超时）
     * 
     * @param dbTransaction 数据库事务对象
     * @param timeout       超时时间（毫秒）
     * @return CAP 事务对象的Future
     */
    CompletableFuture<CapTransaction> beginTransactionAsync(Object dbTransaction, long timeout);

    /**
     * 获取当前事务
     * 
     * @return 当前事务对象，如果没有则返回null
     */
    CapTransaction getCurrentTransaction();

    /**
     * 检查是否有活动的事务
     * 
     * @return 是否有活动的事务
     */
    boolean hasActiveTransaction();

    /**
     * 提交当前事务
     */
    void commitCurrentTransaction();

    /**
     * 异步提交当前事务
     * 
     * @return 提交操作的Future
     */
    CompletableFuture<Void> commitCurrentTransactionAsync();

    /**
     * 回滚当前事务
     */
    void rollbackCurrentTransaction();

    /**
     * 异步回滚当前事务
     * 
     * @return 回滚操作的Future
     */
    CompletableFuture<Void> rollbackCurrentTransactionAsync();

    /**
     * 注册事务监听器
     * 
     * @param listener 事务监听器
     */
    void addTransactionListener(CapTransaction.TransactionListener listener);

    /**
     * 移除事务监听器
     * 
     * @param listener 事务监听器
     */
    void removeTransactionListener(CapTransaction.TransactionListener listener);

    /**
     * 清理过期的事务
     */
    void cleanupExpiredTransactions();

    /**
     * 获取事务统计信息
     * 
     * @return 事务统计信息
     */
    TransactionStatistics getStatistics();

    /**
     * 事务统计信息
     */
    interface TransactionStatistics {
        /**
         * 获取活动事务数量
         */
        int getActiveTransactionCount();

        /**
         * 获取已提交事务数量
         */
        long getCommittedTransactionCount();

        /**
         * 获取已回滚事务数量
         */
        long getRolledBackTransactionCount();

        /**
         * 获取超时事务数量
         */
        long getTimeoutTransactionCount();

        /**
         * 获取平均事务执行时间（毫秒）
         */
        double getAverageTransactionTime();

        /**
         * 获取最长事务执行时间（毫秒）
         */
        long getMaxTransactionTime();

        /**
         * 获取最短事务执行时间（毫秒）
         */
        long getMinTransactionTime();
    }
}