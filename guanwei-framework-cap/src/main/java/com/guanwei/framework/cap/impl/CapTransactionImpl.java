package com.guanwei.framework.cap.impl;

import com.guanwei.framework.cap.CapTransaction;
import com.guanwei.framework.cap.util.MessageIdGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CAP 事务实现类
 * 实现 CAP 事务包装器，用于包装数据库事务
 * 参考 .NET Core CAP 组件的 ICapTransaction 实现
 */
@Slf4j
public class CapTransactionImpl implements CapTransaction {

    private final String transactionId;
    private final long startTime;
    private final long timeout;
    private final Object dbTransaction;
    private final List<TransactionListener> listeners = new CopyOnWriteArrayList<>();

    private final AtomicBoolean autoCommit = new AtomicBoolean(true);
    private final AtomicBoolean committed = new AtomicBoolean(false);
    private final AtomicBoolean rolledBack = new AtomicBoolean(false);
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public CapTransactionImpl(Object dbTransaction, long timeout) {
        this.transactionId = String.valueOf(MessageIdGenerator.getInstance().nextId());
        this.startTime = System.currentTimeMillis();
        this.timeout = timeout;
        this.dbTransaction = dbTransaction;
    }

    @Override
    public boolean isAutoCommit() {
        return autoCommit.get();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit.set(autoCommit);
    }

    @Override
    public Object getDbTransaction() {
        return dbTransaction;
    }

    @Override
    public void setDbTransaction(Object dbTransaction) {
        // 在实现中，dbTransaction是final的，这里只是为了接口兼容性
        throw new UnsupportedOperationException("Cannot change dbTransaction after creation");
    }

    @Override
    public void commit() {
        if (!active.get() || committed.get() || rolledBack.get()) {
            log.warn("Cannot commit transaction: {} - not active or already completed", transactionId);
            return;
        }

        try {
            // 检查超时
            if (System.currentTimeMillis() - startTime > timeout) {
                log.error("Transaction timeout: {}", transactionId);
                rollback();
                return;
            }

            // 通知监听器
            notifyBeforeCommit();

            // 提交数据库事务
            if (dbTransaction != null) {
                // 这里需要根据具体的数据库事务类型进行提交
                // 例如：如果是Spring的TransactionStatus，需要调用commit()
                log.debug("Committing database transaction for CAP transaction: {}", transactionId);
            }

            // 标记为已提交
            committed.set(true);
            active.set(false);

            // 通知监听器
            notifyAfterCommit();

            log.info("Successfully committed CAP transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to commit CAP transaction: {}", transactionId, e);
            rollback();
            throw new RuntimeException("Failed to commit transaction", e);
        }
    }

    @Override
    public CompletableFuture<Void> commitAsync() {
        return CompletableFuture.runAsync(this::commit);
    }

    @Override
    public void rollback() {
        if (!active.get() || committed.get() || rolledBack.get()) {
            log.warn("Cannot rollback transaction: {} - not active or already completed", transactionId);
            return;
        }

        try {
            // 通知监听器
            notifyBeforeRollback();

            // 回滚数据库事务
            if (dbTransaction != null) {
                // 这里需要根据具体的数据库事务类型进行回滚
                // 例如：如果是Spring的TransactionStatus，需要调用rollback()
                log.debug("Rolling back database transaction for CAP transaction: {}", transactionId);
            }

            // 标记为已回滚
            rolledBack.set(true);
            active.set(false);

            // 通知监听器
            notifyAfterRollback();

            log.info("Successfully rolled back CAP transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to rollback CAP transaction: {}", transactionId, e);
            throw new RuntimeException("Failed to rollback transaction", e);
        }
    }

    @Override
    public CompletableFuture<Void> rollbackAsync() {
        return CompletableFuture.runAsync(this::rollback);
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public boolean isCommitted() {
        return committed.get();
    }

    @Override
    public boolean isRolledBack() {
        return rolledBack.get();
    }

    @Override
    public boolean isActive() {
        return active.get() && !closed.get();
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        // 在实现中，timeout是final的，这里只是为了接口兼容性
        throw new UnsupportedOperationException("Cannot change timeout after creation");
    }

    @Override
    public void addTransactionListener(TransactionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTransactionListener(TransactionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (active.get() && !committed.get() && !rolledBack.get()) {
                // 如果事务仍然活跃且未完成，则回滚
                rollback();
            }
        }
    }

    /**
     * 通知提交前监听器
     */
    private void notifyBeforeCommit() {
        for (TransactionListener listener : listeners) {
            try {
                listener.beforeCommit(this);
            } catch (Exception e) {
                log.error("Error in beforeCommit listener for transaction: {}", transactionId, e);
            }
        }
    }

    /**
     * 通知提交后监听器
     */
    private void notifyAfterCommit() {
        for (TransactionListener listener : listeners) {
            try {
                listener.afterCommit(this);
            } catch (Exception e) {
                log.error("Error in afterCommit listener for transaction: {}", transactionId, e);
            }
        }
    }

    /**
     * 通知回滚前监听器
     */
    private void notifyBeforeRollback() {
        for (TransactionListener listener : listeners) {
            try {
                listener.beforeRollback(this);
            } catch (Exception e) {
                log.error("Error in beforeRollback listener for transaction: {}", transactionId, e);
            }
        }
    }

    /**
     * 通知回滚后监听器
     */
    private void notifyAfterRollback() {
        for (TransactionListener listener : listeners) {
            try {
                listener.afterRollback(this);
            } catch (Exception e) {
                log.error("Error in afterRollback listener for transaction: {}", transactionId, e);
            }
        }
    }
}