package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.annotation.CapSubscribe;
import com.guanwei.framework.cap.queue.CapQueueManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CAP 订阅处理器
 * 用于处理带 @CapSubscribe 注解的订阅方法
 * 参考 .NET Core CAP 组件的订阅处理机制
 */
@Slf4j
public class CapSubscribeProcessor implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private ApplicationContext applicationContext;
    private final Map<String, SubscribeHandler> handlers = new ConcurrentHashMap<>();
    private CapSubscriberProcessor subscriberProcessor;
    private boolean initialized = false;

    public CapSubscribeProcessor() {
        log.info("CapSubscribeProcessor constructor called");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("CapSubscribeProcessor setApplicationContext called");
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("CapSubscribeProcessor onApplicationEvent called, initialized: {}, event context: {}", 
                initialized, event.getApplicationContext().getDisplayName());
        
        if (initialized || event.getApplicationContext() != applicationContext) {
            log.info("CapSubscribeProcessor skipping initialization - already initialized or different context");
            return;
        }

        // 在上下文刷新完成后初始化，确保所有bean都已创建
        initializeAfterContextRefresh();
    }

    /**
     * 手动触发初始化（用于测试或特殊情况）
     */
    public void initialize() {
        log.info("CapSubscribeProcessor manual initialize called");
        if (!initialized && applicationContext != null) {
            initializeAfterContextRefresh();
        }
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 在上下文刷新后初始化
     */
    private void initializeAfterContextRefresh() {
        log.info("CapSubscribeProcessor initializeAfterContextRefresh started");
        try {
            // 获取 subscriberProcessor
            if (subscriberProcessor == null) {
                try {
                    this.subscriberProcessor = applicationContext.getBean(CapSubscriberProcessor.class);
                    log.info("CapSubscribeProcessor successfully got CapSubscriberProcessor bean");
                } catch (Exception e) {
                    log.warn("CapSubscriberProcessor not available yet, will retry later", e);
                }
            }

            // 扫描订阅方法
            scanSubscribeMethods();
            // 注册之前扫描到的处理器
            registerPendingHandlers();

            initialized = true;
            log.info("CapSubscribeProcessor initialized successfully with {} handlers", handlers.size());
        } catch (Exception e) {
            log.error("Failed to initialize CapSubscribeProcessor", e);
        }
    }

    /**
     * 扫描订阅方法
     */
    private void scanSubscribeMethods() {
        log.info("CapSubscribeProcessor scanSubscribeMethods started");
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int scannedCount = 0;
        int handlerCount = 0;

        log.info("CapSubscribeProcessor found {} bean definitions", beanNames.length);

        for (String beanName : beanNames) {
            try {
                // 跳过当前正在创建的bean，避免循环依赖
                if (beanName.equals("capSubscribeProcessor")) {
                    log.debug("Skipping current bean to avoid circular dependency: {}", beanName);
                    continue;
                }

                // 检查Bean是否已经创建，避免循环依赖
                if (applicationContext.containsBean(beanName)) {
                    Object bean = applicationContext.getBean(beanName);
                    Class<?> beanClass = bean.getClass();
                    scannedCount++;

                    log.debug("Scanning bean: {} of type: {}", beanName, beanClass.getSimpleName());

                    // 扫描所有带有@CapSubscribe注解的方法
                    int handlersInBean = scanMethodsForSubscribe(bean, beanClass);
                    handlerCount += handlersInBean;
                    if (handlersInBean > 0) {
                        log.info("Found {} handlers in bean: {}", handlersInBean, beanName);
                    }
                } else {
                    // 如果Bean还没有创建，跳过它，等待后续扫描
                    log.debug("Bean not yet created, skipping: {}", beanName);
                }
            } catch (Exception e) {
                // 如果获取Bean失败，跳过它
                log.debug("Failed to get bean: {}, skipping", beanName, e);
            }
        }

        log.info("CapSubscribeProcessor scanned {} beans, found {} subscribe handlers", scannedCount, handlerCount);
    }

    /**
     * 扫描类中的订阅方法
     */
    private int scanMethodsForSubscribe(Object bean, Class<?> beanClass) {
        Method[] methods = beanClass.getDeclaredMethods();
        int handlerCount = 0;

        for (Method method : methods) {
            CapSubscribe annotation = method.getAnnotation(CapSubscribe.class);
            if (annotation != null && annotation.enabled()) {
                registerSubscribeHandler(bean, method, annotation);
                handlerCount++;
            }
        }

        return handlerCount;
    }

    /**
     * 注册订阅处理器
     */
    private void registerSubscribeHandler(Object bean, Method method, CapSubscribe annotation) {
        String messageName = annotation.value();
        if (messageName.isEmpty()) {
            messageName = method.getName();
        }

        String group = annotation.group();
        if (group.isEmpty()) {
            group = "default";
        }

        String handlerKey = buildHandlerKey(messageName, group);

        SubscribeHandler handler = new SubscribeHandler(bean, method, annotation);
        handlers.put(handlerKey, handler);

        log.info("Registered subscribe handler: {} -> {}.{} (message: {}, group: {})", 
                handlerKey, bean.getClass().getSimpleName(), method.getName(), messageName, group);

        // 立即创建队列（如果队列管理器可用）
        createQueueIfNeeded(messageName, group);

        // 注册到订阅者处理器（延迟注册，避免循环依赖）
        if (subscriberProcessor != null) {
            subscriberProcessor.registerHandler(messageName, group, handler);
            log.info("Successfully registered handler to subscriber processor: {} -> {}.{}", 
                    handlerKey, bean.getClass().getSimpleName(), method.getName());
        } else {
            log.warn("SubscriberProcessor not available yet, handler will be registered later: {} -> {}.{}",
                    handlerKey, bean.getClass().getSimpleName(), method.getName());
        }
    }

    /**
     * 创建队列（如果需要）
     */
    private void createQueueIfNeeded(String messageName, String group) {
        log.info("CapSubscribeProcessor createQueueIfNeeded called for message: {} (group: {})", messageName, group);
        try {
            // 尝试获取队列管理器
            if (applicationContext != null) {
                try {
                    CapQueueManager queueManager = applicationContext.getBean(CapQueueManager.class);
                    if (queueManager != null) {
                        String queueName = queueManager.createQueueAndBind(messageName, group);
                        log.info("Successfully created queue for subscription: {} (group: {}) -> {}", messageName, group, queueName);
                    } else {
                        log.warn("Queue manager is null");
                    }
                } catch (Exception e) {
                    log.warn("Queue manager not available yet, queue will be created later: {} - {}", messageName, e.getMessage());
                }
            } else {
                log.warn("ApplicationContext is null, cannot create queue");
            }
        } catch (Exception e) {
            log.error("Failed to create queue for subscription: {} (group: {})", messageName, group, e);
        }
    }

    /**
     * 构建处理器键
     */
    private String buildHandlerKey(String messageName, String group) {
        return messageName + ":" + group;
    }

    /**
     * 获取处理器
     */
    public SubscribeHandler getHandler(String messageName, String group) {
        String handlerKey = buildHandlerKey(messageName, group);
        return handlers.get(handlerKey);
    }

    /**
     * 注册待处理的处理器
     */
    private void registerPendingHandlers() {
        if (subscriberProcessor != null) {
            for (Map.Entry<String, SubscribeHandler> entry : handlers.entrySet()) {
                SubscribeHandler handler = entry.getValue();
                // 从 handlerKey 中提取 messageName 和 group
                String[] parts = entry.getKey().split(":");
                if (parts.length == 2) {
                    String messageName = parts[0];
                    String group = parts[1];
                    subscriberProcessor.registerHandler(messageName, group, handler);
                    log.info("Registered pending handler: {} -> {}.{}", entry.getKey(),
                            handler.getBean().getClass().getSimpleName(), handler.getMethod().getName());
                }
            }
        }
    }

    /**
     * 订阅处理器
     */
    public static class SubscribeHandler {
        private final Object bean;
        private final Method method;
        private final CapSubscribe annotation;

        public SubscribeHandler(Object bean, Method method, CapSubscribe annotation) {
            this.bean = bean;
            this.method = method;
            this.annotation = annotation;
        }

        /**
         * 处理消息
         */
        public Object handle(CapMessage message) throws Exception {
            // 检查重试次数
            Integer retries = message.getRetries();
            if (retries != null && retries >= annotation.maxRetries()) {
                log.warn("Message {} exceeded max retries ({})", message.getId(), annotation.maxRetries());
                throw new RuntimeException("Message exceeded max retries");
            }

            // 设置方法可访问
            method.setAccessible(true);

            // 调用方法
            if (method.getParameterCount() == 1) {
                // 单参数方法，直接传递消息
                return method.invoke(bean, message);
            } else if (method.getParameterCount() == 2) {
                // 双参数方法，可能是消息内容和消息对象
                // 这里需要根据方法参数类型来决定如何调用
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes[0] == String.class && paramTypes[1] == CapMessage.class) {
                    return method.invoke(bean, message.getContent(), message);
                } else if (paramTypes[0] == CapMessage.class && paramTypes[1] == String.class) {
                    return method.invoke(bean, message, message.getContent());
                }
            }

            // 默认调用方式
            return method.invoke(bean, message);
        }

        public Object getBean() {
            return bean;
        }

        public Method getMethod() {
            return method;
        }

        public CapSubscribe getAnnotation() {
            return annotation;
        }

        public boolean isAsync() {
            return annotation.async();
        }

        public int getMaxRetries() {
            return annotation.maxRetries();
        }

        public long getRetryInterval() {
            return annotation.retryInterval();
        }

        public String getHandlerName() {
            return annotation.handlerName();
        }

        public CapSubscribe.MessageType getMessageType() {
            return annotation.messageType();
        }
    }
}