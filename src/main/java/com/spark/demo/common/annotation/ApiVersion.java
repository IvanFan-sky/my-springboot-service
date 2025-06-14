package com.spark.demo.common.annotation;

import java.lang.annotation.*;

/**
 * API版本管理注解
 * 用于标识接口的版本信息
 * @author spark
 * @date 2025-06-14
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * API版本号
     * @return 版本号，如 "v1", "v2"
     */
    String value() default "v1";

    /**
     * 版本描述
     * @return 版本描述信息
     */
    String description() default "";

    /**
     * 是否已废弃
     * @return 是否废弃
     */
    boolean deprecated() default false;

    /**
     * 废弃说明
     * @return 废弃说明
     */
    String deprecatedReason() default "";

    /**
     * 推荐使用的新版本
     * @return 新版本号
     */
    String recommendedVersion() default "";
} 