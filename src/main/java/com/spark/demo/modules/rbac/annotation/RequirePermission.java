package com.spark.demo.modules.rbac.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * 用于方法级别的权限控制
 * 
 * @author spark
 * @date 2025-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限编码
     * 支持多个权限，默认为AND关系
     * @return 权限编码数组
     */
    String[] value() default {};

    /**
     * 权限关系类型
     * @return 关系类型
     */
    LogicalType logical() default LogicalType.AND;

    /**
     * 权限验证失败时的错误消息
     * @return 错误消息
     */
    String message() default "权限不足，访问被拒绝";

    /**
     * 是否允许超级管理员跳过权限检查
     * @return 是否允许跳过
     */
    boolean allowSuperAdmin() default true;

    /**
     * 逻辑关系类型
     */
    enum LogicalType {
        /**
         * AND关系：需要拥有所有指定权限
         */
        AND,
        
        /**
         * OR关系：只需要拥有其中一个权限
         */
        OR
    }
} 