package com.guanwei.framework.cap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * CAP 消息实体
 * 参考 .NET Core CAP 的 MediumMessage 类
 */
@Data
public class CapMessage {

    /**
     * 数据库ID
     */
    private String dbId;

    /**
     * 消息名称
     */
    private String name;

    /**
     * 消息组
     */
    private String group;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息状态
     */
    private CapMessageStatus status;

    /**
     * 重试次数
     */
    private int retries;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 添加时间
     */
    private LocalDateTime added;

    /**
     * 版本
     */
    private String version;

    /**
     * 消息头
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 原始消息对象
     */
    @JsonIgnore
    private Object origin;

    /**
     * 构造函数
     */
    public CapMessage() {
        this.added = LocalDateTime.now();
        this.retries = 0;
        this.status = CapMessageStatus.SCHEDULED;
    }

    /**
     * 构造函数
     *
     * @param name    消息名称
     * @param content 消息内容
     */
    public CapMessage(String name, Object content) {
        this();
        this.name = name;
        this.content = content instanceof String ? (String) content : content.toString();
        this.origin = content;
    }

    /**
     * 构造函数
     *
     * @param name    消息名称
     * @param group   消息组
     * @param content 消息内容
     */
    public CapMessage(String name, String group, Object content) {
        this(name, content);
        this.group = group;
    }

    /**
     * 获取消息ID
     *
     * @return 消息ID
     */
    public String getId() {
        return dbId;
    }

    /**
     * 获取消息名称
     *
     * @return 消息名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取消息组
     *
     * @return 消息组
     */
    public String getGroup() {
        return group;
    }

    /**
     * 获取消息内容
     *
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取消息头
     *
     * @param key 键
     * @return 值
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * 设置消息头
     *
     * @param key   键
     * @param value 值
     */
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * 获取消息头映射
     *
     * @return 消息头映射
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 设置消息头映射
     *
     * @param headers 消息头映射
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * 获取原始消息对象
     *
     * @return 原始消息对象
     */
    public Object getOrigin() {
        return origin;
    }

    /**
     * 设置原始消息对象
     *
     * @param origin 原始消息对象
     */
    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetries() {
        this.retries++;
    }

    /**
     * 检查是否过期
     *
     * @return 是否过期
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 检查是否可以重试
     *
     * @param maxRetries 最大重试次数
     * @return 是否可以重试
     */
    public boolean canRetry(int maxRetries) {
        return retries < maxRetries;
    }
}