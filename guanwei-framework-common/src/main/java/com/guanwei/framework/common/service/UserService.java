package com.guanwei.framework.common.service;

import com.guanwei.framework.common.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 扩展基础服务接口，提供用户特定的业务操作
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
public interface UserService extends BaseService<User> {

    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 批量查询用户
     */
    List<User> findBatchByIds(List<String> ids);

    /**
     * 启用用户
     */
    boolean enableUser(String id);

    /**
     * 禁用用户
     */
    boolean disableUser(String id);

    /**
     * 重置密码
     */
    boolean resetPassword(String id, String newPassword);

    /**
     * 修改密码
     */
    boolean changePassword(String id, String oldPassword, String newPassword);
}