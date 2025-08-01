package com.guanwei.framework.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guanwei.framework.common.entity.User;
import com.guanwei.framework.common.exception.BusinessException;
import com.guanwei.framework.common.service.UserService;
import com.guanwei.framework.common.service.impl.BaseServiceImpl;
import com.guanwei.framework.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 * 展示业务逻辑实现
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return findOne(queryWrapper);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return findOne(queryWrapper);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return findOne(queryWrapper);
    }

    @Override
    public List<User> findBatchByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return baseRepository.listByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableUser(String id) {
        User user = baseRepository.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(1); // 1表示启用
        return baseRepository.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableUser(String id) {
        User user = baseRepository.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(0); // 0表示禁用
        return baseRepository.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(String id, String newPassword) {
        User user = baseRepository.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return baseRepository.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(String id, String oldPassword, String newPassword) {
        User user = baseRepository.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return baseRepository.updateById(user);
    }
}