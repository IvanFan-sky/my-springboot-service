package com.spark.demo.config;

import com.spark.demo.modules.rbac.filter.RbacFilter;
import com.spark.demo.modules.rbac.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * RBAC Web配置类
 * 注册权限拦截器和过滤器
 * 
 * @author spark
 * @date 2025-01-01
 */
@Configuration
public class RbacWebConfig implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Autowired
    private RbacFilter rbacFilter;

    /**
     * 注册权限拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                        "/api/v1/auth/**",      // 认证相关接口
                        "/api/v1/captcha/**",   // 验证码接口
                        "/api/v1/sms/**",       // 短信接口
                        "/swagger-ui/**",       // Swagger UI
                        "/v3/api-docs/**",      // API文档
                        "/favicon.ico",         // 网站图标
                        "/error",               // 错误页面
                        "/actuator/**",         // 监控端点
                        "/static/**",           // 静态资源
                        "/css/**",              // CSS文件
                        "/js/**",               // JS文件
                        "/images/**"            // 图片文件
                );
    }

    /**
     * 注册RBAC过滤器
     */
    @Bean
    public FilterRegistrationBean<RbacFilter> rbacFilterRegistration() {
        FilterRegistrationBean<RbacFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rbacFilter);
        registration.addUrlPatterns("/api/*"); // 只对API路径进行过滤
        registration.setName("rbacFilter");
        registration.setOrder(1); // 设置过滤器顺序
        return registration;
    }
} 