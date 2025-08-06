package com.guanwei.framework.cap.impl;

import com.guanwei.framework.cap.CapTransaction;
import com.guanwei.framework.cap.CapTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CAP 事务管理器实现
 * 负责管理 CAP 事务的生命周期
 * 参考 .NET Core CAP 组件的事务管理机制
 */
@Slf4j
public class CapTransactionManagerImpl implements CapTransactionManager {

    private final ThreadLocal<CapTransaction> currentTransaction = new ThreadLocal<>();
    private final List<CapTransaction.TransactionListener> listeners = new CopyOnWriteArrayList<>();

    // 统计信息
    private final AtomicLong committedCount = new AtomicLong(0);
    private final AtomicLong rolledBackCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);
    private final AtomicLong totalTransactionTime = new AtomicLong(0);
    private final AtomicLong maxTransactionTime = new AtomicLong(0);
    private final AtomicLong minTransactionTime = new AtomicLong(Long.MAX_VALUE);

    @Override
    public CapTransaction beginTransaction() {
        return beginTransaction(30000); // 默认30秒超时
    }

    @Override
    public CapTransaction beginTransaction(long timeout) {
        return beginTransaction(null, timeout);
    }

    @Override
    public CapTransaction beginTransaction(Object dbTransaction) {
        return beginTransaction(dbTransaction, 30000); // 默认30秒超时
    }

    @Override
    public CapTransaction beginTransaction(Object dbTransaction, long timeout) {
        CapTransaction transaction = new CapTransactionImpl(dbTransaction, timeout);
        currentTransaction.set(transaction);
        return transaction;
    }

    @Override
    public CompletableFuture<CapTransaction> beginTransactionAsync() {
        return CompletableFuture.supplyAsync(this::beginTransaction);
    }

    @Override
    public CompletableFuture<CapTransaction> beginTransactionAsync(long timeout) {
        return CompletableFuture.supplyAsync(() -> beginTransaction(timeout));
    }

    @Override
    public CompletableFuture<CapTransaction> beginTransactionAsync(Object dbTransaction) {
        return CompletableFuture.supplyAsync(() -> beginTransaction(dbTransaction));
    }

    @Override
    public CompletableFuture<CapTransaction> beginTransactionAsync(Object dbTransaction, long timeout) {
        return CompletableFuture.supplyAsync(() -> beginTransaction(dbTransaction, timeout));
    }

    @Override
    public CapTransaction getCurrentTransaction() {
        return currentTransaction.get();
    }

    @Override
    public boolean hasActiveTransaction() {
        CapTransaction transaction = currentTransaction.get();
        return transaction != null && transaction.isActive();
    }

    @Override
    public void commitCurrentTransaction() {
        CapTransaction transaction = currentTransaction.get();
        if (transaction != null) {
            transaction.commit();
            currentTransaction.remove();
        }
    }

    @Override
    public CompletableFuture<Void> commitCurrentTransactionAsync() {
        return CompletableFuture.runAsync(this::commitCurrentTransaction);
    }

    @Override
    public void rollbackCurrentTransaction() {
        CapTransaction transaction = currentTransaction.get();
        if (transaction != null) {
            transaction.rollback();
            currentTransaction.remove();
        }
    }

    @Override
    public CompletableFuture<Void> rollbackCurrentTransactionAsync() {
        return CompletableFuture.runAsync(this::rollbackCurrentTransaction);
    }

    @Override
    public void addTransactionListener(CapTransaction.TransactionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTransactionListener(CapTransaction.TransactionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void cleanupExpiredTransactions() {
        // 清理过期的线程本地事务
        CapTransaction transaction = currentTransaction.get();
        if (transaction != null && !transaction.isActive()) {
            currentTransaction.remove();
        }
    }

    @Override
    public TransactionStatistics getStatistics() {
        return new TransactionStatistics() {
            @Override
            public int getActiveTransactionCount() {
                return hasActiveTransaction() ? 1 : 0;
            }

            @Override
            public long getCommittedTransactionCount() {
                return committedCount.get();
            }

            @Override
            public long getRolledBackTransactionCount() {
                return rolledBackCount.get();
            }

            @Override
            public long getTimeoutTransactionCount() {
                return timeoutCount.get();
            }

            @Override
            public double getAverageTransactionTime() {
                long total = totalTransactionTime.get();
                long committed = committedCount.get();
                long rolledBack = rolledBackCount.get();
                long totalCount = committed + rolledBack;
                return totalCount > 0 ? (double) total / totalCount : 0.0;
            }

            @Override
            public long getMaxTransactionTime() {
                return maxTransactionTime.get();
            }

            @Override
            public long getMinTransactionTime() {
                long min = minTransactionTime.get();
                return min == Long.MAX_VALUE ? 0 : min;
            }
        };
    }

    /**
     * 通知事务监听器
     */
    void notifyBeforeCommit(CapTransaction transaction) {
        for (CapTransaction.TransactionListener listener : listeners) {
            try {
                listener.beforeCommit(transaction);
            } catch (Exception e) {
                log.error("Error in beforeCommit listener", e);
            }
        }
    }

    /**
     * 通知事务监听器
     */
    void notifyAfterCommit(CapTransaction transaction) {
        for (CapTransaction.TransactionListener listener : listeners) {
            try {
                listener.afterCommit(transaction);
            } catch (Exception e) {
                log.error("Error in afterCommit listener", e);
            }
        }
    }

    /**
     * 通知事务监听器
     */
    void notifyBeforeRollback(CapTransaction transaction) {
        for (CapTransaction.TransactionListener listener : listeners) {
            try {
                listener.beforeRollback(transaction);
            } catch (Exception e) {
                log.error("Error in beforeRollback listener", e);
            }
        }
    }

    /**
     * 通知事务监听器
     */
    void notifyAfterRollback(CapTransaction transaction) {
        for (CapTransaction.TransactionListener listener : listeners) {
            try {
                listener.afterRollback(transaction);
            } catch (Exception e) {
                log.error("Error in afterRollback listener", e);
            }
        }
    }

    /**
     * 更新统计信息
     */
    void updateStatistics(long transactionTime, boolean committed, boolean timeout) {
        if (committed) {
            committedCount.incrementAndGet();
        } else {
            rolledBackCount.incrementAndGet();
        }

        if (timeout) {
            timeoutCount.incrementAndGet();
        }

        totalTransactionTime.addAndGet(transactionTime);

        // 更新最大和最小时间
        long currentMax = maxTransactionTime.get();
        while (transactionTime > currentMax && !maxTransactionTime.compareAndSet(currentMax, transactionTime)) {
            currentMax = maxTransactionTime.get();
        }

        long currentMin = minTransactionTime.get();
        while (transactionTime < currentMin && !minTransactionTime.compareAndSet(currentMin, transactionTime)) {
            currentMin = minTransactionTime.get();
        }
    }
}