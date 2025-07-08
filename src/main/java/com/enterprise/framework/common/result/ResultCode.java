package com.enterprise.framework.common.result;

/**
 * 统一响应状态码
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "资源冲突"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 服务器错误
    ERROR(500, "系统内部错误"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // 业务错误码 (6000-6999)
    BUSINESS_ERROR(6000, "业务处理失败"),
    VALIDATION_ERROR(6001, "参数校验失败"),
    DATA_NOT_FOUND(6002, "数据不存在"),
    DATA_ALREADY_EXISTS(6003, "数据已存在"),
    OPERATION_NOT_ALLOWED(6004, "操作不被允许"),

    // 认证授权错误码 (7000-7999)
    TOKEN_INVALID(7001, "Token无效"),
    TOKEN_EXPIRED(7002, "Token已过期"),
    USERNAME_PASSWORD_ERROR(7003, "用户名或密码错误"),
    ACCOUNT_LOCKED(7004, "账户已锁定"),
    ACCOUNT_DISABLED(7005, "账户已禁用"),
    PERMISSION_DENIED(7006, "权限不足"),
    
    // 系统错误码 (8000-8999)
    DATABASE_ERROR(8001, "数据库操作失败"),
    REDIS_ERROR(8002, "缓存操作失败"),
    MQ_ERROR(8003, "消息队列操作失败"),
    FILE_UPLOAD_ERROR(8004, "文件上传失败"),
    FILE_NOT_FOUND(8005, "文件不存在"),
    NETWORK_ERROR(8006, "网络连接失败"),
    CONFIG_ERROR(8007, "配置错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}