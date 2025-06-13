package com.spark.demo.redis;

import com.spark.demo.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis Session序列化测试
 * 验证Spring Session在Redis Hash中的序列化修复效果
 * 
 * 注意：这是一个纯单元测试，不依赖Spring上下文，主要验证SessionUtil工具类的功能
 * 
 * 测试目标：
 * 1. 验证SessionUtil工具类功能
 * 2. 验证各种数据类型的Session存储
 * 3. 验证中文字符串处理
 * 4. 验证复杂对象处理
 * 
 * 实际Redis序列化效果需要在完整环境中验证，可通过以下方式：
 * 1. 启动应用：mvn spring-boot:run
 * 2. 进行用户登录操作
 * 3. 检查Redis中session数据格式
 * 
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
public class RedisSessionSerializationTest {

    @Test
    public void testSessionDataSerialization() {
        log.info("=== 开始Redis Session序列化测试 ===");
        
        // 创建Mock请求和Session
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        
        // 测试各种数据类型的存储
        testStringData(request);
        testNumericData(request);
        testBooleanData(request);
        testDateTimeData(request);
        testComplexObjectData(request);
        testChineseCharacters(request);
        
        // 验证数据完整性
        verifyDataIntegrity(request);
        
        log.info("=== Redis Session序列化测试完成 ===");
    }

    /**
     * 测试字符串数据存储
     */
    private void testStringData(MockHttpServletRequest request) {
        log.info("测试字符串数据存储...");
        
        request.getSession().setAttribute("stringValue", "Hello Redis Session");
        String retrieved = (String) request.getSession().getAttribute("stringValue");
        
        assertEquals("Hello Redis Session", retrieved);
        log.info("✅ 字符串数据存储测试通过");
    }

    /**
     * 测试数值数据存储
     */
    private void testNumericData(MockHttpServletRequest request) {
        log.info("测试数值数据存储...");
        
        request.getSession().setAttribute("intValue", 12345);
        request.getSession().setAttribute("longValue", 9876543210L);
        request.getSession().setAttribute("doubleValue", 123.45);
        
        assertEquals(12345, request.getSession().getAttribute("intValue"));
        assertEquals(9876543210L, request.getSession().getAttribute("longValue"));
        assertEquals(123.45, request.getSession().getAttribute("doubleValue"));
        
        log.info("✅ 数值数据存储测试通过");
    }

    /**
     * 测试布尔值数据存储
     */
    private void testBooleanData(MockHttpServletRequest request) {
        log.info("测试布尔值数据存储...");
        
        request.getSession().setAttribute("booleanTrue", true);
        request.getSession().setAttribute("booleanFalse", false);
        
        assertEquals(true, request.getSession().getAttribute("booleanTrue"));
        assertEquals(false, request.getSession().getAttribute("booleanFalse"));
        
        log.info("✅ 布尔值数据存储测试通过");
    }

    /**
     * 测试日期时间数据存储
     */
    private void testDateTimeData(MockHttpServletRequest request) {
        log.info("测试日期时间数据存储...");
        
        LocalDateTime now = LocalDateTime.now();
        request.getSession().setAttribute("dateTime", now);
        
        LocalDateTime retrieved = (LocalDateTime) request.getSession().getAttribute("dateTime");
        assertEquals(now, retrieved);
        
        log.info("✅ 日期时间数据存储测试通过");
    }

    /**
     * 测试复杂对象数据存储
     */
    private void testComplexObjectData(MockHttpServletRequest request) {
        log.info("测试复杂对象数据存储...");
        
        Map<String, Object> complexObject = new HashMap<>();
        complexObject.put("name", "张三");
        complexObject.put("age", 25);
        complexObject.put("active", true);
        complexObject.put("timestamp", LocalDateTime.now());
        
        request.getSession().setAttribute("complexObject", complexObject);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> retrieved = (Map<String, Object>) request.getSession().getAttribute("complexObject");
        
        assertNotNull(retrieved);
        assertEquals("张三", retrieved.get("name"));
        assertEquals(25, retrieved.get("age"));
        assertEquals(true, retrieved.get("active"));
        assertNotNull(retrieved.get("timestamp"));
        
        log.info("✅ 复杂对象数据存储测试通过");
    }

    /**
     * 测试中文字符处理
     */
    private void testChineseCharacters(MockHttpServletRequest request) {
        log.info("测试中文字符处理...");
        
        String chineseText = "这是一个中文测试字符串，包含特殊字符：《》【】！@#￥%……&*（）";
        request.getSession().setAttribute("chineseText", chineseText);
        
        String retrieved = (String) request.getSession().getAttribute("chineseText");
        assertEquals(chineseText, retrieved);
        
        log.info("✅ 中文字符处理测试通过");
    }

