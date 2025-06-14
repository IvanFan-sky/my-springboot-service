package com.spark.demo.modules.rbac.aspect;

import com.spark.demo.common.context.UserContext;
import com.spark.demo.common.exception.BusinessException;
import com.spark.demo.common.result.ResultCode;
import com.spark.demo.entity.User;
import com.spark.demo.modules.rbac.annotation.RequirePermission;
import com.spark.demo.modules.rbac.annotation.RequireRole;
import com.spark.demo.modules.rbac.service.RbacCacheService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * 权限验证AOP切面
 * 实现基于注解的权限和角色验证
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@Aspect
@Component
@Order(100) // 确保在其他切面之后执行
public class PermissionAspect {

    @Autowired
    private RbacCacheService rbacCacheService;

    /**
     * 权限验证切点
     */
    @Before("@annotation(com.spark.demo.modules.rbac.annotation.RequirePermission) || " +
            "@within(com.spark.demo.modules.rbac.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        log.debug("开始权限验证");
        
        try {
            // 获取当前用户
            User currentUser = UserContext.getCurrentUser();
            if (currentUser == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
            }

            // 获取方法和类上的权限注解
            RequirePermission methodAnnotation = getMethodAnnotation(joinPoint, RequirePermission.class);
            RequirePermission classAnnotation = getClassAnnotation(joinPoint, RequirePermission.class);
            
            // 优先使用方法上的注解，如果没有则使用类上的注解
            RequirePermission annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
            
            if (annotation == null) {
                return; // 没有权限注解，跳过验证
            }

            // 检查是否允许超级管理员跳过
            if (annotation.allowSuperAdmin() && isSuperAdmin(currentUser)) {
                log.debug("超级管理员跳过权限验证, userId: {}", currentUser.getId());
                return;
            }

            // 验证权限
            validatePermissions(currentUser.getId(), annotation);
            
            log.debug("权限验证通过, userId: {}, permissions: {}", 
                    currentUser.getId(), Arrays.toString(annotation.value()));
                    
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("权限验证异常", e);
            throw new BusinessException(ResultCode.FORBIDDEN, "权限验证失败");
        }
    }

    /**
     * 角色验证切点
     */
    @Before("@annotation(com.spark.demo.modules.rbac.annotation.RequireRole) || " +
            "@within(com.spark.demo.modules.rbac.annotation.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        log.debug("开始角色验证");
        
        try {
            // 获取当前用户
            User currentUser = UserContext.getCurrentUser();
            if (currentUser == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
            }

            // 获取方法和类上的角色注解
            RequireRole methodAnnotation = getMethodAnnotation(joinPoint, RequireRole.class);
            RequireRole classAnnotation = getClassAnnotation(joinPoint, RequireRole.class);
            
            // 优先使用方法上的注解，如果没有则使用类上的注解
            RequireRole annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
            
            if (annotation == null) {
                return; // 没有角色注解，跳过验证
            }

            // 检查是否允许超级管理员跳过
            if (annotation.allowSuperAdmin() && isSuperAdmin(currentUser)) {
                log.debug("超级管理员跳过角色验证, userId: {}", currentUser.getId());
                return;
            }

            // 验证角色
            validateRoles(currentUser.getId(), annotation);
            
            log.debug("角色验证通过, userId: {}, roles: {}", 
                    currentUser.getId(), Arrays.toString(annotation.value()));
                    
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("角色验证异常", e);
            throw new BusinessException(ResultCode.FORBIDDEN, "角色验证失败");
        }
    }

    /**
     * 验证权限
     */
    private void validatePermissions(Long userId, RequirePermission annotation) {
        String[] requiredPermissions = annotation.value();
        if (requiredPermissions.length == 0) {
            return; // 没有指定权限，跳过验证
        }

        Set<String> userPermissions = rbacCacheService.getUserPermissionCodes(userId);
        
        boolean hasPermission;
        if (annotation.logical() == RequirePermission.LogicalType.AND) {
            // AND关系：需要拥有所有权限
            hasPermission = Arrays.stream(requiredPermissions)
                    .allMatch(userPermissions::contains);
        } else {
            // OR关系：只需要拥有其中一个权限
            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            String message = StringUtils.hasText(annotation.message()) ? 
                    annotation.message() : "权限不足，访问被拒绝";
            log.warn("权限验证失败, userId: {}, 需要权限: {}, 用户权限: {}", 
                    userId, Arrays.toString(requiredPermissions), userPermissions);
            throw new BusinessException(ResultCode.FORBIDDEN, message);
        }
    }

    /**
     * 验证角色
     */
    private void validateRoles(Long userId, RequireRole annotation) {
        String[] requiredRoles = annotation.value();
        if (requiredRoles.length == 0) {
            return; // 没有指定角色，跳过验证
        }

        Set<String> userRoles = rbacCacheService.getUserRoleCodes(userId);
        
        boolean hasRole;
        if (annotation.logical() == RequireRole.LogicalType.AND) {
            // AND关系：需要拥有所有角色
            hasRole = Arrays.stream(requiredRoles)
                    .allMatch(userRoles::contains);
        } else {
            // OR关系：只需要拥有其中一个角色
            hasRole = Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);
        }

        if (!hasRole) {
            String message = StringUtils.hasText(annotation.message()) ? 
                    annotation.message() : "角色权限不足，访问被拒绝";
            log.warn("角色验证失败, userId: {}, 需要角色: {}, 用户角色: {}", 
                    userId, Arrays.toString(requiredRoles), userRoles);
            throw new BusinessException(ResultCode.FORBIDDEN, message);
        }
    }

    /**
     * 检查是否为超级管理员
     */
    private boolean isSuperAdmin(User user) {
        if (user == null) {
            return false;
        }
        
        // 检查用户角色是否包含超级管理员
        Set<String> userRoles = rbacCacheService.getUserRoleCodes(user.getId());
        return userRoles.contains("super_admin");
    }

    /**
     * 获取方法上的注解
     */
    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getMethodAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(annotationClass);
    }

    /**
     * 获取类上的注解
     */
    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getClassAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        return joinPoint.getTarget().getClass().getAnnotation(annotationClass);
    }
} 