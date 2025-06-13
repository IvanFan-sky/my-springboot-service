package com.spark.demo.common.exception;

import com.spark.demo.common.result.ResultCode;

/**
 * 自定义业务异常类
 */
public class BusinessException extends RuntimeException {
    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}