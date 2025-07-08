package com.enterprise.framework.service;

import com.enterprise.framework.entity.User;
import com.enterprise.framework.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

/**
 * 用户详情服务实现类
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired(required = false)
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 如果UserMapper还不存在，返回默认用户
        if (userMapper == null) {
            if ("admin".equals(username)) {
                return new org.springframework.security.core.userdetails.User(
                        "admin",
                        "$2a$10$7JB720yubVSOMV0H5nnZP.IhbU6B3SrEP1KcxqvjTM1YRKDC/T3bC", // 密码是 admin
                        Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            }
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getStatus, 1));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                true,
                true,
                true,
                getAuthorities()
        );
    }

    private Collection<SimpleGrantedAuthority> getAuthorities() {
        // 这里可以根据用户角色返回权限列表
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}