package com.guanwei.auth.controller;

import com.guanwei.auth.dto.LoginRequest;
import com.guanwei.auth.dto.LoginResponse;
import com.guanwei.auth.dto.RegisterRequest;
import com.guanwei.auth.dto.OAuth2TokenRequest;
import com.guanwei.auth.dto.OAuth2TokenResponse;
import com.guanwei.auth.service.AuthService;
import com.guanwei.framework.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并返回JWT Token")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return Result.success(response);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<String> register(@RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return Result.success("注册成功");
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息")
    public Result<UserDetails> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return Result.success((UserDetails) authentication.getPrincipal());
        }
        return Result.error("用户未登录");
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "刷新JWT Token")
    public Result<LoginResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            String newToken = authService.refreshToken(token);
            
            LoginResponse response = new LoginResponse();
            response.setToken(newToken);
            response.setUsername("admin"); // 这里应该从Token中解析
            response.setExpiresIn(System.currentTimeMillis() + 86400000);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("Token刷新失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "验证Token", description = "验证JWT Token是否有效")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            boolean isValid = authService.validateToken(token);
            return Result.success(isValid);
        } catch (Exception e) {
            return Result.error("Token验证失败: " + e.getMessage());
        }
    }

    @PostMapping("/revoke")
    @Operation(summary = "撤销Token", description = "撤销JWT Token")
    public Result<String> revokeToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            authService.revokeToken(token);
            return Result.success("Token已撤销");
        } catch (Exception e) {
            return Result.error("Token撤销失败: " + e.getMessage());
        }
    }
}
