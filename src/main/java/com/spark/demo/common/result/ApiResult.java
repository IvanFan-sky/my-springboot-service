package com.spark.demo.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.spark.demo.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一API响应结果封装（新版本）
 * 提供更规范的接口返回格式
 * @param <T> 响应数据类型
 * @author spark
 * @date 2025-06-14
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一API响应结果")
public class ApiResult<T> implements Serializable {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "响应时间戳", example = "2025-06-14 12:00:00")
    private String timestamp;

    @Schema(description = "请求追踪ID", example = "abc123")
    private String traceId;

    @Schema(description = "API版本", example = "v1")
    private String version;

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 私有构造函数
     */
    private ApiResult() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.version = "v1";
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResult<T> success() {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        result.setSuccess(true);
        return result;
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = success();
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResult<T> success(String message) {
        ApiResult<T> result = success();
        result.setMessage(message);
        return result;
    }

    /**
     * 成功响应（带数据和自定义消息）
     */
    public static <T> ApiResult<T> success(T data, String message) {
        ApiResult<T> result = success();
        result.setData(data);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（使用ErrorCode）
     */
    public static <T> ApiResult<T> fail(ErrorCode errorCode) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        result.setSuccess(false);
        return result;
    }

    /**
     * 失败响应（使用ErrorCode和自定义消息）
     */
    public static <T> ApiResult<T> fail(ErrorCode errorCode, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(errorCode.getCode());
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    /**
     * 失败响应（自定义错误码和消息）
     */
    public static <T> ApiResult<T> fail(Integer code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    /**
     * 失败响应（默认系统错误）
     */
    public static <T> ApiResult<T> fail() {
        return fail(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 失败响应（自定义消息，使用系统错误码）
     */
    public static <T> ApiResult<T> fail(String message) {
        return fail(ErrorCode.SYSTEM_ERROR, message);
    }

    /**
     * 设置追踪ID
     */
    public ApiResult<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 设置版本
     */
    public ApiResult<T> version(String version) {
        this.version = version;
        return this;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success) && code != null && code.equals(ErrorCode.SUCCESS.getCode());
    }

    /**
     * 判断是否失败
     */
    public boolean isFail() {
        return !isSuccess();
    }
} 