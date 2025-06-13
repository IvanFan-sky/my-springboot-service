package com.spark.demo.service.impl;

import com.spark.demo.service.SmsService;
import com.spark.demo.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现类
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";
    private static final int CODE_EXPIRE_MINUTES = 5; // 验证码5分钟过期
    private static final int LIMIT_EXPIRE_MINUTES = 1; // 限制1分钟内只能发送一次
    private static final int CODE_LENGTH = 6; // 验证码长度
    
    @Override
    public boolean sendVerifyCode(String phone) {
        if (!StringUtils.hasText(phone)) {
            log.warn("手机号为空");
            return false;
        }
        
        // 检查发送频率限制
        String limitKey = SMS_LIMIT_PREFIX + phone;
        if (redisUtil.hasKey(limitKey)) {
            log.warn("发送验证码过于频繁，手机号: {}", phone);
            return false;
        }
        
        // 生成6位随机验证码
        String code = generateVerifyCode();
        String codeKey = SMS_CODE_PREFIX + phone;
        
        try {
            // 存储验证码，设置过期时间（使用字符串存储，避免序列化问题）
            redisUtil.setString(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // 设置发送频率限制
            redisUtil.setString(limitKey, "1", LIMIT_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // TODO: 这里应该调用真实的短信服务API发送短信
            // 目前仅在日志中打印验证码（仅用于开发环境）
            log.info("发送验证码到手机号: {}, 验证码: {} (仅开发环境显示)", phone, code);
            
            return true;
        } catch (Exception e) {
            log.error("发送验证码失败，手机号: {}", phone, e);
            return false;
        }
    }
    
    @Override
    public boolean verifyCode(String phone, String code) {
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            log.warn("手机号或验证码为空");
            return false;
        }
        
        String codeKey = SMS_CODE_PREFIX + phone;
        try {
            // 使用字符串获取验证码，避免序列化问题
            String storedCode = redisUtil.getString(codeKey);
            if (storedCode == null) {
                log.warn("验证码不存在或已过期，手机号: {}", phone);
                return false;
            }
            
            boolean isValid = code.equals(storedCode);
            if (isValid) {
                log.info("验证码验证成功，手机号: {}", phone);
                // 验证成功后删除验证码
                removeCode(phone);
            } else {
                log.warn("验证码错误，手机号: {}, 输入: {}, 期望: {}", phone, code, storedCode);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("验证验证码失败，手机号: {}", phone, e);
            return false;
        }
    }
    
    @Override
    public void removeCode(String phone) {
        if (StringUtils.hasText(phone)) {
            String codeKey = SMS_CODE_PREFIX + phone;
            redisUtil.delete(codeKey);
            log.debug("删除验证码，手机号: {}", phone);
        }
    }
    
    /**
     * 生成随机验证码
     */
    private String generateVerifyCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 