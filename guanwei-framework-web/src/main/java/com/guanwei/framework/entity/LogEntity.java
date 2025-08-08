package com.guanwei.framework.entity;

import com.guanwei.framework.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 日志实体（示例）
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Schema(description = "日志实体")
public class LogEntity extends BaseEntity {

    @Schema(description = "日志级别")
    private String level;

    @Schema(description = "日志消息")
    private String message;

    @Schema(description = "类名")
    private String className;

    @Schema(description = "方法名")
    private String methodName;

    @Schema(description = "行号")
    private Integer lineNumber;

    @Schema(description = "线程名")
    private String threadName;

    @Schema(description = "请求IP")
    private String requestIp;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "执行时间（毫秒）")
    private Long executionTime;

    @Schema(description = "异常信息")
    private String exception;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}