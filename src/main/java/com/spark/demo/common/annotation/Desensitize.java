package com.spark.demo.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spark.demo.common.serializer.DesensitizeSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解
 * 用于在JSON序列化时对敏感数据进行脱敏处理
 * 
 * @author spark
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizeSerializer.class)
public @interface Desensitize {

    /**
     * 脱敏类型
     */
    DesensitizeType type() default DesensitizeType.CUSTOM;

    /**
     * 开始保留长度（从左开始）
     */
    int startLen() default 0;

    /**
     * 结束保留长度（从右开始）
     */
    int endLen() default 0;

    /**
     * 替换字符
     */
    String replacement() default "*";

    /**
     * 脱敏类型枚举
     */
    enum DesensitizeType {
        /**
         * 自定义脱敏
         */
        CUSTOM,
        /**
         * 手机号脱敏：138****1234
         */
        PHONE,
        /**
         * 身份证脱敏：1234***********5678
         */
        ID_CARD,
        /**
         * 邮箱脱敏：abc***@example.com
         */
        EMAIL,
        /**
         * 姓名脱敏：张*
         */
        NAME,
        /**
         * 地址脱敏：北京市***
         */
        ADDRESS,
        /**
         * 银行卡脱敏：1234 **** **** 5678
         */
        BANK_CARD,
        /**
         * 密码脱敏：******
         */
        PASSWORD
    }
} 