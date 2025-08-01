package com.guanwei.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置类
 * 
 * @author Enterprise Framework
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置OpenAPI信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise Framework API")
                        .description("企业级Spring Boot框架API文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Enterprise Framework")
                                .email("support@enterprise.com")
                                .url("https://github.com/enterprise/framework"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}