package com.spark.demo.modules.user.domain.exception;

import com.spark.demo.common.exception.ErrorCode;

/**
 * 用户模块异常类
 * @author spark
 * @date 2025-06-14
 */
public class UserException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public UserException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    public UserException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public UserException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    public UserException(ErrorCode errorCode, String message, Throwable cause) {
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

    public static UserException userNotFound() {
        return new UserException(ErrorCode.USER_NOT_FOUND);
    }

    public static UserException userNotFound(String identifier) {
        return new UserException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + identifier);
    }

    public static UserException userAlreadyExists() {
        return new UserException(ErrorCode.USER_ALREADY_EXISTS);
    }

    public static UserException usernameAlreadyExists(String username) {
        return new UserException(ErrorCode.USERNAME_ALREADY_EXISTS, "用户名已存在: " + username);
    }

    public static UserException phoneAlreadyExists(String phone) {
        return new UserException(ErrorCode.PHONE_ALREADY_EXISTS, "手机号已存在: " + phone);
    }

    public static UserException emailAlreadyExists(String email) {
        return new UserException(ErrorCode.EMAIL_ALREADY_EXISTS, "邮箱已存在: " + email);
    }

    public static UserException passwordError() {
        return new UserException(ErrorCode.PASSWORD_ERROR);
    }

    public static UserException oldPasswordError() {
        return new UserException(ErrorCode.OLD_PASSWORD_ERROR);
    }

    public static UserException passwordTooWeak() {
        return new UserException(ErrorCode.PASSWORD_TOO_WEAK);
    }

    public static UserException accountDisabled() {
        return new UserException(ErrorCode.ACCOUNT_DISABLED);
    }

    public static UserException userInfoIncomplete() {
        return new UserException(ErrorCode.USER_INFO_INCOMPLETE);
    }

    public static UserException userStatusInvalid(Integer status) {
        return new UserException(ErrorCode.USER_STATUS_INVALID, "无效的用户状态: " + status);
    }

    public static UserException userRoleInvalid(String role) {
        return new UserException(ErrorCode.USER_ROLE_INVALID, "无效的用户角色: " + role);
    }
} 