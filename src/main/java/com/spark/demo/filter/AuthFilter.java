package com.spark.demo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.demo.common.context.UserContext;
import com.spark.demo.common.result.Result;
import com.spark.demo.common.result.ResultCode;
import com.spark.demo.entity.User;
import com.spark.demo.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 认证过滤器
 *
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Component
public class AuthFilter implements Filter {

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 不需要认证的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/v1/users/login",
            "/api/v1/users/register",
            "/api/v1/users/password-login",
            "/api/v1/users/sms-login",
            "/api/v1/sms/send",
            "/api/swagger-ui",
            "/api/v3/api-docs",
            "/api/doc.html",
            "/api/favicon.ico",
            "/api/webjars",
            "/api/swagger-resources",
            "/error"
    );

    @Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
        log.warn("不支持的请求/响应类型");
        writeErrorResponse((HttpServletResponse) response, "不支持的请求/响应类型");
        return;
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String requestURI = httpRequest.getRequestURI();
    String method = httpRequest.getMethod();
    String clientIp = getClientIpAddress(httpRequest);

    // 提前记录关键信息
    log.info("AuthFilter处理请求 - URI: {}, Method: {}", requestURI, method);
    
    // 对OPTIONS预检请求直接放行，不进行身份验证
    if ("OPTIONS".equalsIgnoreCase(method)) {
        log.info("OPTIONS预检请求直接放行: {}", requestURI);
        chain.doFilter(request, response);
        return;
    }
    
    // 调试排除路径
    log.debug("检查排除路径 - URI: {}, 排除路径列表: {}", requestURI, EXCLUDE_PATHS);
    boolean excluded = isExcludePath(requestURI);
    log.debug("排除路径检查结果 - URI: {}, 是否排除: {}", requestURI, excluded);

    try {
        // 快速处理排除路径
        if (excluded) {
            log.info("请求路径在排除列表中: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // 处理认证流程
        HttpSession session = httpRequest.getSession(false);
        User currentUser = null;

        if (session != null) {
            String userUuid = (String) session.getAttribute("userUuid");

            if (userUuid != null) {
                currentUser = userService.findByUuid(userUuid);

                if (log.isDebugEnabled()) {
                    log.debug("通过UUID查找用户结果: {}", currentUser != null ? "找到用户" : "用户不存在");
                }

                if (currentUser == null || currentUser.getDeletedTime() != null) {
                    if (currentUser != null && currentUser.getDeletedTime() != null) {
                        log.warn("用户已被删除，UUID: {}", userUuid);
                    }
                    currentUser = null;
                }
            }

            if (log.isTraceEnabled()) {
                log.trace("处理认证请求 - URI: {}, SessionId: {}", requestURI,
                        session != null ? session.getId() : "null");
                log.trace("Session中的用户信息 - userUuid: {}", userUuid);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Session为空，请求路径: {}", requestURI);
        }

        if (currentUser == null) {
            // 特殊处理logout请求：即使Session无效，也允许访问logout接口
            if (requestURI.equals("/api/v1/users/logout")) {
                log.info("处理logout请求，即使用户未登录也允许访问");
                chain.doFilter(request, response);
                return;
            }
            
            log.warn("用户未登录，请求路径: {}, IP: {}, Session存在: {}",
                    requestURI, clientIp, session != null);
            writeUnauthorizedResponse(httpResponse, "用户未登录");
            return;
        }

        if (currentUser.getStatus() == 0) {
            log.warn("用户已被禁用，UUID: {}, IP: {}", currentUser.getUuid(), clientIp);
            writeUnauthorizedResponse(httpResponse, "用户已被禁用");
            return;
        }

        // 设置用户上下文
        UserContext.setCurrentUser(currentUser);

        // 继续执行后续过滤器链
        chain.doFilter(request, response);

    } catch (ServletException | IOException e) {
        throw e;
    } catch (Exception e) {
        log.error("认证过滤器异常，请求路径: {}, IP: {}", requestURI, clientIp, e);
        writeErrorResponse(httpResponse, "认证异常");
    } finally {
        UserContext.clear();
    }
}


    /**
     * 检查是否为排除路径
     */
    private boolean isExcludePath(String requestURI) {
        return EXCLUDE_PATHS.stream().anyMatch(requestURI::startsWith);
    }

    /**
     * 写入未授权响应
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(ResultCode.FAIL.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
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