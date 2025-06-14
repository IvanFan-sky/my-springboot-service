package com.spark.demo.common.annotation;

import java.lang.annotation.*;

/**
 * 需要特定角色的注解
 * @author spark
 * @date 2025-05-29
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    /**
     * 需要的角色列表，满足其中一个即可
     */
    String[] value() default {};
    
    /**
     * 是否需要同时满足所有角色，默认false（满足任一角色即可）
     */
    boolean requireAll() default false;
} 