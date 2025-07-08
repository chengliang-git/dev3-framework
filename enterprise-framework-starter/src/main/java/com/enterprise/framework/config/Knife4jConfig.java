package com.enterprise.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("企业级Spring Boot框架API文档")
                        .version("1.0.0")
                        .description("企业级Spring Boot 3开发框架，支持JWT认证、Redis缓存、RabbitMQ消息队列、Oracle数据库、Apollo配置中心等")
                        .contact(new Contact()
                                .name("Enterprise Framework")
                                .email("framework@enterprise.com")
                                .url("https://github.com/enterprise/framework"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}