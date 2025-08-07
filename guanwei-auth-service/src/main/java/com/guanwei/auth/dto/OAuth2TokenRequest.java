package com.guanwei.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * OAuth2令牌请求DTO
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@Schema(description = "OAuth2令牌请求")
public class OAuth2TokenRequest {

    @NotBlank(message = "授权码不能为空")
    @Schema(description = "授权码")
    private String code;

    @NotBlank(message = "客户端ID不能为空")
    @Schema(description = "客户端ID")
    private String clientId;

    @NotBlank(message = "客户端密钥不能为空")
    @Schema(description = "客户端密钥")
    private String clientSecret;

    @Schema(description = "重定向URI")
    private String redirectUri;

    @Schema(description = "授权类型", example = "authorization_code")
    private String grantType = "authorization_code";
}
