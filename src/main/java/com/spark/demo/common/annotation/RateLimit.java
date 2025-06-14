package com.spark.demo.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 * 基于Redis + Bucket4j实现分布式限流
 * 
 * @author spark
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key前缀，默认为方法全路径
     */
    String key() default "";

    /**
     * 限流次数，默认每分钟10次
     */
    long count() default 10;

    /**
     * 限流时间窗口，默认1分钟
     */
    long time() default 1;

    /**
     * 时间单位，默认分钟
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 限流失败消息
     */
    String message() default "访问过于频繁，请稍后再试";

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 默认策略全局限流
         */
        DEFAULT,
        /**
         * 根据请求者IP进行限流
         */
        IP,
        /**
         * 根据用户ID进行限流
         */
        USER
    }
} 