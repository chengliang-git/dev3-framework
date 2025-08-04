package com.guanwei.framework.cap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * CAP 消息实体类
 * 参考 .NET Core CAP 组件的消息结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CapMessage {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 消息名称/主题
     */
    private String name;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息组
     */
    private String group;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 重试次数
     */
    private Integer retries;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 消息头信息
     */
    private Map<String, String> headers;

    /**
     * 回调名称
     */
    private String callbackName;

    /**
     * 关联ID
     */
    private String correlationId;

    /**
     * 关联序列号
     */
    private Integer correlationSequence;

    /**
     * 执行实例ID
     */
    private String executionInstanceId;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentTime;

    /**
     * 延迟时间（秒）
     */
    private Long delayTime;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 消息类型
     */
    private MessageType messageType;

    /**
     * 消息状态枚举
     */
    public enum MessageStatus {
        PENDING, // 待处理
        SUCCEEDED, // 成功
        FAILED, // 失败
        RETRYING // 重试中
    }

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        NORMAL, // 普通消息
        DELAY, // 延迟消息
        TRANSACTIONAL // 事务性消息
    }

    /**
     * 消息头常量
     */
    public static class Headers {
        public static final String MESSAGE_ID = "cap-msg-id";
        public static final String MESSAGE_NAME = "cap-msg-name";
        public static final String GROUP = "cap-msg-group";
        public static final String TYPE = "cap-msg-type";
        public static final String CORRELATION_ID = "cap-corr-id";
        public static final String CORRELATION_SEQUENCE = "cap-corr-seq";
        public static final String CALLBACK_NAME = "cap-callback-name";
        public static final String EXECUTION_INSTANCE_ID = "cap-exec-instance-id";
        public static final String SENT_TIME = "cap-senttime";
        public static final String DELAY_TIME = "cap-delaytime";
        public static final String EXCEPTION = "cap-exception";
        public static final String TRACE_PARENT = "traceparent";
    }

    /**
     * 获取消息ID
     */
    public String getId() {
        if (id != null) {
            return id;
        }
        if (headers != null && headers.containsKey(Headers.MESSAGE_ID)) {
            return headers.get(Headers.MESSAGE_ID);
        }
        return null;
    }

    /**
     * 获取消息名称
     */
    public String getName() {
        if (name != null) {
            return name;
        }
        if (headers != null && headers.containsKey(Headers.MESSAGE_NAME)) {
            return headers.get(Headers.MESSAGE_NAME);
        }
        return null;
    }

    /**
     * 获取消息组
     */
    public String getGroup() {
        if (group != null) {
            return group;
        }
        if (headers != null && headers.containsKey(Headers.GROUP)) {
            return headers.get(Headers.GROUP);
        }
        return null;
    }

    /**
     * 获取回调名称
     */
    public String getCallbackName() {
        if (callbackName != null) {
            return callbackName;
        }
        if (headers != null && headers.containsKey(Headers.CALLBACK_NAME)) {
            return headers.get(Headers.CALLBACK_NAME);
        }
        return null;
    }

    /**
     * 获取关联ID
     */
    public String getCorrelationId() {
        if (correlationId != null) {
            return correlationId;
        }
        if (headers != null && headers.containsKey(Headers.CORRELATION_ID)) {
            return headers.get(Headers.CORRELATION_ID);
        }
        return null;
    }

    /**
     * 获取关联序列号
     */
    public Integer getCorrelationSequence() {
        if (correlationSequence != null) {
            return correlationSequence;
        }
        if (headers != null && headers.containsKey(Headers.CORRELATION_SEQUENCE)) {
            try {
                return Integer.parseInt(headers.get(Headers.CORRELATION_SEQUENCE));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 获取执行实例ID
     */
    public String getExecutionInstanceId() {
        if (executionInstanceId != null) {
            return executionInstanceId;
        }
        if (headers != null && headers.containsKey(Headers.EXECUTION_INSTANCE_ID)) {
            return headers.get(Headers.EXECUTION_INSTANCE_ID);
        }
        return null;
    }

    /**
     * 检查是否有异常
     */
    public boolean hasException() {
        return exception != null || (headers != null && headers.containsKey(Headers.EXCEPTION));
    }

    /**
     * 添加或更新异常信息
     */
    public void addOrUpdateException(Exception ex) {
        String msg = ex.getClass().getSimpleName() + "-->" + ex.getMessage();
        this.exception = msg;
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(Headers.EXCEPTION, msg);
    }

    /**
     * 移除异常信息
     */
    public void removeException() {
        this.exception = null;
        if (headers != null) {
            headers.remove(Headers.EXCEPTION);
        }
    }

    /**
     * 初始化消息头
     */
    public void initializeHeaders() {
        if (headers == null) {
            headers = new HashMap<>();
        }

        if (id != null) {
            headers.put(Headers.MESSAGE_ID, id);
        }
        if (name != null) {
            headers.put(Headers.MESSAGE_NAME, name);
        }
        if (group != null) {
            headers.put(Headers.GROUP, group);
        }
        if (callbackName != null) {
            headers.put(Headers.CALLBACK_NAME, callbackName);
        }
        if (correlationId != null) {
            headers.put(Headers.CORRELATION_ID, correlationId);
        }
        if (correlationSequence != null) {
            headers.put(Headers.CORRELATION_SEQUENCE, correlationSequence.toString());
        }
        if (executionInstanceId != null) {
            headers.put(Headers.EXECUTION_INSTANCE_ID, executionInstanceId);
        }
        if (sentTime != null) {
            headers.put(Headers.SENT_TIME, sentTime.toString());
        }
        if (delayTime != null) {
            headers.put(Headers.DELAY_TIME, delayTime.toString());
        }
        if (exception != null) {
            headers.put(Headers.EXCEPTION, exception);
        }
        if (messageType != null) {
            headers.put(Headers.TYPE, messageType.name());
        }
    }
}