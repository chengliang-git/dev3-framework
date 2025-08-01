package com.guanwei.framework.controller;

import com.guanwei.framework.common.controller.BaseController;
import com.guanwei.framework.common.entity.User;
import com.guanwei.framework.common.result.Result;
import com.guanwei.framework.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 展示优化后的架构使用
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Tag(name = "用户管理", description = "用户相关操作接口")
@RestController
@RequestMapping("/users")
public class UserController extends BaseController<UserService, User> {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected UserService getService() {
        return userService;
    }

    @Operation(summary = "根据用户名查询用户", description = "根据用户名查询用户信息")
    @GetMapping("/username/{username}")
    public Result<User> findByUsername(@Parameter(description = "用户名") @PathVariable String username) {
        return userService.findByUsername(username)
                .map(Result::success)
                .orElse(Result.error("用户不存在"));
    }

    @Operation(summary = "根据邮箱查询用户", description = "根据邮箱查询用户信息")
    @GetMapping("/email/{email}")
    public Result<User> findByEmail(@Parameter(description = "邮箱") @PathVariable String email) {
        return userService.findByEmail(email)
                .map(Result::success)
                .orElse(Result.error("用户不存在"));
    }

    @Operation(summary = "批量查询用户", description = "根据ID列表批量查询用户")
    @PostMapping("/batch")
    public Result<List<User>> findBatchByIds(@RequestBody List<String> ids) {
        List<User> users = userService.findBatchByIds(ids);
        return Result.success(users);
    }

    @Operation(summary = "启用用户", description = "启用指定用户")
    @PutMapping("/{id}/enable")
    public Result<Boolean> enableUser(@Parameter(description = "用户ID") @PathVariable String id) {
        boolean success = userService.enableUser(id);
        return success ? Result.success(true) : Result.error("启用用户失败");
    }

    @Operation(summary = "禁用用户", description = "禁用指定用户")
    @PutMapping("/{id}/disable")
    public Result<Boolean> disableUser(@Parameter(description = "用户ID") @PathVariable String id) {
        boolean success = userService.disableUser(id);
        return success ? Result.success(true) : Result.error("禁用用户失败");
    }
}