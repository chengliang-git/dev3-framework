package com.guanwei.auth.service;

import com.guanwei.auth.dto.LoginRequest;
import com.guanwei.auth.dto.LoginResponse;
import com.guanwei.auth.dto.RegisterRequest;
import com.guanwei.auth.dto.OAuth2TokenRequest;
import com.guanwei.auth.dto.OAuth2TokenResponse;

/**
 * 认证服务接口
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     */
    void register(RegisterRequest registerRequest);

    /**
     * 刷新Token
     * 
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);

    /**
     * 验证Token
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 获取OAuth2访问令牌
     * 
     * @param tokenRequest OAuth2令牌请求
     * @return OAuth2令牌响应
     */
    OAuth2TokenResponse getOAuth2Token(OAuth2TokenRequest tokenRequest);

    /**
     * 撤销Token
     * 
     * @param token 要撤销的令牌
     */
    void revokeToken(String token);
}
