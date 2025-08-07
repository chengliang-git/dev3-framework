package com.guanwei.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * OAuth2令牌响应DTO
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@Schema(description = "OAuth2令牌响应")
public class OAuth2TokenResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "过期时间（秒）")
    private Long expiresIn;

    @Schema(description = "授权范围")
    private String scope;
}
