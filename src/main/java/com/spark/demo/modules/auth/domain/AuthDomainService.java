package com.spark.demo.modules.auth.domain;

import com.spark.demo.modules.user.domain.UserDomain;
import com.spark.demo.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证领域服务
 * 处理用户认证相关的业务逻辑
 * @author spark
 * @date 2025-06-14
 */
@Slf4j
@Service
public class AuthDomainService {

    @Autowired
    private SmsService smsService;

    /**
     * 验证用户登录凭据
     * @param user 用户领域对象
     * @param credential 登录凭据（密码或验证码）
     * @param credentialType 凭据类型（password/sms）
     * @param phone 手机号（短信登录时使用）
     * @return 验证结果
     */
    public AuthResult validateCredentials(UserDomain user, String credential, String credentialType, String phone) {
        if (user == null) {
            return AuthResult.failure("用户不存在");
        }

        if (!user.isActive()) {
            return AuthResult.failure("用户已被禁用");
        }

        if (user.isDeleted()) {
            return AuthResult.failure("用户不存在");
        }

        switch (credentialType.toLowerCase()) {
            case "password":
                return validatePassword(user, credential);
            case "sms":
                return validateSmsCode(phone, credential);
            default:
                return AuthResult.failure("不支持的认证方式");
        }
    }

    /**
     * 验证密码
     * @param user 用户领域对象
     * @param password 密码
     * @return 验证结果
     */
    private AuthResult validatePassword(UserDomain user, String password) {
        if (password == null || password.trim().isEmpty()) {
            return AuthResult.failure("密码不能为空");
        }

        if (user.verifyPassword(password)) {
            log.info("用户密码验证成功 - 用户: {}", user.getUsername());
            return AuthResult.success("密码验证成功");
        } else {
            log.warn("用户密码验证失败 - 用户: {}", user.getUsername());
            return AuthResult.failure("密码错误");
        }
    }

    /**
     * 验证短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    private AuthResult validateSmsCode(String phone, String code) {
        if (phone == null || phone.trim().isEmpty()) {
            return AuthResult.failure("手机号不能为空");
        }

        if (code == null || code.trim().isEmpty()) {
            return AuthResult.failure("验证码不能为空");
        }

        boolean isValid = smsService.verifyCode(phone, code);
        if (isValid) {
            log.info("短信验证码验证成功 - 手机号: {}", phone);
            return AuthResult.success("验证码验证成功");
        } else {
            log.warn("短信验证码验证失败 - 手机号: {}", phone);
            return AuthResult.failure("验证码错误或已过期");
        }
    }

    /**
     * 验证用户注册信息
     * @param username 用户名
     * @param password 密码
     * @param phone 手机号
     * @param email 邮箱
     * @return 验证结果
     */
    public AuthResult validateRegistration(String username, String password, String phone, String email) {
        if (username == null || username.trim().isEmpty()) {
            return AuthResult.failure("用户名不能为空");
        }

        if (password == null || password.trim().isEmpty()) {
            return AuthResult.failure("密码不能为空");
        }

        if (password.length() < 6) {
            return AuthResult.failure("密码长度不能少于6位");
        }

        if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
            return AuthResult.failure("手机号格式不正确");
        }

        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return AuthResult.failure("邮箱格式不正确");
        }

        return AuthResult.success("注册信息验证通过");
    }

    /**
     * 创建新用户
     * @param username 用户名
     * @param password 密码
     * @param phone 手机号
     * @param email 邮箱
     * @return 用户领域对象
     */
    public UserDomain createNewUser(String username, String password, String phone, String email) {
        AuthResult validationResult = validateRegistration(username, password, phone, email);
        if (!validationResult.isSuccess()) {
            throw new IllegalArgumentException(validationResult.getMessage());
        }

        UserDomain userDomain = UserDomain.createUser(username, password, phone, email);
        log.info("创建新用户 - 用户名: {}, 手机号: {}", username, phone);
        return userDomain;
    }

    /**
     * 认证结果类
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;

        private AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static AuthResult success(String message) {
            return new AuthResult(true, message);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
} 