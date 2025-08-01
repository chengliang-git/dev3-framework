package com.guanwei.framework.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 框架自动配置类
 * 启用框架相关的配置属性
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(FrameworkProperties.class)
public class FrameworkAutoConfiguration {
} 