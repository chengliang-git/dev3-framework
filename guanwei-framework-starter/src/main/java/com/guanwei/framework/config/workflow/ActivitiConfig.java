package com.guanwei.framework.config.workflow;

import lombok.extern.slf4j.Slf4j;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.List;

/**
 * Activiti7工作流配置
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.workflow", name = "enabled", havingValue = "true")
public class ActivitiConfig {

    /**
     * 配置Activiti流程引擎
     * 使用兼容的配置方式
     */
    @Bean
    public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer() {
        return processEngineConfiguration -> {
            try {
                // 使用UUID生成器
                processEngineConfiguration.setIdGenerator(new StrongUuidGenerator());
                
                // 启用历史记录
                processEngineConfiguration.setHistory("full");
                
                // 启用数据库事件日志
                processEngineConfiguration.setEnableDatabaseEventLogging(true);
                
                log.info("Activiti流程引擎基础配置完成");
                
                // 尝试启用异步执行器（如果方法可用）
                try {
                    java.lang.reflect.Method method = processEngineConfiguration.getClass().getMethod("setAsyncExecutorEnabled", boolean.class);
                    method.invoke(processEngineConfiguration, true);
                    log.info("Activiti异步执行器已启用");
                } catch (Exception e) {
                    log.info("Activiti异步执行器配置方法不可用，跳过配置: {}", e.getMessage());
                }
                
            } catch (Exception e) {
                log.warn("Activiti流程引擎配置过程中遇到问题: {}", e.getMessage());
            }
        };
    }

    /**
     * 用户组管理器
     */
    @Bean
    @Primary
    public UserGroupManager userGroupManager() {
        return new UserGroupManager() {
            @Override
            public List<String> getUserGroups(String userId) {
                // 这里应该从数据库或外部系统获取用户组信息
                // 暂时返回默认组
                return Arrays.asList("users", "managers");
            }

            @Override
            public List<String> getUserRoles(String userId) {
                // 这里应该从数据库或外部系统获取用户角色信息
                // 暂时返回默认角色
                return Arrays.asList("USER", "MANAGER");
            }

            @Override
            public List<String> getGroups() {
                // 返回所有可用的组
                return Arrays.asList("users", "managers", "admins");
            }

            @Override
            public List<String> getUsers() {
                // 返回所有可用的用户
                return Arrays.asList("admin", "user1", "user2");
            }
        };
    }
}
