package com.spark.demo.common.util;

import com.spark.demo.BaseTest;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 * 
 * @author spark
 */
class JwtUtilTest extends BaseTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateAccessToken() {
        // 准备测试数据
        Long userId = 1L;
        String username = "testuser";
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("role", "USER");

        // 执行测试
        String token = jwtUtil.generateAccessToken(userId, username, additionalClaims);

        // 验证结果
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // 验证token内容
        Claims claims = jwtUtil.parseToken(token);
        assertEquals(username, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals("access", claims.get("type", String.class));
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void testGenerateRefreshToken() {
        // 准备测试数据
        Long userId = 1L;
        String username = "testuser";

        // 执行测试
        String token = jwtUtil.generateRefreshToken(userId, username);

        // 验证结果
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // 验证token内容
        Claims claims = jwtUtil.parseToken(token);
        assertEquals(username, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals("refresh", claims.get("type", String.class));
    }

    @Test
    void testValidateToken() {
        // 准备测试数据
        String validToken = jwtUtil.generateAccessToken(1L, "testuser", null);
        String invalidToken = "invalid.token.here";

        // 执行测试
        assertTrue(jwtUtil.validateToken(validToken));
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testGetUsernameFromToken() {
        // 准备测试数据
        String username = "testuser";
        String token = jwtUtil.generateAccessToken(1L, username, null);

        // 执行测试
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // 验证结果
        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetUserIdFromToken() {
        // 准备测试数据
        Long userId = 123L;
        String token = jwtUtil.generateAccessToken(userId, "testuser", null);

        // 执行测试
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        // 验证结果
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testGetTokenType() {
        // 准备测试数据
        String accessToken = jwtUtil.generateAccessToken(1L, "testuser", null);
        String refreshToken = jwtUtil.generateRefreshToken(1L, "testuser");

        // 执行测试
        String accessType = jwtUtil.getTokenType(accessToken);
        String refreshType = jwtUtil.getTokenType(refreshToken);

        // 验证结果
        assertEquals("access", accessType);
        assertEquals("refresh", refreshType);
    }

    @Test
    void testTokenExpiration() {
        // 设置短过期时间用于测试
        ReflectionTestUtils.setField(jwtUtil, "expiration", 0); // 0小时，立即过期

        // 生成token
        String token = jwtUtil.generateAccessToken(1L, "testuser", null);

        // 等待一小段时间确保token过期
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 验证token已过期
        assertFalse(jwtUtil.validateToken(token));
        
        // 恢复原始配置
        ReflectionTestUtils.setField(jwtUtil, "expiration", 24);
    }

    @Test
    void testParseInvalidToken() {
        // 测试解析无效token时的异常处理
        assertThrows(RuntimeException.class, () -> {
            jwtUtil.parseToken("invalid.token.format");
        });
    }

    @Test
    void testGetTokenRemainingTime() {
        // 生成token
        String token = jwtUtil.generateAccessToken(1L, "testuser", null);

        // 获取剩余时间
        long remainingTime = jwtUtil.getTokenRemainingTime(token);

        // 验证剩余时间大于0（token未过期）
        assertTrue(remainingTime > 0);
    }

    @Test
    void testIsTokenExpiringSoon() {
        // 生成正常token
        String token = jwtUtil.generateAccessToken(1L, "testuser", null);

        // 验证token不会很快过期
        assertFalse(jwtUtil.isTokenExpiringSoon(token));
    }
} 