package com.guanwei.auth.config;

import com.guanwei.framework.config.FrameworkProperties;
import com.guanwei.framework.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 认证服务安全配置
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class AuthServiceSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private FrameworkProperties frameworkProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置会话管理
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权
                .authorizeHttpRequests(auth -> {
                    // 放行的路径
                    List<String> permitAllPaths = Arrays.asList(
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/validate",
                            "/oauth2/**",
                            "/doc.html",
                            "/webjars/**",
                            "/swagger-resources/**",
                            "/v3/api-docs/**",
                            "/actuator/**",
                            "/auth/api/auth/login",
                            "/auth/api/auth/register",
                            "/auth/api/auth/validate",
                            "/auth/oauth2/**",
                            "/auth/doc.html",
                            "/auth/webjars/**",
                            "/auth/swagger-resources/**",
                            "/auth/v3/api-docs/**",
                            "/auth/actuator/**"
                    );
                    auth.requestMatchers(permitAllPaths.toArray(new String[0])).permitAll();
                    // 其他请求需要认证
                    auth.anyRequest().authenticated();
                })
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(frameworkProperties.getSecurity().getCors().getAllowedOrigins());
        configuration.setAllowedMethods(frameworkProperties.getSecurity().getCors().getAllowedMethods());
        configuration.setAllowedHeaders(frameworkProperties.getSecurity().getCors().getAllowedHeaders());
        configuration.setAllowCredentials(frameworkProperties.getSecurity().getCors().isAllowCredentials());
        configuration.setMaxAge(frameworkProperties.getSecurity().getCors().getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
