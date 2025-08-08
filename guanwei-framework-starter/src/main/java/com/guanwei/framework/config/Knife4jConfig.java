package com.guanwei.framework.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * Knife4j配置类
 * 
 * @author Enterprise Framework
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "knife4j", name = "enable", havingValue = "true", matchIfMissing = false)
public class Knife4jConfig {

    /**
     * 配置OpenAPI信息
     */
    @Bean("frameworkOpenAPI")
    public OpenAPI customOpenAPI() {
        log.info("🔧 框架默认 OpenAPI 配置已加载");
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
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes(HttpHeaders.AUTHORIZATION, new SecurityScheme()
                                .name(HttpHeaders.AUTHORIZATION)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .scheme("Bearer")
                                .bearerFormat("JWT")));
    }

    /**
     * 全局自定义扩展
     * <p>
     * 在OpenAPI规范中，Operation 是一个表示 API 端点（Endpoint）或操作的对象。
     * 每个路径（Path）对象可以包含一个或多个 Operation 对象，用于描述与该路径相关联的不同 HTTP 方法（例如 GET、POST、PUT 等）。
     */
    @Bean("frameworkGlobalOpenApiCustomizer")
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            // 全局添加鉴权参数
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((s, pathItem) -> {
                    // 接口添加鉴权参数
                    pathItem.readOperations()
                            .forEach(operation -> operation
                                    .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION)));
                });
            }
        };
    }
}