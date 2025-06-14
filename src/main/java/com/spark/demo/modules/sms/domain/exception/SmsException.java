package com.spark.demo.modules.sms.domain.exception;

import com.spark.demo.common.exception.ErrorCode;

/**
 * 短信模块异常类
 * @author spark
 * @date 2025-06-14
 */
public class SmsException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public SmsException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    public SmsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public SmsException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    public SmsException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }

    // ========== 静态工厂方法 ==========

    public static SmsException sendFailed() {
        return new SmsException(ErrorCode.SMS_SEND_FAILED);
    }

    public static SmsException sendFailed(String message) {
        return new SmsException(ErrorCode.SMS_SEND_FAILED, message);
    }

    public static SmsException codeInvalid() {
        return new SmsException(ErrorCode.SMS_CODE_INVALID);
    }

    public static SmsException codeExpired() {
        return new SmsException(ErrorCode.SMS_CODE_EXPIRED);
    }

    public static SmsException codeError() {
        return new SmsException(ErrorCode.SMS_CODE_ERROR);
    }

    public static SmsException sendTooFrequent() {
        return new SmsException(ErrorCode.SMS_SEND_TOO_FREQUENT);
    }

    public static SmsException sendTooFrequent(long remainingSeconds) {
        return new SmsException(ErrorCode.SMS_SEND_TOO_FREQUENT, 
            "发送过于频繁，请" + remainingSeconds + "秒后再试");
    }

    public static SmsException phoneInvalid() {
        return new SmsException(ErrorCode.SMS_PHONE_INVALID);
    }

    public static SmsException phoneInvalid(String phone) {
        return new SmsException(ErrorCode.SMS_PHONE_INVALID, "手机号格式不正确: " + phone);
    }

    public static SmsException templateNotFound() {
        return new SmsException(ErrorCode.SMS_TEMPLATE_NOT_FOUND);
    }

    public static SmsException quotaExceeded() {
        return new SmsException(ErrorCode.SMS_QUOTA_EXCEEDED);
    }
} 