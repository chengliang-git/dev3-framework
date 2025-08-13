package com.guanwei.framework.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 异步任务工具类
 * 提供异步任务执行的便捷方法
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class AsyncTaskUtils {

    /**
     * 异步执行任务
     */
    @Async("taskExecutor")
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task) {
        try {
            T result = task.get();
            log.debug("Async task completed successfully");
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Async task failed", e);
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 异步执行无返回值任务
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> executeAsync(Runnable task) {
        try {
            task.run();
            log.debug("Async task completed successfully");
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async task failed", e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 异步执行任务（指定执行器）
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task, Executor executor) {
        return CompletableFuture.supplyAsync(task, executor);
    }

    /**
     * 异步执行无返回值任务（指定执行器）
     */
    public CompletableFuture<Void> executeAsync(Runnable task, Executor executor) {
        return CompletableFuture.runAsync(task, executor);
    }

    /**
     * 延迟执行任务
     */
    public <T> CompletableFuture<T> executeDelayed(Supplier<T> task, long delay, TimeUnit unit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(unit.toMillis(delay));
                return task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task interrupted", e);
            }
        });
    }

    /**
     * 延迟执行无返回值任务
     */
    public CompletableFuture<Void> executeDelayed(Runnable task, long delay, TimeUnit unit) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(unit.toMillis(delay));
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task interrupted", e);
            }
        });
    }

    /**
     * 超时执行任务
     */
    public <T> CompletableFuture<T> executeWithTimeout(Supplier<T> task, long timeout, TimeUnit unit) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(task);
        
        return future.orTimeout(timeout, unit)
                .exceptionally(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        log.warn("Task execution timed out after {} {}", timeout, unit);
                    }
                    throw new RuntimeException("Task execution failed", throwable);
                });
    }

    /**
     * 重试执行任务
     */
    public <T> CompletableFuture<T> executeWithRetry(Supplier<T> task, int maxRetries, long retryDelay, TimeUnit unit) {
        return executeWithRetryInternal(task, maxRetries, retryDelay, unit, 0);
    }

    private <T> CompletableFuture<T> executeWithRetryInternal(Supplier<T> task, int maxRetries, long retryDelay, TimeUnit unit, int currentRetry) {
        return CompletableFuture.supplyAsync(task)
                .exceptionally(throwable -> {
                    if (currentRetry < maxRetries) {
                        log.warn("Task failed, retrying {}/{}", currentRetry + 1, maxRetries, throwable);
                        try {
                            Thread.sleep(unit.toMillis(retryDelay));
                            return executeWithRetryInternal(task, maxRetries, retryDelay, unit, currentRetry + 1).join();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Retry interrupted", e);
                        }
                    } else {
                        log.error("Task failed after {} retries", maxRetries, throwable);
                        throw new RuntimeException("Task failed after maximum retries", throwable);
                    }
                });
    }
} 