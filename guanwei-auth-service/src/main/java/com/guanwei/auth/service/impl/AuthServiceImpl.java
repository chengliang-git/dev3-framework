package com.guanwei.auth.service.impl;

import com.guanwei.auth.dto.LoginRequest;
import com.guanwei.auth.dto.LoginResponse;
import com.guanwei.auth.dto.RegisterRequest;
import com.guanwei.auth.dto.OAuth2TokenRequest;
import com.guanwei.auth.dto.OAuth2TokenResponse;
import com.guanwei.auth.service.AuthService;
import com.guanwei.framework.common.exception.BusinessException;
import com.guanwei.framework.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 认证服务实现类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // 生成JWT Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(1L, userDetails.getUsername());

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(userDetails.getUsername());
            response.setRealName("管理员"); // 这里应该从数据库获取
            response.setRoles(Arrays.asList("ROLE_ADMIN")); // 这里应该从数据库获取
            response.setExpiresIn(jwtTokenUtil.getExpirationDate(token).getTime());

            log.info("用户 {} 登录成功", loginRequest.getUsername());
            return response;
        } catch (Exception e) {
            log.warn("用户 {} 登录失败: {}", loginRequest.getUsername(), e.getMessage());
            throw new BusinessException("用户名或密码错误");
        }
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        log.info("用户注册: {}", registerRequest.getUsername());

        // 验证密码确认
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException("密码和确认密码不一致");
        }

        // 这里可以添加用户注册的具体逻辑
        // 例如：检查用户名是否已存在、保存用户信息到数据库等
        
        log.info("用户注册成功: {}", registerRequest.getUsername());
    }

    @Override
    public String refreshToken(String refreshToken) {
        log.info("刷新Token");

        try {
            if (!jwtTokenUtil.isTokenExpired(refreshToken)) {
                String username = jwtTokenUtil.getUsername(refreshToken);
                return jwtTokenUtil.refreshToken(refreshToken);
            } else {
                throw new BusinessException("刷新令牌已过期");
            }
        } catch (Exception e) {
            log.warn("刷新Token失败: {}", e.getMessage());
            throw new BusinessException("刷新Token失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        log.debug("验证Token");
        return jwtTokenUtil.validateToken(token);
    }

    @Override
    public OAuth2TokenResponse getOAuth2Token(OAuth2TokenRequest tokenRequest) {
        log.info("获取OAuth2访问令牌: {}", tokenRequest.getClientId());

        try {
            // 这里应该验证授权码和客户端信息
            // 暂时返回模拟数据
            OAuth2TokenResponse response = new OAuth2TokenResponse();
            response.setAccessToken("oauth2_access_token_" + System.currentTimeMillis());
            response.setRefreshToken("oauth2_refresh_token_" + System.currentTimeMillis());
            response.setExpiresIn(3600L);
            response.setScope(tokenRequest.getClientId());

            log.info("OAuth2令牌生成成功: {}", tokenRequest.getClientId());
            return response;
        } catch (Exception e) {
            log.warn("获取OAuth2令牌失败: {}", e.getMessage());
            throw new BusinessException("获取OAuth2令牌失败: " + e.getMessage());
        }
    }

    @Override
    public void revokeToken(String token) {
        log.info("撤销Token");

        try {
            // 这里应该将Token加入黑名单或从数据库中删除
            // 暂时只是记录日志
            log.info("Token已撤销: {}", token);
        } catch (Exception e) {
            log.warn("撤销Token失败: {}", e.getMessage());
            throw new BusinessException("撤销Token失败: " + e.getMessage());
        }
    }
}
