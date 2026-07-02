package com.jeestudio.tools.base.exceptions;

/**
 * @Description: 业务异常
 */
public class BusinessException extends RuntimeException{

    private String errorCode;
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String errorCode,String message) {
        super(message);
        this.errorCode=errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
