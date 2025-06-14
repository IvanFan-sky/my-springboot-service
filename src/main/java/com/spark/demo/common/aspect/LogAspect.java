package com.spark.demo.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.demo.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 日志切面
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Aspect
@Component
@Order(2) // 设置优先级，在权限检查之后执行
public class LogAspect {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Around("execution(* com.spark.demo.controller.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        
        // 记录请求日志
        try {
            String requestInfo = buildRequestInfo(request, className, methodName, args);
            log.info("请求开始: {}", requestInfo);
        } catch (Exception e) {
            log.warn("记录请求日志失败", e);
        }
        
        Object result = null;
        Exception exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;
            
            // 记录响应日志
            try {
                String responseInfo = buildResponseInfo(className, methodName, executeTime, result, exception);
                if (exception != null) {
                    log.error("请求异常: {}", responseInfo);
                } else {
                    log.info("请求完成: {}", responseInfo);
                }
            } catch (Exception e) {
                log.warn("记录响应日志失败", e);
            }
        }
    }
    
    /**
     * 构建请求信息
     */
    private String buildRequestInfo(HttpServletRequest request, String className, String methodName, Object[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("方法: ").append(className).append(".").append(methodName);
        
        if (request != null) {
            sb.append(", URI: ").append(request.getRequestURI());
            sb.append(", 方式: ").append(request.getMethod());
            sb.append(", IP: ").append(getClientIpAddress(request));
        }
        
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) {
            sb.append(", 用户ID: ").append(userId);
        }
        
        // 记录参数（排除敏感信息）
        if (args != null && args.length > 0) {
            try {
                String argsJson = objectMapper.writeValueAsString(maskSensitiveInfo(args));
                sb.append(", 参数: ").append(argsJson);
            } catch (Exception e) {
                sb.append(", 参数: [序列化失败]");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 构建响应信息
     */
    private String buildResponseInfo(String className, String methodName, long executeTime, Object result, Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("方法: ").append(className).append(".").append(methodName);
        sb.append(", 耗时: ").append(executeTime).append("ms");
        
        if (exception != null) {
            sb.append(", 异常: ").append(exception.getClass().getSimpleName());
            sb.append(", 消息: ").append(exception.getMessage());
        } else if (result != null) {
            try {
                String resultJson = objectMapper.writeValueAsString(result);
                // 限制响应日志长度
                if (resultJson.length() > 500) {
                    resultJson = resultJson.substring(0, 500) + "...";
                }
                sb.append(", 结果: ").append(resultJson);
            } catch (Exception e) {
                sb.append(", 结果: [序列化失败]");
            }
        }
        
        return sb.toString();
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
    
    /**
     * 屏蔽敏感信息
     */
    private Object[] maskSensitiveInfo(Object[] args) {
        // 简单处理，实际项目中可以更复杂的逻辑
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg != null && arg.toString().contains("password")) {
                        return "[MASKED]";
                    }
                    return arg;
                })
                .toArray();
    }
} 