package com.guanwei.tles.casetransfer.config;

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
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;

import jakarta.annotation.PostConstruct;

/**
 * OpenAPI配置类
 * 配置Swagger UI支持JWT认证
 *
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.application.name", havingValue = "guanwei-tles-case-transfer")
public class OpenApiConfig {

        @PostConstruct
        public void init() {
                log.info("🚀 案件转存服务 OpenApiConfig 配置类已初始化");
        }

        @Bean("caseTransferOpenAPI")
        @Primary
        public OpenAPI customOpenAPI() {
                log.info("🔧 案件转存服务 OpenAPI 配置已加载 - 使用自定义配置");

                return new OpenAPI()
                                .info(new Info()
                                                .title("案件转存服务API文档")
                                                .description("案件数据转存服务 - 接收CAP消息并转存案件数据")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Guanwei Framework")
                                                                .email("support@guanwei.com")
                                                                .url("https://www.guanwei.com"))
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
        @Bean("caseTransferGlobalOpenApiCustomizer")
        public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
                log.info("🔧 案件转存服务 GlobalOpenApiCustomizer 配置已加载");
                return openApi -> {
                        // 全局添加鉴权参数
                        if (openApi.getPaths() != null) {
                                openApi.getPaths().forEach((s, pathItem) -> {
                                        // 接口添加鉴权参数
                                        pathItem.readOperations()
                                                        .forEach(operation -> operation.addSecurityItem(
                                                                        new SecurityRequirement().addList(
                                                                                        HttpHeaders.AUTHORIZATION)));
                                });
                        }
                };
        }
}
