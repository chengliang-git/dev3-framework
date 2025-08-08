package com.guanwei.framework.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtTokenUtil.validateToken(token)) {
            String username = jwtTokenUtil.getUsername(token);

            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("用户 {} 认证成功", username);
                    }
                } catch (Exception e) {
                    log.warn("用户认证失败: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取Token
     *
     * @param request 请求对象
     * @return Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String headerValue = request.getHeader(jwtProperties.getHeader());
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }

        // 标准处理：兼容 "Bearer <token>" 或配置前缀大小写及是否带空格
        String configuredPrefix = jwtProperties.getPrefix();
        if (StringUtils.hasText(configuredPrefix)) {
            String normalizedHeader = headerValue.trim();
            String normalizedPrefix = configuredPrefix.trim();
            if (normalizedHeader.regionMatches(true, 0, normalizedPrefix, 0, normalizedPrefix.length())) {
                String tokenPart = normalizedHeader.substring(normalizedPrefix.length());
                return tokenPart.trim();
            }
        }

        // 兜底：按标准 Bearer 方案解析
        if (headerValue.regionMatches(true, 0, "Bearer", 0, "Bearer".length())) {
            String tokenPart = headerValue.substring("Bearer".length());
            return tokenPart.trim();
        }

        return null;
    }
}