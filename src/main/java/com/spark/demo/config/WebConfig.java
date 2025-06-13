package com.spark.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许发送凭证（包括Session Cookie）
        config.setAllowCredentials(true);
        
        // 明确指定前端地址，避免使用通配符
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        
        // 允许所有常用HTTP头
        config.addAllowedHeader("*");
        
        // 允许所有HTTP方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("PATCH");
        
        // 暴露自定义响应头
        config.addExposedHeader("X-Session-Id");
        config.addExposedHeader("Set-Cookie");
        
        // 预检请求的有效期
        config.setMaxAge(3600L);
        
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 