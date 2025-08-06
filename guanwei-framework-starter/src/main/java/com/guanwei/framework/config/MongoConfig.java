//package com.guanwei.framework.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.data.mongodb.config.EnableMongoAuditing;
//import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
//import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
//
//import java.util.Optional;
//
///**
// * MongoDB配置 - 暂时禁用
// *
// * @author Enterprise Framework
// * @since 1.0.0
// */
//// @Configuration
//// @EnableMongoAuditing
//public class MongoConfig {
//
//    /**
//     * 启用MongoDB审计功能
//     */
//    @Bean
//    public AuditorAware<String> auditorProvider() {
//        return () -> {
//            try {
//                // 这里可以从SecurityContext中获取当前登录用户
//                // 暂时返回默认值，实际使用时需要根据具体的安全框架实现
//                return Optional.of("system");
//            } catch (Exception e) {
//                return Optional.of("system");
//            }
//        };
//    }
//
//    /**
//     * MongoDB验证监听器
//     */
//    @Bean
//    public ValidatingMongoEventListener validatingMongoEventListener() {
//        return new ValidatingMongoEventListener(validator());
//    }
//
//    /**
//     * 验证器
//     */
//    @Bean
//    public LocalValidatorFactoryBean validator() {
//        return new LocalValidatorFactoryBean();
//    }
//}