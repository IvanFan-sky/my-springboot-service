package com.spark.demo.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一API响应结果封装
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> implements Serializable {
    // 状态码
    private Integer code;
    // 消息
    private String msg;
    // 数据
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        return result;
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（默认错误码）
     */
    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.FAIL.getCode());
        result.setMsg(ResultCode.FAIL.getMsg());
        return result;
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> fail(String message) {
        Result<T> result = fail();
        result.setMsg(message);
        return result;
    }

    /**
     * 失败响应（自定义错误码和消息）
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        return result;
    }
}