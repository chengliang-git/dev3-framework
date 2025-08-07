package com.guanwei.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 登录响应DTO
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户角色")
    private List<String> roles;

    @Schema(description = "Token过期时间戳")
    private Long expiresIn;
}
