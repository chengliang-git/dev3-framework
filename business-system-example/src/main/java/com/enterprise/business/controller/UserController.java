package com.enterprise.business.controller;

import com.enterprise.framework.cap.CapPublisher;
import com.enterprise.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 * 展示如何在业务系统中使用框架功能
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private CapPublisher capPublisher;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, Object> userInfo) {
        try {
            String username = (String) userInfo.get("username");
            String email = (String) userInfo.get("email");

            log.info("用户注册: {}", username);

            // 发布用户注册事件
            Map<String, Object> eventData = Map.of(
                    "username", username,
                    "email", email,
                    "timestamp", System.currentTimeMillis());

            String messageId = capPublisher.publish("user.registered", eventData, "user-service");
            log.info("发布用户注册事件: {}", messageId);

            return Result.success("用户注册成功，事件ID: " + messageId);
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("用户注册失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUser(@PathVariable String id) {
        // 模拟获取用户信息
        Map<String, Object> user = Map.of(
                "id", id,
                "username", "user_" + id,
                "email", "user" + id + "@example.com",
                "status", "active");

        return Result.success(user);
    }
}