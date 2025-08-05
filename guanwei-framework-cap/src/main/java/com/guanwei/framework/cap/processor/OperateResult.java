package com.guanwei.framework.cap.processor;

import lombok.Data;

/**
 * CAP 操作结果
 * 参考 .NET Core CAP 的 OperateResult 类
 */
@Data
public class OperateResult {

    /**
     * 是否成功
     */
    private boolean succeeded;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 异常
     */
    private Exception exception;

    /**
     * 构造函数
     */
    public OperateResult() {
    }

    /**
     * 构造函数
     *
     * @param succeeded 是否成功
     * @param error     错误信息
     * @param exception 异常
     */
    public OperateResult(boolean succeeded, String error, Exception exception) {
        this.succeeded = succeeded;
        this.error = error;
        this.exception = exception;
    }

    /**
     * 创建成功结果
     *
     * @return 成功结果
     */
    public static OperateResult success() {
        return new OperateResult(true, null, null);
    }

    /**
     * 创建失败结果
     *
     * @param error 错误信息
     * @return 失败结果
     */
    public static OperateResult failed(String error) {
        return new OperateResult(false, error, null);
    }

    /**
     * 创建失败结果
     *
     * @param exception 异常
     * @return 失败结果
     */
    public static OperateResult failed(Exception exception) {
        return new OperateResult(false, exception.getMessage(), exception);
    }

    /**
     * 创建失败结果
     *
     * @param error     错误信息
     * @param exception 异常
     * @return 失败结果
     */
    public static OperateResult failed(String error, Exception exception) {
        return new OperateResult(false, error, exception);
    }

    @Override
    public String toString() {
        if (succeeded) {
            return "OperateResult{success}";
        } else {
            return "OperateResult{failed, error='" + error + "', exception=" + exception + "}";
        }
    }
} 