package com.guanwei.framework.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 默认用户详情服务
 * 提供基本的用户认证功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Service
public class DefaultUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 这里提供一个默认的用户实现
        // 在实际项目中，应该从数据库或其他数据源加载用户信息
        if ("admin".equals(username)) {
            return new User(
                    username,
                    "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa", // admin123
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }
        
        // 对于其他用户，返回一个默认的认证用户
        return new User(
                username,
                "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa", // 默认密码
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
} 