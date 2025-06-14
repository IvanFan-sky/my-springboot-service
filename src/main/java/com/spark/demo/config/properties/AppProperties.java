package com.spark.demo.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * 应用配置属性
 * 用于配置验证和类型安全的配置访问
 * 
 * @author spark
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 应用基本信息
     */
    @NotBlank(message = "应用名称不能为空")
    private String name = "SpringBoot通用后台服务";

    @NotBlank(message = "应用版本不能为空")
    private String version = "1.0.0";

    @NotBlank(message = "应用描述不能为空")
    private String description = "基于SpringBoot3的通用后台服务";

    @NotBlank(message = "应用作者不能为空")
    private String author = "spark";

    /**
     * JWT配置
     */
    @Valid
    private Jwt jwt = new Jwt();

    /**
     * 安全配置
     */
    @Valid
    private Security security = new Security();

    /**
     * 异步配置
     */
    @Valid
    private Async async = new Async();

    @Data
    public static class Jwt {
        /**
         * JWT密钥
         */
        @NotBlank(message = "JWT密钥不能为空")
        @Size(min = 32, message = "JWT密钥长度不能少于32位")
        private String secret = "mySecretKeyForJWTTokenGenerationThatShouldBeLongEnoughForHS256Algorithm";

        /**
         * 访问token过期时间（小时）
         */
        @Min(value = 1, message = "访问token过期时间不能少于1小时")
        @Max(value = 168, message = "访问token过期时间不能超过168小时")
        private int expiration = 24;

        /**
         * 刷新token过期时间（天）
         */
        @Min(value = 1, message = "刷新token过期时间不能少于1天")
        @Max(value = 30, message = "刷新token过期时间不能超过30天")
        private int refreshExpiration = 7;
    }

    @Data
    public static class Security {
        /**
         * CORS配置
         */
        @Valid
        private Cors cors = new Cors();

        /**
         * 限流配置
         */
        @Valid
        private RateLimit rateLimit = new RateLimit();

        @Data
        public static class Cors {
            /**
             * 允许的源
             */
            @NotEmpty(message = "CORS允许的源不能为空")
            private String allowedOrigins = "http://localhost:3000,http://localhost:8080";

            /**
             * 允许的方法
             */
            @NotEmpty(message = "CORS允许的方法不能为空")
            private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";

            /**
             * 允许的头
             */
            @NotEmpty(message = "CORS允许的头不能为空")
            private String allowedHeaders = "*";

            /**
             * 是否允许凭证
             */
            private boolean allowCredentials = true;

            /**
             * 预检请求缓存时间（秒）
             */
            @Min(value = 0, message = "预检请求缓存时间不能为负数")
            @Max(value = 86400, message = "预检请求缓存时间不能超过24小时")
            private long maxAge = 3600;
        }

        @Data
        public static class RateLimit {
            /**
             * 是否启用限流
             */
            private boolean enabled = true;

            /**
             * 默认限流次数（每分钟）
             */
            @Min(value = 1, message = "默认限流次数不能少于1")
            @Max(value = 10000, message = "默认限流次数不能超过10000")
            private long defaultLimit = 100;

            /**
             * 登录接口限流次数（每分钟）
             */
            @Min(value = 1, message = "登录接口限流次数不能少于1")
            @Max(value = 100, message = "登录接口限流次数不能超过100")
            private long loginLimit = 5;

            /**
             * 短信接口限流次数（每分钟）
             */
            @Min(value = 1, message = "短信接口限流次数不能少于1")
            @Max(value = 10, message = "短信接口限流次数不能超过10")
            private long smsLimit = 1;
        }
    }

    @Data
    public static class Async {
        /**
         * 核心线程数
         */
        @Min(value = 1, message = "核心线程数不能少于1")
        @Max(value = 100, message = "核心线程数不能超过100")
        private int corePoolSize = 10;

        /**
         * 最大线程数
         */
        @Min(value = 1, message = "最大线程数不能少于1")
        @Max(value = 500, message = "最大线程数不能超过500")
        private int maxPoolSize = 50;

        /**
         * 队列容量
         */
        @Min(value = 1, message = "队列容量不能少于1")
        @Max(value = 10000, message = "队列容量不能超过10000")
        private int queueCapacity = 200;

        /**
         * 线程空闲时间（秒）
         */
        @Min(value = 1, message = "线程空闲时间不能少于1秒")
        @Max(value = 3600, message = "线程空闲时间不能超过3600秒")
        private int keepAliveSeconds = 60;

        /**
         * 线程名前缀
         */
        @NotBlank(message = "线程名前缀不能为空")
        private String threadNamePrefix = "async-";
    }
} 