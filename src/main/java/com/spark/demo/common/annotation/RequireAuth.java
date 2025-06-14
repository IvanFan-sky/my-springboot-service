package com.spark.demo.common.annotation;

import java.lang.annotation.*;

/**
 * 需要登录认证的注解
 * @author spark
 * @date 2025-05-29
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAuth {
    /**
     * 是否需要登录，默认需要
     */
    boolean value() default true;
} 