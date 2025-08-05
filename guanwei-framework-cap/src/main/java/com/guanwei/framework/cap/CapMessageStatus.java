package com.guanwei.framework.cap;

/**
 * CAP 消息状态枚举
 * 参考 .NET Core CAP 的 StatusName 枚举
 */
public enum CapMessageStatus {
    /**
     * 失败状态
     */
    FAILED(-1),

    /**
     * 已调度状态
     */
    SCHEDULED(0),

    /**
     * 成功状态
     */
    SUCCEEDED(1),

    /**
     * 延迟状态
     */
    DELAYED(2),

    /**
     * 队列中状态
     */
    QUEUED(3);

    private final int value;

    CapMessageStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CapMessageStatus fromValue(int value) {
        for (CapMessageStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status value: " + value);
    }
} 