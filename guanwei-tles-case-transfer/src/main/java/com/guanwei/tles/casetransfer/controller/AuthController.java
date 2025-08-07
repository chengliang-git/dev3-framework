package com.guanwei.tles.casetransfer.controller;

import com.guanwei.framework.common.result.Result;
import com.guanwei.framework.security.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 提供JWT认证相关接口
 *
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "JWT认证相关接口")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取JWT token")
    public Result<Map<String, String>> login(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {

        log.info("用户登录请求: {}", username);

        try {
            // 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 生成JWT token
            String token = jwtTokenUtil.generateToken(1L, username); // 使用默认用户ID 1
            log.info("生成的JWT token: {}", token);

            // 返回token
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("message", "登录成功");

            log.info("用户 {} 登录成功", username);
            return Result.success(response);

        } catch (Exception e) {
            log.warn("用户 {} 登录失败: {}", username, e.getMessage());
            return Result.error("用户名或密码错误");
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "验证Token", description = "验证JWT token是否有效")
    public Result<Map<String, Object>> validateToken(
            @Parameter(description = "JWT Token", required = true) @RequestParam String token) {

        log.debug("验证JWT token");

        try {
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsername(token);
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", username);
                response.put("message", "Token有效");
                return Result.success(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Token无效");
                return Result.success(response);
            }
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token验证失败");
            return Result.success(response);
        }
    }
}
