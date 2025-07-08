package com.enterprise.framework.config;

import com.enterprise.framework.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SecurityProperties securityProperties;

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
                String[] permitAllPaths = securityProperties.getPermitAllPaths().toArray(new String[0]);
                auth.requestMatchers(permitAllPaths).permitAll();
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
        configuration.setAllowedOriginPatterns(securityProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(securityProperties.getCors().getAllowedMethods());
        configuration.setAllowedHeaders(securityProperties.getCors().getAllowedHeaders());
        configuration.setAllowCredentials(securityProperties.getCors().isAllowCredentials());
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 安全配置属性
     */
    @Component
    @ConfigurationProperties(prefix = "framework.security")
    public static class SecurityProperties {
        private List<String> permitAllPaths = Arrays.asList(
                "/api/auth/login",
                "/api/auth/register",
                "/druid/**",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/actuator/**"
        );
        
        private CorsProperties cors = new CorsProperties();

        public List<String> getPermitAllPaths() {
            return permitAllPaths;
        }

        public void setPermitAllPaths(List<String> permitAllPaths) {
            this.permitAllPaths = permitAllPaths;
        }

        public CorsProperties getCors() {
            return cors;
        }

        public void setCors(CorsProperties cors) {
            this.cors = cors;
        }

        public static class CorsProperties {
            private List<String> allowedOrigins = List.of("*");
            private List<String> allowedMethods = List.of("*");
            private List<String> allowedHeaders = List.of("*");
            private boolean allowCredentials = true;

            public List<String> getAllowedOrigins() {
                return allowedOrigins;
            }

            public void setAllowedOrigins(List<String> allowedOrigins) {
                this.allowedOrigins = allowedOrigins;
            }

            public List<String> getAllowedMethods() {
                return allowedMethods;
            }

            public void setAllowedMethods(List<String> allowedMethods) {
                this.allowedMethods = allowedMethods;
            }

            public List<String> getAllowedHeaders() {
                return allowedHeaders;
            }

            public void setAllowedHeaders(List<String> allowedHeaders) {
                this.allowedHeaders = allowedHeaders;
            }

            public boolean isAllowCredentials() {
                return allowCredentials;
            }

            public void setAllowCredentials(boolean allowCredentials) {
                this.allowCredentials = allowCredentials;
            }
        }
    }
}