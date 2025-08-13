package com.guanwei.framework.config.security;

import com.guanwei.framework.config.FrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * API安全配置
 * 提供API签名验证、防重放攻击、访问频率限制等功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.security.api", name = "enabled", havingValue = "true")
public class ApiSecurityConfig {

    private final FrameworkProperties frameworkProperties;
    private final RedisTemplate<String, String> redisTemplate;

    public ApiSecurityConfig(FrameworkProperties frameworkProperties, RedisTemplate<String, String> redisTemplate) {
        this.frameworkProperties = frameworkProperties;
        this.redisTemplate = redisTemplate;
    }

    /**
     * API安全过滤器
     */
    @Bean
    public ApiSecurityFilter apiSecurityFilter() {
        return new ApiSecurityFilter();
    }

    /**
     * API安全过滤器实现
     */
    public class ApiSecurityFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            // 跳过不需要验证的路径
            if (shouldSkipSecurity(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            try {
                // 1. 验证时间戳防重放攻击
                if (!validateTimestamp(request)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"message\":\"请求已过期\"}");
                    return;
                }

                // 2. 验证API签名
                if (!validateSignature(request)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"message\":\"签名验证失败\"}");
                    return;
                }

                // 3. 检查访问频率限制
                if (!checkRateLimit(request)) {
                    response.setStatus(429); // Too Many Requests
                    response.getWriter().write("{\"code\":429,\"message\":\"访问频率超限\"}");
                    return;
                }

                filterChain.doFilter(request, response);

            } catch (Exception e) {
                log.error("API安全验证异常", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"code\":500,\"message\":\"服务器内部错误\"}");
            }
        }

        /**
         * 判断是否需要跳过安全验证
         */
        private boolean shouldSkipSecurity(HttpServletRequest request) {
            String requestUri = request.getRequestURI();
            return frameworkProperties.getSecurity().getApi().getSkipPaths().stream()
                    .anyMatch(requestUri::startsWith);
        }

        /**
         * 验证时间戳防重放攻击
         */
        private boolean validateTimestamp(HttpServletRequest request) {
            String timestamp = request.getHeader("X-Timestamp");
            if (timestamp == null) {
                return false;
            }

            try {
                long requestTime = Long.parseLong(timestamp);
                long currentTime = Instant.now().getEpochSecond();
                long timeWindow = frameworkProperties.getSecurity().getApi().getTimeWindow();

                // 检查时间窗口
                if (Math.abs(currentTime - requestTime) > timeWindow) {
                    return false;
                }

                // 检查是否重复请求
                String nonce = request.getHeader("X-Nonce");
                if (nonce != null) {
                    String nonceKey = "api:nonce:" + nonce;
                    Boolean exists = redisTemplate.hasKey(nonceKey);
                    if (Boolean.TRUE.equals(exists)) {
                        return false; // 重复请求
                    }
                    // 记录nonce，设置过期时间
                    redisTemplate.opsForValue().set(nonceKey, "1", timeWindow, TimeUnit.SECONDS);
                }

                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        /**
         * 验证API签名
         */
        private boolean validateSignature(HttpServletRequest request) {
            String signature = request.getHeader("X-Signature");
            String appKey = request.getHeader("X-App-Key");
            
            if (signature == null || appKey == null) {
                return false;
            }

            // 获取应用密钥
            String appSecret = getAppSecret(appKey);
            if (appSecret == null) {
                return false;
            }

            // 构建签名字符串
            String signString = buildSignString(request, appSecret);
            
            // 计算签名
            String expectedSignature = calculateSignature(signString);
            
            return signature.equals(expectedSignature);
        }

        /**
         * 构建签名字符串
         */
        private String buildSignString(HttpServletRequest request, String appSecret) {
            StringBuilder sb = new StringBuilder();
            
            // 添加请求方法
            sb.append(request.getMethod()).append("&");
            
            // 添加请求路径
            sb.append(request.getRequestURI()).append("&");
            
            // 添加时间戳
            String timestamp = request.getHeader("X-Timestamp");
            sb.append(timestamp).append("&");
            
            // 添加随机数
            String nonce = request.getHeader("X-Nonce");
            sb.append(nonce).append("&");
            
            // 添加应用密钥
            sb.append(appSecret);
            
            return sb.toString();
        }

        /**
         * 计算签名
         */
        private String calculateSignature(String signString) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(signString.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                log.error("计算签名失败", e);
                return "";
            }
        }

        /**
         * 获取应用密钥
         */
        private String getAppSecret(String appKey) {
            // 这里应该从数据库或配置中获取应用密钥
            // 暂时使用配置中的默认密钥
            return frameworkProperties.getSecurity().getApi().getDefaultAppSecret();
        }

        /**
         * 检查访问频率限制
         */
        private boolean checkRateLimit(HttpServletRequest request) {
            String appKey = request.getHeader("X-App-Key");
            if (appKey == null) {
                return false;
            }

            String rateLimitKey = "api:rate_limit:" + appKey;
            String currentCount = redisTemplate.opsForValue().get(rateLimitKey);
            
            int count = currentCount == null ? 0 : Integer.parseInt(currentCount);
            int maxRequests = frameworkProperties.getSecurity().getApi().getMaxRequestsPerMinute();
            
            if (count >= maxRequests) {
                return false;
            }

            // 增加计数
            if (count == 0) {
                redisTemplate.opsForValue().set(rateLimitKey, "1", 1, TimeUnit.MINUTES);
            } else {
                redisTemplate.opsForValue().increment(rateLimitKey);
            }

            return true;
        }
    }
}
