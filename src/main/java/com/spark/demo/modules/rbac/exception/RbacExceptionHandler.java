package com.spark.demo.modules.rbac.exception;

import com.spark.demo.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * RBAC权限异常处理器
 * 统一处理权限相关异常
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@RestControllerAdvice
@Order(1) // 确保在全局异常处理器之前执行
public class RbacExceptionHandler {

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handlePermissionDenied(PermissionDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.fail(403, e.getMessage());
    }

    /**
     * 处理角色不足异常
     */
    @ExceptionHandler(RoleDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleRoleDenied(RoleDeniedException e) {
        log.warn("角色不足: {}", e.getMessage());
        return Result.fail(403, e.getMessage());
    }

    /**
     * 处理用户未登录异常
     */
    @ExceptionHandler(UserNotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleUserNotLogin(UserNotLoginException e) {
        log.warn("用户未登录: {}", e.getMessage());
        return Result.fail(401, e.getMessage());
    }

    /**
     * 处理权限验证异常
     */
    @ExceptionHandler(PermissionValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handlePermissionValidation(PermissionValidationException e) {
        log.warn("权限验证异常: {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 处理RBAC系统异常
     */
    @ExceptionHandler(RbacSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleRbacSystem(RbacSystemException e) {
        log.error("RBAC系统异常: {}", e.getMessage(), e);
        return Result.fail(500, "权限系统异常，请联系管理员");
    }

    /**
     * 权限不足异常
     */
    public static class PermissionDeniedException extends RuntimeException {
        public PermissionDeniedException(String message) {
            super(message);
        }
        
        public PermissionDeniedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 角色不足异常
     */
    public static class RoleDeniedException extends RuntimeException {
        public RoleDeniedException(String message) {
            super(message);
        }
        
        public RoleDeniedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 用户未登录异常
     */
    public static class UserNotLoginException extends RuntimeException {
        public UserNotLoginException(String message) {
            super(message);
        }
        
        public UserNotLoginException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 权限验证异常
     */
    public static class PermissionValidationException extends RuntimeException {
        public PermissionValidationException(String message) {
            super(message);
        }
        
        public PermissionValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * RBAC系统异常
     */
    public static class RbacSystemException extends RuntimeException {
        public RbacSystemException(String message) {
            super(message);
        }
        
        public RbacSystemException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 