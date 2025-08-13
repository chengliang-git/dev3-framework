package com.guanwei.framework.config.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import java.util.Set;

/**
 * Apollo配置中心配置
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.config-center", name = "type", havingValue = "apollo")
public class ApolloConfig {

    private Config config;

    @PostConstruct
    public void init() {
        // 获取默认命名空间的配置
        config = ConfigService.getAppConfig();
        
        // 添加配置变更监听器
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                log.info("Apollo配置变更: {}", changeEvent.changedKeys());
                
                for (String key : changeEvent.changedKeys()) {
                    ConfigChange change = changeEvent.getChange(key);
                    log.info("配置项 {} 变更: {} -> {}", 
                            key, change.getOldValue(), change.getNewValue());
                    
                    // 这里可以添加配置热更新的逻辑
                    handleConfigChange(key, change.getOldValue(), change.getNewValue());
                }
            }
        });
        
        log.info("Apollo配置中心初始化完成");
    }

    /**
     * Apollo配置管理器
     */
    @Bean
    @Primary
    public ApolloConfigManager apolloConfigManager() {
        return new ApolloConfigManager(config);
    }

    /**
     * 处理配置变更
     */
    private void handleConfigChange(String key, String oldValue, String newValue) {
        // 根据配置项类型进行相应的处理
        if (key.startsWith("framework.security")) {
            log.info("安全配置变更: {} = {}", key, newValue);
            // 重新加载安全配置
        } else if (key.startsWith("framework.cache")) {
            log.info("缓存配置变更: {} = {}", key, newValue);
            // 重新加载缓存配置
        } else if (key.startsWith("framework.rate-limit")) {
            log.info("限流配置变更: {} = {}", key, newValue);
            // 重新加载限流配置
        }
    }

    /**
     * Apollo配置管理器
     */
    public static class ApolloConfigManager {

        private final Config config;

        public ApolloConfigManager(Config config) {
            this.config = config;
        }

        /**
         * 获取字符串配置
         */
        public String getStringProperty(String key, String defaultValue) {
            return config.getProperty(key, defaultValue);
        }

        /**
         * 获取整数配置
         */
        public Integer getIntProperty(String key, Integer defaultValue) {
            return config.getIntProperty(key, defaultValue);
        }

        /**
         * 获取长整数配置
         */
        public Long getLongProperty(String key, Long defaultValue) {
            return config.getLongProperty(key, defaultValue);
        }

        /**
         * 获取布尔配置
         */
        public Boolean getBooleanProperty(String key, Boolean defaultValue) {
            return config.getBooleanProperty(key, defaultValue);
        }

        /**
         * 获取双精度浮点数配置
         */
        public Double getDoubleProperty(String key, Double defaultValue) {
            return config.getDoubleProperty(key, defaultValue);
        }

        /**
         * 获取配置项的所有键
         */
        public Set<String> getPropertyNames() {
            return config.getPropertyNames();
        }

        /**
         * 检查配置项是否存在
         */
        public boolean containsKey(String key) {
            // Apollo Config 没有 containsKey 方法，通过获取属性值来判断
            String value = config.getProperty(key, null);
            return value != null;
        }

        /**
         * 添加配置变更监听器
         */
        public void addChangeListener(ConfigChangeListener listener) {
            config.addChangeListener(listener);
        }

        /**
         * 移除配置变更监听器
         */
        public void removeChangeListener(ConfigChangeListener listener) {
            config.removeChangeListener(listener);
        }
    }
}