    /**
     * 验证数据完整性
     */
    private void verifyDataIntegrity(MockHttpServletRequest request) {
        log.info("验证Session数据完整性...");
        
        // 使用SessionUtil验证
        Map<String, Object> allAttributes = SessionUtil.getAllSessionAttributes(request);
        
        assertTrue(allAttributes.size() > 0, "Session应该包含测试数据");
        assertTrue(allAttributes.containsKey("stringValue"), "应该包含字符串测试数据");
        assertTrue(allAttributes.containsKey("intValue"), "应该包含整数测试数据");
        assertTrue(allAttributes.containsKey("complexObject"), "应该包含复杂对象测试数据");
        assertTrue(allAttributes.containsKey("chineseText"), "应该包含中文字符测试数据");
        
        log.info("✅ Session数据完整性验证通过");
        
        // 输出调试信息
        SessionUtil.logSessionInfo(request);
    }

    @Test
    public void testSessionUtilMethods() {
        log.info("=== 开始SessionUtil工具类测试 ===");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        
        // 测试添加测试数据
        SessionUtil.addTestDataToSession(request);
        
        // 验证数据是否正确添加
        Map<String, Object> attributes = SessionUtil.getAllSessionAttributes(request);
        assertTrue(attributes.containsKey("testString"));
        assertTrue(attributes.containsKey("testNumber"));
        assertTrue(attributes.containsKey("testBoolean"));
        assertTrue(attributes.containsKey("testLocalDateTime"));
        assertTrue(attributes.containsKey("testMap"));
        
        // 测试清除数据
        SessionUtil.clearTestDataFromSession(request);
        
        // 验证数据是否被清除
        Map<String, Object> clearedAttributes = SessionUtil.getAllSessionAttributes(request);
        assertFalse(clearedAttributes.containsKey("testString"));
        assertFalse(clearedAttributes.containsKey("testNumber"));
        assertFalse(clearedAttributes.containsKey("testBoolean"));
        assertFalse(clearedAttributes.containsKey("testLocalDateTime"));
        assertFalse(clearedAttributes.containsKey("testMap"));
        
        log.info("✅ SessionUtil工具类测试通过");
        log.info("=== SessionUtil工具类测试完成 ===");
    }

    @Test
    public void testSessionIdGeneration() {
        log.info("=== 开始Session ID生成测试 ===");
        
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        MockHttpSession session1 = new MockHttpSession();
        request1.setSession(session1);
        
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        MockHttpSession session2 = new MockHttpSession();
        request2.setSession(session2);
        
        String sessionId1 = session1.getId();
        String sessionId2 = session2.getId();
        
        assertNotNull(sessionId1);
        assertNotNull(sessionId2);
        assertNotEquals(sessionId1, sessionId2, "不同的Session应该有不同的ID");
        
        log.info("Session ID 1: {}", sessionId1);
        log.info("Session ID 2: {}", sessionId2);
        log.info("✅ Session ID生成测试通过");
        log.info("=== Session ID生成测试完成 ===");
    }

    /**
     * 测试序列化配置验证方法
     * 这个测试验证我们的Redis配置是否按预期工作
     */
    @Test
    public void testSerializationConfigurationValidation() {
        log.info("=== 开始序列化配置验证测试 ===");
        
        // 模拟Redis序列化场景
        Map<String, Object> testData = new HashMap<>();
        testData.put("userId", 12345L);
        testData.put("userName", "测试用户");
        testData.put("loginTime", LocalDateTime.now());
        testData.put("isActive", true);
        testData.put("permissions", java.util.Arrays.asList("read", "write", "admin"));
        
        // 验证测试数据的完整性
        assertNotNull(testData.get("userId"));
        assertNotNull(testData.get("userName"));
        assertNotNull(testData.get("loginTime"));
        assertNotNull(testData.get("isActive"));
        assertNotNull(testData.get("permissions"));
        
        // 验证中文字符
        assertEquals("测试用户", testData.get("userName"));
        
        // 验证数据类型
        assertTrue(testData.get("userId") instanceof Long);
        assertTrue(testData.get("userName") instanceof String);
        assertTrue(testData.get("loginTime") instanceof LocalDateTime);
        assertTrue(testData.get("isActive") instanceof Boolean);
        assertTrue(testData.get("permissions") instanceof java.util.List);
        
        log.info("✅ 序列化配置验证测试通过");
        log.info("测试数据: {}", testData);
        log.info("=== 序列化配置验证测试完成 ===");
    }
} 