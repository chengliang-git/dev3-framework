package com.guanwei.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 认证服务 Knife4j 配置类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Configuration
public class AuthServiceKnife4jConfig {

    /**
     * 配置OpenAPI信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("认证授权服务 API")
                        .description("认证授权服务 - 提供JWT、OAuth2认证和第三方授权功能")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Guanwei Framework")
                                .email("support@guanwei.com")
                                .url("https://github.com/guanwei/framework"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * 创建API密钥安全方案
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("请输入JWT Token，格式：Bearer {token}");
    }
}
