package com.spark.demo.modules.rbac.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.demo.common.result.Result;
import com.spark.demo.modules.rbac.service.RbacCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RBAC权限过滤器
 * 提供更细粒度的权限控制，在拦截器之前执行
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@Component
@Order(1) // 确保在其他过滤器之前执行
public class RbacFilter implements Filter {

    @Autowired
    private RbacCacheService rbacCacheService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 不需要权限验证的路径模式
     */
    private static final List<Pattern> EXCLUDE_PATTERNS = Arrays.asList(
            Pattern.compile("^/api/v1/auth/.*"),
            Pattern.compile("^/api/v1/captcha.*"),
            Pattern.compile("^/api/v1/sms.*"),
            Pattern.compile("^/swagger-ui.*"),
            Pattern.compile("^/v3/api-docs.*"),
            Pattern.compile("^/favicon\\.ico$"),
            Pattern.compile("^/error.*"),
            Pattern.compile("^/actuator.*"),
            Pattern.compile("^/static/.*"),
            Pattern.compile("^/css/.*"),
            Pattern.compile("^/js/.*"),
            Pattern.compile("^/images/.*")
    );

    /**
     * 需要特殊权限的API路径配置
     */
    private static final List<ApiPermissionRule> API_PERMISSION_RULES = Arrays.asList(
            // 用户管理API
            new ApiPermissionRule("^/api/v1/users.*", "GET", "user:read"),
            new ApiPermissionRule("^/api/v1/users.*", "POST", "user:create"),
            new ApiPermissionRule("^/api/v1/users.*", "PUT", "user:update"),
            new ApiPermissionRule("^/api/v1/users.*", "DELETE", "user:delete"),
            
            // 角色管理API
            new ApiPermissionRule("^/api/v1/rbac/roles.*", "GET", "role:read"),
            new ApiPermissionRule("^/api/v1/rbac/roles.*", "POST", "role:create"),
            new ApiPermissionRule("^/api/v1/rbac/roles.*", "PUT", "role:update"),
            new ApiPermissionRule("^/api/v1/rbac/roles.*", "DELETE", "role:delete"),
            
            // 权限管理API
            new ApiPermissionRule("^/api/v1/rbac/permissions.*", "GET", "permission:read"),
            new ApiPermissionRule("^/api/v1/rbac/permissions.*", "POST", "permission:create"),
            new ApiPermissionRule("^/api/v1/rbac/permissions.*", "PUT", "permission:update"),
            new ApiPermissionRule("^/api/v1/rbac/permissions.*", "DELETE", "permission:delete"),
            
            // 菜单管理API
            new ApiPermissionRule("^/api/v1/rbac/menus.*", "GET", "menu:read"),
            new ApiPermissionRule("^/api/v1/rbac/menus.*", "POST", "menu:create"),
            new ApiPermissionRule("^/api/v1/rbac/menus.*", "PUT", "menu:update"),
            new ApiPermissionRule("^/api/v1/rbac/menus.*", "DELETE", "menu:delete"),
            
            // 系统管理API
            new ApiPermissionRule("^/api/v1/system.*", ".*", "system:manage"),
            new ApiPermissionRule("^/api/v1/rbac/.*/cache.*", "DELETE", "system:manage_cache")
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        log.debug("RBAC过滤器检查: {} {}", method, requestPath);

        // 检查是否为排除路径
        if (isExcludePath(requestPath)) {
            log.debug("路径 {} 在排除列表中，跳过权限检查", requestPath);
            chain.doFilter(request, response);
            return;
        }

        // 获取当前用户ID
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            log.warn("用户未登录，拒绝访问: {} {}", method, requestPath);
            writeUnauthorizedResponse(httpResponse, "用户未登录");
            return;
        }

        // 检查是否为超级管理员
        if (isSuperAdmin(userId)) {
            log.debug("用户 {} 是超级管理员，允许访问", userId);
            chain.doFilter(request, response);
            return;
        }

        // 检查API权限
        String requiredPermission = getRequiredPermission(requestPath, method);
        if (requiredPermission != null) {
            boolean hasPermission = rbacCacheService.hasPermission(userId, requiredPermission);
            if (!hasPermission) {
                log.warn("用户 {} 缺少权限 {} 访问: {} {}", userId, requiredPermission, method, requestPath);
                writeAccessDeniedResponse(httpResponse, "缺少必要权限: " + requiredPermission);
                return;
            }
        }

        log.debug("用户 {} 权限验证通过: {} {}", userId, method, requestPath);
        chain.doFilter(request, response);
    }

    /**
     * 检查是否为排除路径
     */
    private boolean isExcludePath(String requestPath) {
        return EXCLUDE_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(requestPath).matches());
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
            return rbacCacheService.hasRole(userId, "super_admin");
        } catch (Exception e) {
            log.error("检查超级管理员权限时发生错误", e);
            return false;
        }
    }

    /**
     * 获取访问指定路径和方法所需的权限
     */
    private String getRequiredPermission(String path, String method) {
        return API_PERMISSION_RULES.stream()
                .filter(rule -> rule.matches(path, method))
                .map(ApiPermissionRule::getPermission)
                .findFirst()
                .orElse(null);
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

    /**
     * API权限规则
     */
    private static class ApiPermissionRule {
        private final Pattern pathPattern;
        private final Pattern methodPattern;
        private final String permission;

        public ApiPermissionRule(String pathRegex, String methodRegex, String permission) {
            this.pathPattern = Pattern.compile(pathRegex);
            this.methodPattern = Pattern.compile(methodRegex, Pattern.CASE_INSENSITIVE);
            this.permission = permission;
        }

        public boolean matches(String path, String method) {
            return pathPattern.matcher(path).matches() && methodPattern.matcher(method).matches();
        }

        public String getPermission() {
            return permission;
        }
    }
} 