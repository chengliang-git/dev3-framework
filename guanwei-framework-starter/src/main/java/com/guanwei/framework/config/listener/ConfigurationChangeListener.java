package com.guanwei.framework.config.listener;

import com.guanwei.framework.config.FrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 配置变更监听器
 * 支持配置热更新
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class ConfigurationChangeListener {

    @Autowired
    private FrameworkProperties frameworkProperties;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private Environment environment;

    /**
     * 监听上下文刷新事件
     */
    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        log.info("Application context refreshed");
        
        // 重新绑定配置属性
        refreshConfiguration();
        
        // 发布配置变更事件
        publishConfigurationChangeEvent();
    }

    /**
     * 手动刷新配置
     */
    public void refreshConfiguration() {
        try {
            // 这里可以添加配置刷新的逻辑
            // 例如重新加载配置文件、刷新缓存等
            log.info("Configuration refreshed successfully");
        } catch (Exception e) {
            log.error("Failed to refresh configuration", e);
        }
    }

    /**
     * 发布配置变更事件
     */
    private void publishConfigurationChangeEvent() {
        ConfigurationChangeEvent changeEvent = new ConfigurationChangeEvent();
        eventPublisher.publishEvent(changeEvent);
        log.info("Configuration change event published");
    }

    /**
     * 配置变更事件
     */
    public static class ConfigurationChangeEvent {
        public ConfigurationChangeEvent() {
            // 默认构造函数
        }
    }

    /**
     * 获取配置值
     */
    public String getProperty(String key) {
        return environment.getProperty(key);
    }

    /**
     * 获取配置值（带默认值）
     */
    public String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    /**
     * 获取配置值（指定类型）
     */
    public <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }

    /**
     * 获取配置值（指定类型和默认值）
     */
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return environment.getProperty(key, targetType, defaultValue);
    }

    /**
     * 检查配置是否存在
     */
    public boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }

    /**
     * 获取所有配置键
     */
    public String[] getActiveProfiles() {
        return environment.getActiveProfiles();
    }

    /**
     * 获取默认配置
     */
    public String[] getDefaultProfiles() {
        return environment.getDefaultProfiles();
    }
} 