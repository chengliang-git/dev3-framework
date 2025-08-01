package com.guanwei.framework.service;

import com.guanwei.framework.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 模拟用户数据，实际项目中应该从数据库查询
        if ("admin".equals(username)) {
            User user = new User();
            user.setId(1L);
            user.setUsername("admin");
            user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa"); // admin123
            user.setEmail("admin@example.com");
            user.setDeleted(0);
            user.setStatus(1);

            // 构建用户权限
            Collection<SimpleGrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"));

            // 返回UserDetails对象
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(user.getStatus() == 0)
                    .build();
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
