package com.spark.demo.aspect;

import com.spark.demo.annotation.RequireRole;
import com.spark.demo.common.context.UserContext;
import com.spark.demo.common.exception.BusinessException;
import com.spark.demo.common.result.ResultCode;
import com.spark.demo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 权限检查切面
 * 注意：基础认证由AuthFilter完成，这里只处理角色权限
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Aspect
@Component
@Order(2) // 在AuthFilter之后执行
public class AuthAspect {
    
    /**
     * 角色权限检查
     * 只检查@RequireRole注解，基础认证由AuthFilter完成
     */
    @Around("@annotation(com.spark.demo.annotation.RequireRole) || @within(com.spark.demo.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解
        RequireRole requireRole = getRequireRoleAnnotation(joinPoint);
        
        if (requireRole == null) {
            return joinPoint.proceed();
        }
        
        // 获取当前用户（AuthFilter已确保用户存在）
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            log.error("AuthAspect：用户上下文为空，这不应该发生（AuthFilter应该已经处理）");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        
        String[] requiredRoles = requireRole.value();
        if (requiredRoles.length == 0) {
            return joinPoint.proceed();
        }
        
        String userRole = currentUser.getRole();
        String methodInfo = joinPoint.getSignature().toShortString();
        
        // 检查角色权限
        boolean hasPermission;
        if (requireRole.requireAll()) {
            // 需要满足所有角色
            hasPermission = Arrays.asList(requiredRoles).containsAll(Arrays.asList(userRole));
        } else {
            // 满足任一角色即可
            hasPermission = Arrays.asList(requiredRoles).contains(userRole);
        }

        if (!hasPermission) {
            log.warn("用户权限不足 - 用户: {}, 角色: {}, 需要角色: {}, 方法: {}", 
                currentUser.getUsername(), userRole, Arrays.toString(requiredRoles), methodInfo);
            throw new BusinessException(ResultCode.FORBIDDEN, "权限不足，需要" + Arrays.toString(requiredRoles) + "角色");
        }
        
        log.debug("角色验证通过 - 用户: {}, 角色: {}, 方法: {}", 
            currentUser.getUsername(), userRole, methodInfo);
        return joinPoint.proceed();
    }
    
    /**
     * 获取RequireRole注解
     * 优先从方法获取，如果方法没有则从类获取
     */
    private RequireRole getRequireRoleAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 首先尝试从方法获取注解
        RequireRole annotation = AnnotatedElementUtils.findMergedAnnotation(method, RequireRole.class);
        if (annotation != null) {
            return annotation;
        }
        
        // 如果方法没有，尝试从类获取注解
        return AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), RequireRole.class);
    }
} 