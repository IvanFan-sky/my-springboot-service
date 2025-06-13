package com.spark.demo.config;

import com.spark.demo.util.RedisUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis序列化测试
 * 验证修复后的Redis配置是否能正确处理序列化，避免乱码问题
 * 
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@ActiveProfiles("test")
@SpringBootTest
public class RedisSerializationTest {

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void testStringSerializationWorks() {
        String key = "test:string";
        String value = "Hello Redis 中文测试!";
        
        // 存储
        redisUtil.setString(key, value);
        
        // 获取
        String retrieved = redisUtil.getString(key);
        
        // 验证
        assertEquals(value, retrieved);
        log.info("字符串序列化测试通过: {}", retrieved);
        
        // 清理
        redisUtil.delete(key);
    }

    @Test
    public void testObjectSerializationWorks() {
        String key = "test:object";
        TestUser user = new TestUser(1L, "张三", "test@example.com", LocalDateTime.now());
        
        // 存储
        redisUtil.setObject(key, user);
        
        // 获取
        Object retrieved = redisUtil.getObject(key);
        assertNotNull(retrieved);
        
        // 转换为目标类型
        TestUser retrievedUser = redisUtil.getObject(key, TestUser.class);
        assertNotNull(retrievedUser);
        assertEquals(user.getId(), retrievedUser.getId());
        assertEquals(user.getName(), retrievedUser.getName());
        assertEquals(user.getEmail(), retrievedUser.getEmail());
        
        log.info("对象序列化测试通过: {}", retrievedUser);
        
        // 清理
        redisUtil.delete(key);
    }

    @Test
    public void testMapSerializationWorks() {
        String key = "test:map";
        Map<String, Object> data = new HashMap<>();
        data.put("name", "测试用户");
        data.put("age", 25);
        data.put("active", true);
        data.put("timestamp", LocalDateTime.now());
        
        // 存储
        redisUtil.setObject(key, data);
        
        // 获取
        Object retrieved = redisUtil.getObject(key);
        assertNotNull(retrieved);
        
        log.info("Map序列化测试通过: {}", retrieved);
        
        // 清理
        redisUtil.delete(key);
    }

    @Test
    public void testVerifyCodeFlow() {
        String phone = "13800138000";
        String code = "123456";
        
        // 使用RedisUtil的推荐方法存储验证码
        redisUtil.setVerifyCode(phone, code, 5);
        
        // 获取验证码
        String retrievedCode = redisUtil.getVerifyCode(phone);
        assertEquals(code, retrievedCode);
        
        log.info("验证码流程测试通过: phone={}, code={}", phone, retrievedCode);
        
        // 清理
        redisUtil.delete("verify_code:" + phone);
    }

    @Test
    public void testSessionFlow() {
        String sessionId = "test-session-123";
        TestUser user = new TestUser(1L, "会话用户", "session@example.com", LocalDateTime.now());
        
        // 存储Session
        redisUtil.setUserSession(sessionId, user, 30);
        
        // 获取Session
        Object sessionData = redisUtil.getUserSession(sessionId);
        assertNotNull(sessionData);
        
        log.info("Session流程测试通过: sessionId={}, data={}", sessionId, sessionData);
        
        // 清理
        redisUtil.delete("session:" + sessionId);
    }

    @Test
    public void testHashOperations() {
        String key = "test:hash";
        
        // 存储Hash数据
        redisUtil.hashSet(key, "field1", "value1");
        redisUtil.hashSet(key, "field2", "中文值");
        redisUtil.hashSet(key, "field3", 123);
        
        // 获取Hash数据
        Object value1 = redisUtil.hashGet(key, "field1");
        Object value2 = redisUtil.hashGet(key, "field2");
        Object value3 = redisUtil.hashGet(key, "field3");
        
        assertEquals("value1", value1);
        assertEquals("中文值", value2);
        assertNotNull(value3);
        
        // 检查是否存在
        assertTrue(redisUtil.hashHasKey(key, "field1"));
        assertFalse(redisUtil.hashHasKey(key, "nonexistent"));
        
        log.info("Hash操作测试通过: value1={}, value2={}, value3={}", value1, value2, value3);
        
        // 清理
        redisUtil.delete(key);
    }

    @Test
    public void testExpirationWorks() {
        String key = "test:expiration";
        String value = "将要过期的值";
        
        // 设置1秒过期
        redisUtil.setString(key, value, 1, java.util.concurrent.TimeUnit.SECONDS);
        
        // 立即检查
        assertTrue(redisUtil.hasKey(key));
        assertEquals(value, redisUtil.getString(key));
        
        // 等待过期
        try {
            Thread.sleep(1100); // 等待1.1秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 检查是否已过期
        assertFalse(redisUtil.hasKey(key));
        assertNull(redisUtil.getString(key));
        
        log.info("过期时间测试通过");
    }

    /**
     * 测试用的用户类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestUser {
        private Long id;
        private String name;
        private String email;
        private LocalDateTime createTime;
    }
} 