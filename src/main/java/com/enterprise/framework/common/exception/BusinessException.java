package com.enterprise.framework.common.exception;

import com.enterprise.framework.common.result.ResultCode;

/**
 * 业务异常类
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}