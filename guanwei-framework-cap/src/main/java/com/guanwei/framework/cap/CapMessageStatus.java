package com.guanwei.framework.cap;

/**
 * CAP 消息状态枚举
 * 参考 .NET Core CAP 的 StatusName 枚举
 */
public enum CapMessageStatus {
    /**
     * 失败状态
     */
    FAILED("Failed"),

    /**
     * 已调度状态
     */
    SCHEDULED("Scheduled"),

    /**
     * 成功状态
     */
    SUCCEEDED("Succeeded"),

    /**
     * 延迟状态
     */
    DELAYED("Delayed"),

    /**
     * 队列中状态
     */
    QUEUED("Queued"),

    /**
     * 重试中状态
     */
    RETRYING("Retrying"),

    /**
     * 待处理状态
     */
    PENDING("Pending");

    private final String value;

    CapMessageStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CapMessageStatus fromValue(String value) {
        for (CapMessageStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status value: " + value);
    }
} 