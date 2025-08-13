package com.guanwei.framework.config.apollo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Apollo配置使用示例
 * 展示如何使用Apollo配置中心
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class ApolloConfigExample implements CommandLineRunner {

    @Autowired
    private ApolloConfig.ApolloConfigManager apolloConfigManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Apollo配置中心使用示例 ===");
        
        // 获取字符串配置
        String appName = apolloConfigManager.getStringProperty("app.name", "Unknown");
        log.info("应用名称: {}", appName);
        
        // 获取整数配置
        Integer maxRequests = apolloConfigManager.getIntProperty("framework.security.api.max-requests-per-minute", 100);
        log.info("最大请求数: {}", maxRequests);
        
        // 获取布尔配置
        Boolean apiEnabled = apolloConfigManager.getBooleanProperty("framework.security.api.enabled", true);
        log.info("API安全是否启用: {}", apiEnabled);
        
        // 获取长整数配置
        Long jwtExpiration = apolloConfigManager.getLongProperty("framework.security.jwt.expiration", 86400000L);
        log.info("JWT过期时间: {}ms", jwtExpiration);
        
        // 获取双精度浮点数配置
        Double cacheQuality = apolloConfigManager.getDoubleProperty("framework.file-management.compression-quality", 0.8);
        log.info("压缩质量: {}", cacheQuality);
        
        // 检查配置项是否存在
        boolean hasCustomConfig = apolloConfigManager.containsKey("custom.config");
        log.info("是否存在自定义配置: {}", hasCustomConfig);
        
        // 获取所有配置项
        log.info("所有配置项数量: {}", apolloConfigManager.getPropertyNames().size());
        
        // 添加配置变更监听器
        apolloConfigManager.addChangeListener(new com.ctrip.framework.apollo.ConfigChangeListener() {
            @Override
            public void onChange(com.ctrip.framework.apollo.model.ConfigChangeEvent changeEvent) {
                log.info("配置变更事件: {}", changeEvent.changedKeys());
                
                for (String key : changeEvent.changedKeys()) {
                    com.ctrip.framework.apollo.model.ConfigChange change = changeEvent.getChange(key);
                    log.info("配置项 {} 变更: {} -> {}", 
                            key, change.getOldValue(), change.getNewValue());
                    
                    // 根据配置项类型进行相应的处理
                    handleConfigChange(key, change.getOldValue(), change.getNewValue());
                }
            }
        });
        
        log.info("Apollo配置中心示例运行完成");
    }

    /**
     * 处理配置变更
     */
    private void handleConfigChange(String key, String oldValue, String newValue) {
        // 根据配置项类型进行相应的处理
        if (key.startsWith("framework.security")) {
            log.info("安全配置变更: {} = {}", key, newValue);
            // 重新加载安全配置
            reloadSecurityConfig();
        } else if (key.startsWith("framework.cache")) {
            log.info("缓存配置变更: {} = {}", key, newValue);
            // 重新加载缓存配置
            reloadCacheConfig();
        } else if (key.startsWith("framework.rate-limit")) {
            log.info("限流配置变更: {} = {}", key, newValue);
            // 重新加载限流配置
            reloadRateLimitConfig();
        } else if (key.startsWith("framework.workflow")) {
            log.info("工作流配置变更: {} = {}", key, newValue);
            // 重新加载工作流配置
            reloadWorkflowConfig();
        }
    }

    /**
     * 重新加载安全配置
     */
    private void reloadSecurityConfig() {
        log.info("重新加载安全配置...");
        // 这里实现安全配置的重新加载逻辑
        // 例如：重新创建JWT工具类、更新安全过滤器等
    }

    /**
     * 重新加载缓存配置
     */
    private void reloadCacheConfig() {
        log.info("重新加载缓存配置...");
        // 这里实现缓存配置的重新加载逻辑
        // 例如：清空缓存、重新创建缓存管理器等
    }

    /**
     * 重新加载限流配置
     */
    private void reloadRateLimitConfig() {
        log.info("重新加载限流配置...");
        // 这里实现限流配置的重新加载逻辑
        // 例如：更新限流器参数、重新创建限流桶等
    }

    /**
     * 重新加载工作流配置
     */
    private void reloadWorkflowConfig() {
        log.info("重新加载工作流配置...");
        // 这里实现工作流配置的重新加载逻辑
        // 例如：更新流程引擎配置、重新部署工作流等
    }
}
