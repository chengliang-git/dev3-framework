package com.guanwei.framework.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    @Test
    void testExtractTokenVariants() throws Exception {
        JwtProperties props = new JwtProperties();
        props.setHeader("Authorization");
        props.setPrefix("Bearer");

        JwtTokenUtil util = new JwtTokenUtil();
        // 不验证签名流程，仅测试解析逻辑是否不抛异常

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        // 通过反射注入属性（简化测试）
        java.lang.reflect.Field f1 = JwtAuthenticationFilter.class.getDeclaredField("jwtProperties");
        f1.setAccessible(true);
        f1.set(filter, props);
        java.lang.reflect.Field f2 = JwtAuthenticationFilter.class.getDeclaredField("jwtTokenUtil");
        f2.setAccessible(true);
        f2.set(filter, util);

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        req.addHeader("Authorization", "bearer   abc.def.ghi  ");
        assertDoesNotThrow(() -> filter.doFilter(req, res, chain));
    }
}


