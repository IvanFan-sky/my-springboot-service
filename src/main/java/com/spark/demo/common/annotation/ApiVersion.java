package com.spark.demo.common.annotation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

/**
 * API版本管理注解
 * 用于标识接口的版本信息，支持版本控制和文档生成
 * 
 * @author spark
 * @date 2025-06-14
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Schema(description = "API版本信息")
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

    /**
     * 版本发布日期
     * @return 发布日期，格式：yyyy-MM-dd
     */
    String releaseDate() default "";

    /**
     * 版本变更说明
     * @return 变更说明
     */
    String[] changeLog() default {};

    /**
     * 是否为测试版本
     * @return 是否为测试版本
     */
    boolean beta() default false;

    /**
     * 最低兼容版本
     * @return 最低兼容版本号
     */
    String minCompatibleVersion() default "";
} 