package com.guanwei.tles.casetransfer.config;

import com.guanwei.framework.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.guanwei.framework.security.SecurityExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 案件转存服务安全配置
 * 启用JWT认证，保护API接口
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired(required = false)
    private SecurityExceptionHandler securityExceptionHandler;

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean("caseTransferSecurityFilterChain")
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 允许无需认证的路径
                        .requestMatchers("/api/auth/**", "/doc.html", "/webjars/**", "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/actuator/**", "/error")
                        .permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated())
                // 统一异常 JSON 返回
                .exceptionHandling(ex -> {
                    if (securityExceptionHandler != null) {
                        ex.authenticationEntryPoint(securityExceptionHandler)
                          .accessDeniedHandler(securityExceptionHandler);
                    }
                })
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}