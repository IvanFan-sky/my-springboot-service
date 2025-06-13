package com.spark.demo.config;

import com.spark.demo.filter.AuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Configuration
public class FilterConfig {
    
    @Autowired
    private AuthFilter authFilter;
    
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authFilter);
        registration.addUrlPatterns("/*"); // 匹配所有路径，因为context-path已经是/api了
        registration.setOrder(1); // 设置过滤器优先级
        registration.setName("authFilter");
        
        log.info("认证过滤器注册完成 - URL模式: /*");
        return registration;
    }
} 