package com.guanwei.framework.cap;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * CAP 消息实体类
 * 参考 .NET Core CAP 组件的消息结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private String headers;

    /**
     * 消息状态枚举
     */
    public enum MessageStatus {
        PENDING, // 待处理
        SUCCEEDED, // 成功
        FAILED, // 失败
        RETRYING // 重试中
    }
}