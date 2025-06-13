package com.spark.demo.common.result;

/**
 * 统一返回状态码枚举
 * @author spark
 * @date 2025-05-29
 */
public enum ResultCode {
    SUCCESS(200, "成功"),
    FAIL(500, "系统错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),
    BUSINESS_ERROR(600, "业务异常");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}