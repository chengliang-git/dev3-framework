package com.guanwei.framework.controller;

import com.guanwei.framework.common.result.Result;
import com.guanwei.framework.security.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Tag(name = "认证管理", description = "用户登录、注册等认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 进行用户认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 生成JWT Token
            String token = jwtTokenUtil.generateToken(1L, userDetails.getUsername());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("username", userDetails.getUsername());
            result.put("authorities", userDetails.getAuthorities());

            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error("用户名或密码错误");
        }
    }

    @Operation(summary = "获取用户信息", description = "根据Token获取当前登录用户信息")
    @PostMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", "admin");
        userInfo.put("realName", "管理员");
        userInfo.put("avatar", "https://avatars.githubusercontent.com/u/1?v=4");
        userInfo.put("roles", new String[]{"admin"});
        userInfo.put("permissions", new String[]{"*:*:*"});
        
        return Result.success(userInfo);
    }

    @Operation(summary = "用户退出", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<String> logout() {
        // JWT无状态，客户端删除token即可
        return Result.success("退出成功");
    }

    /**
     * 登录请求参数
     */
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}