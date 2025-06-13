package com.spark.demo.common.exception;

import com.spark.demo.common.context.UserContext;
import com.spark.demo.common.result.Result;
import com.spark.demo.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * 全局异常处理器
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        String requestInfo = getRequestInfo();
        log.warn("业务异常: {}, 请求信息: {}", e.getMessage(), requestInfo);
        return Result.fail(e.getResultCode().getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 - @Valid 注解校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String requestInfo = getRequestInfo();
        
        StringBuilder errorMsg = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error : fieldErrors) {
            errorMsg.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        }
        
        String message = errorMsg.toString();
        log.warn("参数校验失败: {}, 请求信息: {}", message, requestInfo);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), "参数校验失败: " + message);
    }

    /**
     * 参数绑定异常 - @ModelAttribute 校验失败
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String requestInfo = getRequestInfo();
        
        StringBuilder errorMsg = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error : fieldErrors) {
            errorMsg.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        }
        
        String message = errorMsg.toString();
        log.warn("参数绑定失败: {}, 请求信息: {}", message, requestInfo);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), "参数校验失败: " + message);
    }

    /**
     * 路径变量校验异常 - @Validated 注解校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String requestInfo = getRequestInfo();
        
        StringBuilder errorMsg = new StringBuilder();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            errorMsg.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
        }
        
        String message = errorMsg.toString();
        log.warn("路径变量校验失败: {}, 请求信息: {}", message, requestInfo);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), "参数校验失败: " + message);
    }

    /**
     * 缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String requestInfo = getRequestInfo();
        String message = "缺少必需参数: " + e.getParameterName();
        log.warn("缺少请求参数: {}, 请求信息: {}", message, requestInfo);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    /**
     * 参数类型转换异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String requestInfo = getRequestInfo();
        String message = "参数类型错误: " + e.getName() + ", 期望类型: " + e.getRequiredType().getSimpleName();
        log.warn("参数类型转换异常: {}, 请求信息: {}", message, requestInfo);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    /**
     * 请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String requestInfo = getRequestInfo();
        String message = "不支持的请求方法: " + e.getMethod();
        log.warn("请求方法不支持: {}, 请求信息: {}", message, requestInfo);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    /**
     * 404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        String requestInfo = getRequestInfo();
        String message = "请求路径不存在: " + e.getRequestURL();
        log.warn("404异常: {}, 请求信息: {}", message, requestInfo);
        return Result.fail(ResultCode.NOT_FOUND.getCode(), "请求的资源不存在");
    }

    /**
     * 数据库唯一约束异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        String requestInfo = getRequestInfo();
        log.warn("数据库唯一约束异常, 请求信息: {}", requestInfo, e);
        
        String message = "数据已存在";
        String errorMsg = e.getMessage();
        if (errorMsg != null) {
            if (errorMsg.contains("username")) {
                message = "用户名已存在";
            } else if (errorMsg.contains("phone")) {
                message = "手机号已存在";
            } else if (errorMsg.contains("email")) {
                message = "邮箱已存在";
            } else if (errorMsg.contains("uuid")) {
                message = "用户标识已存在";
            }
        }
        
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    /**
     * 数据完整性异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String requestInfo = getRequestInfo();
        log.warn("数据完整性异常, 请求信息: {}", requestInfo, e);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), "数据操作失败，请检查数据完整性");
    }

    /**
     * SQL异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSQLException(SQLException e) {
        String requestInfo = getRequestInfo();
        log.error("SQL异常, 请求信息: {}", requestInfo, e);
        return Result.fail(ResultCode.FAIL.getCode(), "数据库操作失败");
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        String requestInfo = getRequestInfo();
        log.error("空指针异常, 请求信息: {}", requestInfo, e);
        return Result.fail(ResultCode.FAIL.getCode(), "系统内部错误");
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        String requestInfo = getRequestInfo();
        log.warn("非法参数异常: {}, 请求信息: {}", e.getMessage(), requestInfo);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), "参数错误: " + e.getMessage());
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        String requestInfo = getRequestInfo();
        log.error("系统异常, 请求信息: {}", requestInfo, e);
        return Result.fail(ResultCode.FAIL.getCode(), "系统内部错误，请稍后重试");
    }

    /**
     * 获取请求信息
     */
    private String getRequestInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                StringBuilder info = new StringBuilder();
                info.append("URI: ").append(request.getRequestURI());
                info.append(", Method: ").append(request.getMethod());
                info.append(", IP: ").append(getClientIpAddress(request));
                
                Long userId = UserContext.getCurrentUserId();
                if (userId != null) {
                    info.append(", UserId: ").append(userId);
                }
                
                return info.toString();
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }
        return "unknown";
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}