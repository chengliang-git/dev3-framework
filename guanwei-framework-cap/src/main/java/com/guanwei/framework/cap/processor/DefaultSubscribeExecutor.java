package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapMessageStatus;
import com.guanwei.framework.cap.CapProperties;
import com.guanwei.framework.cap.storage.MessageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * CAP 默认订阅执行器
 * 参考 .NET Core CAP 的 SubscribeExecutor 类
 */
@Slf4j
@Component
public class DefaultSubscribeExecutor implements SubscribeExecutor {

    private final CapProperties properties;
    private final MessageStorage messageStorage;
    private final SubscribeInvoker subscribeInvoker;

    @Autowired
    public DefaultSubscribeExecutor(CapProperties properties, MessageStorage messageStorage) {
        this.properties = properties;
        this.messageStorage = messageStorage;
        this.subscribeInvoker = new DefaultSubscribeInvoker();
    }

    @Override
    public CompletableFuture<OperateResult> executeAsync(CapMessage message) {
        return executeAsync(message, null);
    }

    @Override
    public CompletableFuture<OperateResult> executeAsync(CapMessage message, ConsumerExecutorDescriptor descriptor) {
        if (message == null) {
            return CompletableFuture.completedFuture(OperateResult.failed("Message is null"));
        }

        try {
            // 记录执行实例ID
            message.setHeader("cap-execution-instance-id", getInstanceId());

            boolean retry;
            OperateResult result;

            do {
                var executionResult = executeWithoutRetryAsync(message, descriptor);
                result = executionResult.getResult();
                if (result.isSucceeded()) {
                    return CompletableFuture.completedFuture(result);
                }
                retry = executionResult.isShouldRetry();
            } while (retry);

            return CompletableFuture.completedFuture(result);

        } catch (Exception ex) {
            log.error("Error executing message: {}", message.getId(), ex);
            return CompletableFuture.completedFuture(OperateResult.failed(ex));
        }
    }

    private ExecutionResult executeWithoutRetryAsync(CapMessage message, ConsumerExecutorDescriptor descriptor) {
        try {
            if (descriptor == null) {
                // 如果没有提供描述符，尝试查找订阅者
                log.warn("No descriptor provided for message: {}", message.getName());
                return new ExecutionResult(OperateResult.failed("No subscriber found"), false);
            }

            log.debug("Executing subscriber: {}.{}", 
                    descriptor.getImplTypeInfo().getSimpleName(), 
                    descriptor.getMethodInfo().getName());

            long startTime = System.currentTimeMillis();

            // 调用订阅者方法
            Object result = subscribeInvoker.invokeAsync(message, descriptor);

            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Subscriber executed successfully in {}ms", executionTime);

            // 更新消息状态为成功
            messageStorage.changeReceiveStateAsync(message, CapMessageStatus.SUCCEEDED)
                    .exceptionally(ex -> {
                        log.error("Failed to update message status to succeeded: {}", message.getId(), ex);
                        return null;
                    });

            return new ExecutionResult(OperateResult.success(), false);

        } catch (Exception ex) {
            log.error("Error executing subscriber for message: {}", message.getId(), ex);

            // 更新消息状态为失败
            messageStorage.changeReceiveStateAsync(message, CapMessageStatus.FAILED)
                    .exceptionally(updateEx -> {
                        log.error("Failed to update message status to failed: {}", message.getId(), updateEx);
                        return null;
                    });

            // 检查是否需要重试
            boolean shouldRetry = updateMessageForRetry(message);
            return new ExecutionResult(OperateResult.failed(ex), shouldRetry);
        }
    }

    private boolean updateMessageForRetry(CapMessage message) {
        int retries = message.getRetries() + 1;
        message.setRetries(retries);

        int retryCount = Math.min(properties.getFailedRetryCount(), 3);
        if (retries >= retryCount) {
            if (retries == properties.getFailedRetryCount()) {
                log.warn("Message {} reached maximum retry count: {}", message.getId(), properties.getFailedRetryCount());
            }
            return false;
        }

        log.debug("Message {} will be retried, current retry count: {}", message.getId(), retries);
        return true;
    }

    private String getInstanceId() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            int workerId = Math.abs(hostname.hashCode() % 1023);
            return hostname + "_" + workerId;
        } catch (Exception ex) {
            return "unknown_" + System.currentTimeMillis();
        }
    }

    /**
     * 执行结果包装类
     */
    private static class ExecutionResult {
        private final OperateResult result;
        private final boolean shouldRetry;

        public ExecutionResult(OperateResult result, boolean shouldRetry) {
            this.result = result;
            this.shouldRetry = shouldRetry;
        }

        public OperateResult getResult() {
            return result;
        }

        public boolean isShouldRetry() {
            return shouldRetry;
        }
    }
} 