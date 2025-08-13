package com.guanwei.framework.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 基础集成测试类
 * 提供通用的测试功能和工具方法
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 执行GET请求
     */
    protected Result performGet(String url) throws Exception {
        String response = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Result.class);
    }

    /**
     * 执行POST请求
     */
    protected Result performPost(String url, Object requestBody) throws Exception {
        String response = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Result.class);
    }

    /**
     * 执行PUT请求
     */
    protected Result performPut(String url, Object requestBody) throws Exception {
        String response = mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Result.class);
    }

    /**
     * 执行DELETE请求
     */
    protected Result performDelete(String url) throws Exception {
        String response = mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Result.class);
    }

    /**
     * 执行带认证的GET请求
     */
    protected Result performGetWithAuth(String url, String token) throws Exception {
        String response = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Result.class);
    }

    /**
     * 执行带认证的POST请求
     */
    protected Result performPostWithAuth(String url, Object requestBody, String token) throws Exception {
        String response = mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Result.class);
    }

    /**
     * 验证响应成功
     */
    protected void assertSuccess(Result result) {
        assert result != null;
        assert result.getCode() == 200 : "Expected success but got: " + result.getCode();
    }

    /**
     * 验证响应失败
     */
    protected void assertFailure(Result result) {
        assert result != null;
        assert result.getCode() != 200 : "Expected failure but got success";
    }

    /**
     * 验证响应码
     */
    protected void assertCode(Result result, int expectedCode) {
        assert result != null;
        assert result.getCode() == expectedCode : 
            "Expected code " + expectedCode + " but got: " + result.getCode();
    }

    /**
     * 验证响应消息
     */
    protected void assertMessage(Result result, String expectedMessage) {
        assert result != null;
        assert result.getMessage() != null;
        assert result.getMessage().contains(expectedMessage) : 
            "Expected message containing '" + expectedMessage + "' but got: " + result.getMessage();
    }

    /**
     * 验证响应数据不为空
     */
    protected void assertDataNotNull(Result result) {
        assert result != null;
        assert result.getData() != null : "Expected data not null";
    }

    /**
     * 验证响应数据为空
     */
    protected void assertDataNull(Result result) {
        assert result != null;
        assert result.getData() == null : "Expected data null but got: " + result.getData();
    }
}
