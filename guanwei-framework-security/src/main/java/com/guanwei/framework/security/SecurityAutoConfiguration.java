package com.guanwei.framework.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 安全模块自动配置
 * 确保所有安全相关组件被正确扫描和配置
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Configuration
@ComponentScan(basePackages = "com.guanwei.framework.security")
public class SecurityAutoConfiguration {
} 