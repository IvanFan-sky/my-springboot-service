package com.spark.demo.modules.rbac.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.demo.common.result.Result;
import com.spark.demo.modules.rbac.service.RbacCacheService;
import com.spark.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截器
 * 在请求处理前进行权限验证
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private RbacCacheService rbacCacheService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 不需要权限验证的路径
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/api/v1/captcha",
            "/api/v1/sms",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico",
            "/error",
            "/actuator"
    );

    /**
     * 超级管理员角色，拥有所有权限
     */
    private static final String SUPER_ADMIN_ROLE = "super_admin";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("权限拦截器检查: {} {}", method, requestPath);

        // 检查是否为排除路径
        if (isExcludePath(requestPath)) {
            log.debug("路径 {} 在排除列表中，跳过权限检查", requestPath);
            return true;
        }

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            log.warn("用户未登录，拒绝访问: {} {}", method, requestPath);
            writeUnauthorizedResponse(response, "用户未登录");
            return false;
        }

        // 检查是否为超级管理员
        if (isSuperAdmin(userId)) {
            log.debug("用户 {} 是超级管理员，允许访问", userId);
            return true;
        }

        // 检查路径权限
        boolean hasPermission = userService.hasPathPermission(userId, requestPath, method);
        if (!hasPermission) {
            log.warn("用户 {} 没有访问权限: {} {}", userId, method, requestPath);
            writeAccessDeniedResponse(response, "没有访问权限");
            return false;
        }

        log.debug("用户 {} 权限验证通过: {} {}", userId, method, requestPath);
        return true;
    }

    /**
     * 检查是否为排除路径
     */
    private boolean isExcludePath(String requestPath) {
        return EXCLUDE_PATHS.stream().anyMatch(requestPath::startsWith);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // 从Session中获取用户ID
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            }
        }

        // 从Header中获取用户ID（如果使用Token认证）
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID格式: {}", userIdHeader);
            }
        }

        return null;
    }

    /**
     * 检查是否为超级管理员
     */
    private boolean isSuperAdmin(Long userId) {
        try {
            return rbacCacheService.hasRole(userId, SUPER_ADMIN_ROLE);
        } catch (Exception e) {
            log.error("检查超级管理员权限时发生错误", e);
            return false;
        }
    }

    /**
     * 写入未授权响应
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<String> result = Result.fail(401, message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 写入访问拒绝响应
     */
    private void writeAccessDeniedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<String> result = Result.fail(403, message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }
} 