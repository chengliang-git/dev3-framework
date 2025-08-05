package com.guanwei.framework.cap.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * CAP 默认订阅调用器
 * 参考 .NET Core CAP 的 SubscribeInvoker 类
 */
@Slf4j
public class DefaultSubscribeInvoker implements SubscribeInvoker {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public DefaultSubscribeInvoker() {
        this.applicationContext = null;
        this.objectMapper = new ObjectMapper();
    }

    @Autowired
    public DefaultSubscribeInvoker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object invokeAsync(CapMessage message, ConsumerExecutorDescriptor descriptor) {
        try {
            if (descriptor == null) {
                throw new IllegalArgumentException("Descriptor cannot be null");
            }

            // 获取订阅者实例
            Object subscriber = getSubscriberInstance(descriptor);
            if (subscriber == null) {
                throw new RuntimeException("Cannot find subscriber instance for: " + descriptor.getImplTypeInfo().getName());
            }

            // 获取方法
            Method method = descriptor.getMethodInfo();
            if (method == null) {
                throw new RuntimeException("Method info is null");
            }

            // 反序列化消息内容
            Object messageContent = deserializeMessage(message, method);

            // 调用方法
            if (method.getParameterCount() == 0) {
                return method.invoke(subscriber);
            } else if (method.getParameterCount() == 1) {
                return method.invoke(subscriber, messageContent);
            } else {
                throw new RuntimeException("Unsupported method parameter count: " + method.getParameterCount());
            }

        } catch (Exception ex) {
            log.error("Error invoking subscriber method: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to invoke subscriber method", ex);
        }
    }

    @Override
    public CompletableFuture<Object> invokeAsync(CapMessage message, ConsumerExecutorDescriptor descriptor, Object cancellationToken) {
        return CompletableFuture.supplyAsync(() -> invokeAsync(message, descriptor));
    }

    /**
     * 获取订阅者实例
     */
    private Object getSubscriberInstance(ConsumerExecutorDescriptor descriptor) {
        if (applicationContext == null) {
            // 如果没有ApplicationContext，尝试直接实例化
            try {
                return descriptor.getImplTypeInfo().getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                log.error("Failed to create subscriber instance: {}", ex.getMessage(), ex);
                return null;
            }
        }

        // 从Spring容器中获取实例
        try {
            return applicationContext.getBean(descriptor.getImplTypeInfo());
        } catch (Exception ex) {
            log.error("Failed to get subscriber bean from Spring context: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 反序列化消息内容
     */
    private Object deserializeMessage(CapMessage message, Method method) {
        try {
            if (method.getParameterCount() == 0) {
                return null;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            
            // 如果参数类型是String，直接返回消息内容
            if (parameterType == String.class) {
                return message.getContent();
            }

            // 如果参数类型是CapMessage，直接返回消息对象
            if (parameterType == CapMessage.class) {
                return message;
            }

            // 尝试JSON反序列化
            if (message.getContent() != null && !message.getContent().isEmpty()) {
                return objectMapper.readValue(message.getContent(), parameterType);
            }

            // 如果消息内容为空，尝试创建默认实例
            return parameterType.getDeclaredConstructor().newInstance();

        } catch (Exception ex) {
            log.error("Failed to deserialize message content: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to deserialize message content", ex);
        }
    }
} 