package com.guanwei.framework.config.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.Validator;

/**
 * 验证配置
 * 提供统一的数据验证和参数绑定增强
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class ValidationConfig {

    /**
     * 验证器工厂Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalValidatorFactoryBean validatorFactoryBean() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        log.info("Validator factory bean initialized");
        return factoryBean;
    }

    /**
     * 验证器
     */
    @Bean
    @ConditionalOnMissingBean
    public Validator validator(LocalValidatorFactoryBean validatorFactoryBean) {
        log.info("Validator initialized");
        return validatorFactoryBean;
    }

    /**
     * 自定义验证器
     */
    @Bean
    @ConditionalOnMissingBean
    public CustomValidator customValidator() {
        log.info("Custom validator initialized");
        return new CustomValidator();
    }
}

/**
 * 自定义验证器
 */
class CustomValidator {
    
    /**
     * 验证手机号
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // 简单的手机号验证，实际项目中可以使用更复杂的正则表达式
        return phone.matches("^1[3-9]\\d{9}$");
    }
    
    /**
     * 验证邮箱
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // 简单的邮箱验证
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * 验证身份证号
     */
    public boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        // 简单的身份证号验证（18位）
        return idCard.matches("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    }
} 