package com.spark.demo.modules.rbac.annotation;

import java.lang.annotation.*;

/**
 * 角色验证注解
 * 用于方法级别的角色控制
 * 
 * @author spark
 * @date 2025-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 需要的角色编码
     * 支持多个角色，默认为OR关系
     * @return 角色编码数组
     */
    String[] value() default {};

    /**
     * 角色关系类型
     * @return 关系类型
     */
    LogicalType logical() default LogicalType.OR;

    /**
     * 角色验证失败时的错误消息
     * @return 错误消息
     */
    String message() default "角色权限不足，访问被拒绝";

    /**
     * 是否允许超级管理员跳过角色检查
     * @return 是否允许跳过
     */
    boolean allowSuperAdmin() default true;

    /**
     * 逻辑关系类型
     */
    enum LogicalType {
        /**
         * AND关系：需要拥有所有指定角色
         */
        AND,
        
        /**
         * OR关系：只需要拥有其中一个角色
         */
        OR
    }
} 