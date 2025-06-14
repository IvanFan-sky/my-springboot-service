package com.spark.demo.modules.auth.service;

import com.spark.demo.common.util.JwtUtil;
import com.spark.demo.dto.PasswordLoginDTO;
import com.spark.demo.dto.SmsLoginDTO;
import com.spark.demo.entity.User;
import com.spark.demo.service.UserService;
import com.spark.demo.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 * 提供基于Spring Security的JWT认证功能
 * 
 * @author spark
 * @since 2025-06-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final SmsService smsService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 密码登录并生成JWT token
     * 
     * @param loginDTO 密码登录DTO
     * @return JWT token信息
     */
    public Map<String, Object> passwordLogin(PasswordLoginDTO loginDTO) {
        log.info("用户密码登录尝试: {}", loginDTO.getUsername());

        try {
            // 使用Spring Security进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
                )
            );

            // 设置认证上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            // 生成JWT tokens
            Map<String, Object> additionalClaims = new HashMap<>();
            additionalClaims.put("role", user.getRole());
            additionalClaims.put("uuid", user.getUuid());

            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone(), additionalClaims);
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", 86400); // 24小时，单位：秒
            result.put("user", createUserInfo(user));

            log.info("用户密码登录成功: {}", user.getUsername());
            return result;

        } catch (Exception e) {
            log.error("用户密码登录失败: {}", loginDTO.getUsername(), e);
            throw new RuntimeException("用户名或密码错误");
        }
    }

    /**
     * 短信验证码登录并生成JWT token
     * 
     * @param loginDTO 短信登录DTO
     * @return JWT token信息
     */
    public Map<String, Object> smsLogin(SmsLoginDTO loginDTO) {
        log.info("用户短信登录尝试: {}", loginDTO.getPhone());

        try {
            // 验证短信验证码
            boolean isValid = smsService.verifyCode(loginDTO.getPhone(), loginDTO.getCode());
            if (!isValid) {
                throw new RuntimeException("验证码错误或已过期");
            }

            // 查找用户
            User user = findUserByPhone(loginDTO.getPhone());
            if (user == null) {
                throw new RuntimeException("该手机号未注册");
            }

            if (user.getStatus() == null || user.getStatus() != 1) {
                throw new RuntimeException("账号已被禁用");
            }

            // 生成JWT tokens
            Map<String, Object> additionalClaims = new HashMap<>();
            additionalClaims.put("role", user.getRole());
            additionalClaims.put("uuid", user.getUuid());

            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone(), additionalClaims);
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", 86400);
            result.put("user", createUserInfo(user));

            log.info("用户短信登录成功: {}", user.getUsername());
            return result;

        } catch (Exception e) {
            log.error("用户短信登录失败: {}", loginDTO.getPhone(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 刷新JWT token
     * 
     * @param refreshToken 刷新token
     * @return 新的JWT token信息
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            // 验证刷新token
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("刷新token无效");
            }

            // 检查token类型
            String tokenType = jwtUtil.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new RuntimeException("token类型错误");
            }

            // 获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            User user = userService.getById(userId);
            if (user == null || user.getStatus() != 1) {
                throw new RuntimeException("用户不存在或已被禁用");
            }

            // 生成新的tokens
            Map<String, Object> additionalClaims = new HashMap<>();
            additionalClaims.put("role", user.getRole());
            additionalClaims.put("uuid", user.getUuid());

            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone(), additionalClaims);
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", 86400);

            log.info("用户token刷新成功: {}", user.getUsername());
            return result;

        } catch (Exception e) {
            log.error("token刷新失败", e);
            throw new RuntimeException("token刷新失败: " + e.getMessage());
        }
    }

    /**
     * 根据手机号查找用户
     */
    private User findUserByPhone(String phone) {
        return userService.lambdaQuery()
                .eq(User::getPhone, phone)
                .isNull(User::getDeletedTime)
                .one();
    }

    /**
     * 创建用户信息对象（脱敏处理）
     */
    private Map<String, Object> createUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("uuid", user.getUuid());
        userInfo.put("username", user.getUsername());
        userInfo.put("phone", maskPhone(user.getPhone()));
        userInfo.put("email", maskEmail(user.getEmail()));
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());
        userInfo.put("status", user.getStatus());
        return userInfo;
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        if (username.length() <= 2) {
            return email;
        }
        return username.substring(0, 2) + "***@" + parts[1];
    }
} 