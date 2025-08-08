package com.guanwei.framework.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 安全模块自动配置
 * - 仅在 Spring Security 存在时生效
 * - 允许业务侧自定义同名 Bean 覆盖
 */
@AutoConfiguration
@ConditionalOnClass(org.springframework.security.config.annotation.web.builders.HttpSecurity.class)
@ComponentScan(basePackages = "com.guanwei.framework.security")
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }
}