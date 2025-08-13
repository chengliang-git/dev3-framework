package com.guanwei.framework.config.i18n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * 国际化配置
 * 提供多语言支持和时区处理
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * 国际化消息源
     */
    @Bean
    @ConditionalOnMissingBean(name = "messageSource")
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages", "i18n/validation", "i18n/errors");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // 1小时缓存
        messageSource.setUseCodeAsDefaultMessage(true);
        
        log.info("Message source initialized with basenames: i18n/messages, i18n/validation, i18n/errors");
        return messageSource;
    }

    /**
     * 区域解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE); // 默认中文
        
        log.info("Locale resolver initialized with default locale: {}", Locale.SIMPLIFIED_CHINESE);
        return localeResolver;
    }

    /**
     * 区域变更拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // 通过lang参数切换语言
        
        log.info("Locale change interceptor initialized with param name: lang");
        return interceptor;
    }

    /**
     * 添加国际化拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * 国际化工具类
     */
    @Bean
    @ConditionalOnMissingBean
    public I18nUtils i18nUtils(ResourceBundleMessageSource messageSource, LocaleResolver localeResolver) {
        log.info("I18n utils initialized");
        return new I18nUtils(messageSource, localeResolver);
    }
}

/**
 * 国际化工具类
 */
class I18nUtils {
    
    private final ResourceBundleMessageSource messageSource;
    private final LocaleResolver localeResolver;
    
    public I18nUtils(ResourceBundleMessageSource messageSource, LocaleResolver localeResolver) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
    }
    
    /**
     * 获取国际化消息
     */
    public String getMessage(String code) {
        return getMessage(code, null, null);
    }
    
    /**
     * 获取国际化消息（带参数）
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, null);
    }
    
    /**
     * 获取国际化消息（带默认值）
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        try {
            return messageSource.getMessage(code, args, defaultMessage, getCurrentLocale());
        } catch (Exception e) {
            return defaultMessage != null ? defaultMessage : code;
        }
    }
    
    /**
     * 获取当前区域
     */
    public Locale getCurrentLocale() {
        try {
            // 这里需要从当前请求上下文获取区域
            // 简化实现，返回默认区域
            return Locale.SIMPLIFIED_CHINESE;
        } catch (Exception e) {
            return Locale.SIMPLIFIED_CHINESE;
        }
    }
} 