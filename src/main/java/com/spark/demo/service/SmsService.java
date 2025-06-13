package com.spark.demo.service;

/**
 * 短信验证码服务接口
 * @author spark
 * @date 2025-05-29
 */
public interface SmsService {
    
    /**
     * 发送验证码
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendVerifyCode(String phone);
    
    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String phone, String code);
    
    /**
     * 删除验证码
     * @param phone 手机号
     */
    void removeCode(String phone);
} 