package com.spark.demo.modules.auth.domain.exception;

import com.spark.demo.common.exception.ErrorCode;

/**
 * 认证模块异常类
 * @author spark
 * @date 2025-06-14
 */
public class AuthException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public AuthException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    public AuthException(ErrorCode errorCode, String message, Throwable cause) {
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

    public static AuthException unauthorized() {
        return new AuthException(ErrorCode.UNAUTHORIZED);
    }

    public static AuthException unauthorized(String message) {
        return new AuthException(ErrorCode.UNAUTHORIZED, message);
    }

    public static AuthException accessDenied() {
        return new AuthException(ErrorCode.ACCESS_DENIED);
    }

    public static AuthException accessDenied(String message) {
        return new AuthException(ErrorCode.ACCESS_DENIED, message);
    }

    public static AuthException loginFailed() {
        return new AuthException(ErrorCode.LOGIN_FAILED);
    }

    public static AuthException loginFailed(String message) {
        return new AuthException(ErrorCode.LOGIN_FAILED, message);
    }

    public static AuthException passwordError() {
        return new AuthException(ErrorCode.PASSWORD_ERROR);
    }

    public static AuthException accountDisabled() {
        return new AuthException(ErrorCode.ACCOUNT_DISABLED);
    }

    public static AuthException accountLocked() {
        return new AuthException(ErrorCode.ACCOUNT_LOCKED);
    }

    public static AuthException tokenInvalid() {
        return new AuthException(ErrorCode.TOKEN_INVALID);
    }

    public static AuthException tokenExpired() {
        return new AuthException(ErrorCode.TOKEN_EXPIRED);
    }

    public static AuthException permissionDenied() {
        return new AuthException(ErrorCode.PERMISSION_DENIED);
    }

    public static AuthException permissionDenied(String resource) {
        return new AuthException(ErrorCode.PERMISSION_DENIED, "无权限访问: " + resource);
    }
} 