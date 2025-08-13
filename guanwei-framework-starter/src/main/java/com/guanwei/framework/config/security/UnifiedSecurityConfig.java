package com.guanwei.framework.config.security;

import com.guanwei.framework.config.FrameworkProperties;
import com.guanwei.framework.security.JwtAuthenticationFilter;
// import com.guanwei.framework.security.SecurityExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

/**
 * 统一安全配置
 * 集中管理所有安全相关配置，避免重复配置
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnClass(org.springframework.security.config.annotation.web.builders.HttpSecurity.class)
public class UnifiedSecurityConfig {

    private final FrameworkProperties frameworkProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // private final SecurityExceptionHandler securityExceptionHandler;

    @Autowired
    public UnifiedSecurityConfig(FrameworkProperties frameworkProperties,
                               JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.frameworkProperties = frameworkProperties;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        // this.securityExceptionHandler = securityExceptionHandler;
    }

    /**
     * 密码编码器
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤器链
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "securityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring unified security filter chain");
        
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置会话管理
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权
                .authorizeHttpRequests(auth -> {
                    // 获取配置的放行路径
                    String[] permitAllPaths = frameworkProperties.getSecurity().getPermitAllPaths().toArray(new String[0]);
                    auth.requestMatchers(permitAllPaths).permitAll();
                    
                    // 其他请求需要认证
                    auth.anyRequest().authenticated();
                })
                // 统一异常处理
                // 暂时注释掉，等待SecurityExceptionHandler可用
                /*
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityExceptionHandler)
                        .accessDeniedHandler(securityExceptionHandler)
                )
                */
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security filter chain configured with {} permit-all paths", 
                frameworkProperties.getSecurity().getPermitAllPaths().size());
        
        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    public CorsConfigurationSource corsConfigurationSource() {
        FrameworkProperties.Cors corsConfig = frameworkProperties.getSecurity().getCors();
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsConfig.getAllowedOrigins());
        configuration.setAllowedMethods(corsConfig.getAllowedMethods());
        configuration.setAllowedHeaders(corsConfig.getAllowedHeaders());
        configuration.setAllowCredentials(corsConfig.isAllowCredentials());
        configuration.setMaxAge(corsConfig.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS configuration initialized with {} allowed origins", 
                corsConfig.getAllowedOrigins().size());
        
        return source;
    }
}
