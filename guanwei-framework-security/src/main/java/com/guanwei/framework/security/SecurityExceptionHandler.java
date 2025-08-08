package com.guanwei.framework.security;

import com.guanwei.framework.common.result.Result;
import com.guanwei.framework.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 统一安全异常 JSON 返回
 */
@Configuration
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        writeJson(response, Result.error(ResultCode.UNAUTHORIZED.getCode(), "未认证或凭证无效"));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        writeJson(response, Result.error(ResultCode.FORBIDDEN.getCode(), "权限不足"));
    }

    private void writeJson(HttpServletResponse response, Result<?> body) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = String.format("{\"code\":%d,\"message\":\"%s\",\"timestamp\":%d}", body.getCode(), body.getMessage(), System.currentTimeMillis());
        response.getWriter().write(json);
    }
}


