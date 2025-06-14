package com.spark.demo.common.exception;

/**
 * 详细错误码枚举
 * 按业务模块分类，便于维护和定位问题
 * @author spark
 * @date 2025-06-14
 */
public enum ErrorCode {

    // ========== 通用错误码 (1000-1999) ==========
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(1000, "系统内部错误"),
    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "缺少必要参数"),
    PARAM_INVALID(1003, "参数格式不正确"),
    REQUEST_METHOD_NOT_SUPPORTED(1004, "请求方法不支持"),
    MEDIA_TYPE_NOT_SUPPORTED(1005, "媒体类型不支持"),
    REQUEST_TIMEOUT(1006, "请求超时"),
    TOO_MANY_REQUESTS(1007, "请求过于频繁"),

    // ========== 认证授权错误码 (2000-2999) ==========
    UNAUTHORIZED(2000, "未登录或登录已过期"),
    ACCESS_DENIED(2001, "权限不足"),
    TOKEN_INVALID(2002, "Token无效"),
    TOKEN_EXPIRED(2003, "Token已过期"),
    LOGIN_FAILED(2004, "登录失败"),
    PASSWORD_ERROR(2005, "密码错误"),
    ACCOUNT_DISABLED(2006, "账户已被禁用"),
    ACCOUNT_LOCKED(2007, "账户已被锁定"),
    PERMISSION_DENIED(2008, "无权限访问"),

    // ========== 用户相关错误码 (3000-3999) ==========
    USER_NOT_FOUND(3000, "用户不存在"),
    USER_ALREADY_EXISTS(3001, "用户已存在"),
    USERNAME_ALREADY_EXISTS(3002, "用户名已存在"),
    PHONE_ALREADY_EXISTS(3003, "手机号已存在"),
    EMAIL_ALREADY_EXISTS(3004, "邮箱已存在"),
    USER_INFO_INCOMPLETE(3005, "用户信息不完整"),
    PASSWORD_TOO_WEAK(3006, "密码强度不够"),
    OLD_PASSWORD_ERROR(3007, "原密码错误"),
    USER_STATUS_INVALID(3008, "用户状态无效"),
    USER_ROLE_INVALID(3009, "用户角色无效"),

    // ========== 短信相关错误码 (4000-4999) ==========
    SMS_SEND_FAILED(4000, "短信发送失败"),
    SMS_CODE_INVALID(4001, "验证码无效"),
    SMS_CODE_EXPIRED(4002, "验证码已过期"),
    SMS_CODE_ERROR(4003, "验证码错误"),
    SMS_SEND_TOO_FREQUENT(4004, "短信发送过于频繁"),
    SMS_PHONE_INVALID(4005, "手机号格式不正确"),
    SMS_TEMPLATE_NOT_FOUND(4006, "短信模板不存在"),
    SMS_QUOTA_EXCEEDED(4007, "短信发送次数超限"),

    // ========== 数据库相关错误码 (5000-5999) ==========
    DATABASE_ERROR(5000, "数据库操作失败"),
    DATA_NOT_FOUND(5001, "数据不存在"),
    DATA_ALREADY_EXISTS(5002, "数据已存在"),
    DATA_INTEGRITY_VIOLATION(5003, "数据完整性约束违反"),
    OPTIMISTIC_LOCK_FAILURE(5004, "数据已被其他用户修改"),
    DATABASE_CONNECTION_ERROR(5005, "数据库连接失败"),

    // ========== 缓存相关错误码 (6000-6999) ==========
    CACHE_ERROR(6000, "缓存操作失败"),
    CACHE_KEY_NOT_FOUND(6001, "缓存键不存在"),
    CACHE_CONNECTION_ERROR(6002, "缓存连接失败"),
    CACHE_SERIALIZATION_ERROR(6003, "缓存序列化失败"),

    // ========== 文件相关错误码 (7000-7999) ==========
    FILE_UPLOAD_FAILED(7000, "文件上传失败"),
    FILE_NOT_FOUND(7001, "文件不存在"),
    FILE_SIZE_EXCEEDED(7002, "文件大小超限"),
    FILE_TYPE_NOT_SUPPORTED(7003, "文件类型不支持"),
    FILE_DOWNLOAD_FAILED(7004, "文件下载失败"),

    // ========== 第三方服务错误码 (8000-8999) ==========
    THIRD_PARTY_SERVICE_ERROR(8000, "第三方服务异常"),
    PAYMENT_FAILED(8001, "支付失败"),
    WECHAT_API_ERROR(8002, "微信接口异常"),
    ALIPAY_API_ERROR(8003, "支付宝接口异常"),

    // ========== 业务逻辑错误码 (9000-9999) ==========
    BUSINESS_ERROR(9000, "业务处理失败"),
    OPERATION_NOT_ALLOWED(9001, "操作不被允许"),
    STATUS_CONFLICT(9002, "状态冲突"),
    RESOURCE_LOCKED(9003, "资源被锁定"),
    QUOTA_EXCEEDED(9004, "配额已用完"),
    TIME_WINDOW_INVALID(9005, "时间窗口无效");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举
     * @param code 错误码
     * @return 错误码枚举
     */
    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }

    /**
     * 判断是否为成功状态
     * @return 是否成功
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为客户端错误（4xx）
     * @return 是否为客户端错误
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * 判断是否为服务器错误（5xx）
     * @return 是否为服务器错误
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }
} 