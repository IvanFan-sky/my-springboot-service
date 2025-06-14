package com.spark.demo.modules.sms.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * 短信领域服务
 * 处理短信验证码相关的业务逻辑
 * @author spark
 * @date 2025-06-14
 */
@Slf4j
@Service
public class SmsDomainService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final Random RANDOM = new Random();

    /**
     * 验证手机号格式
     * @param phone 手机号
     * @return 验证结果
     */
    public SmsResult validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return SmsResult.failure("手机号不能为空");
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return SmsResult.failure("手机号格式不正确");
        }

        return SmsResult.success("手机号格式正确");
    }

    /**
     * 验证验证码格式
     * @param code 验证码
     * @return 验证结果
     */
    public SmsResult validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return SmsResult.failure("验证码不能为空");
        }

        if (!CODE_PATTERN.matcher(code).matches()) {
            return SmsResult.failure("验证码格式不正确，应为6位数字");
        }

        return SmsResult.success("验证码格式正确");
    }

    /**
     * 生成6位数字验证码
     * @return 验证码
     */
    public String generateVerifyCode() {
        int code = RANDOM.nextInt(900000) + 100000; // 生成100000-999999之间的数字
        String verifyCode = String.valueOf(code);
        log.debug("生成验证码: {}", verifyCode);
        return verifyCode;
    }

    /**
     * 检查发送频率限制
     * @param phone 手机号
     * @param lastSendTime 上次发送时间（毫秒）
     * @return 检查结果
     */
    public SmsResult checkSendFrequency(String phone, Long lastSendTime) {
        if (lastSendTime == null) {
            return SmsResult.success("可以发送");
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastSendTime;
        long minInterval = 60 * 1000; // 1分钟

        if (timeDiff < minInterval) {
            long remainingSeconds = (minInterval - timeDiff) / 1000;
            return SmsResult.failure("发送过于频繁，请" + remainingSeconds + "秒后再试");
        }

        return SmsResult.success("可以发送");
    }

    /**
     * 构建短信内容
     * @param code 验证码
     * @return 短信内容
     */
    public String buildSmsContent(String code) {
        return String.format("【Spark Demo】您的验证码是：%s，5分钟内有效，请勿泄露给他人。", code);
    }

    /**
     * 验证验证码是否过期
     * @param sendTime 发送时间（毫秒）
     * @return 验证结果
     */
    public SmsResult checkCodeExpiry(Long sendTime) {
        if (sendTime == null) {
            return SmsResult.failure("验证码不存在");
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - sendTime;
        long expireTime = 5 * 60 * 1000; // 5分钟

        if (timeDiff > expireTime) {
            return SmsResult.failure("验证码已过期");
        }

        return SmsResult.success("验证码有效");
    }

    /**
     * 短信结果类
     */
    public static class SmsResult {
        private final boolean success;
        private final String message;

        private SmsResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SmsResult success(String message) {
            return new SmsResult(true, message);
        }

        public static SmsResult failure(String message) {
            return new SmsResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
} 