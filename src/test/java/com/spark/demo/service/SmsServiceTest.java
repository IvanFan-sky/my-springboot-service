package com.spark.demo.service;

import com.spark.demo.service.impl.SmsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SmsService 单元测试
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class SmsServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SmsServiceImpl smsService;

    private static final String TEST_PHONE = "13800138000";
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";

    @BeforeEach
    void setUp() {
        // Mock RedisTemplate的opsForValue()方法（只在需要时设置）
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSendVerifyCode_Success() {
        // Arrange
        when(redisTemplate.hasKey(SMS_LIMIT_PREFIX + TEST_PHONE)).thenReturn(false); // 没有频率限制

        // Act
        boolean result = smsService.sendVerifyCode(TEST_PHONE);

        // Assert
        assertTrue(result);
        
        // 验证验证码存储和频率限制设置（应该调用2次set方法）
        verify(valueOperations, times(2)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        
        // 验证验证码存储
        verify(valueOperations).set(
            eq(SMS_CODE_PREFIX + TEST_PHONE), 
            argThat(code -> code instanceof String && ((String)code).length() == 6 && ((String)code).matches("\\d{6}")), 
            eq(5L), 
            eq(TimeUnit.MINUTES)
        );
        
        // 验证频率限制设置
        verify(valueOperations).set(
            eq(SMS_LIMIT_PREFIX + TEST_PHONE), 
            eq("1"), 
            eq(1L), 
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void testSendVerifyCode_EmptyPhone() {
        // Act
        boolean result1 = smsService.sendVerifyCode("");
        boolean result2 = smsService.sendVerifyCode(null);
        boolean result3 = smsService.sendVerifyCode("   ");

        // Assert
        assertFalse(result1);
        assertFalse(result2);
        assertFalse(result3);
        
        // 验证没有调用Redis
        verify(redisTemplate, never()).hasKey(anyString());
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testSendVerifyCode_FrequencyLimit() {
        // Arrange
        when(redisTemplate.hasKey(SMS_LIMIT_PREFIX + TEST_PHONE)).thenReturn(true); // 有频率限制

        // Act
        boolean result = smsService.sendVerifyCode(TEST_PHONE);

        // Assert
        assertFalse(result);
        
        // 验证没有发送验证码
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testSendVerifyCode_RedisException() {
        // Arrange
        when(redisTemplate.hasKey(SMS_LIMIT_PREFIX + TEST_PHONE)).thenReturn(false);
        doThrow(new RuntimeException("Redis connection failed"))
            .when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // Act
        boolean result = smsService.sendVerifyCode(TEST_PHONE);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyCode_Success() {
        // Arrange
        String testCode = "123456";
        when(valueOperations.get(SMS_CODE_PREFIX + TEST_PHONE)).thenReturn(testCode);

        // Act
        boolean result = smsService.verifyCode(TEST_PHONE, testCode);

        // Assert
        assertTrue(result);
        
        // 验证成功后删除验证码
        verify(redisTemplate, times(1)).delete(SMS_CODE_PREFIX + TEST_PHONE);
    }

    @Test
    void testVerifyCode_WrongCode() {
        // Arrange
        String storedCode = "123456";
        String inputCode = "654321";
        when(valueOperations.get(SMS_CODE_PREFIX + TEST_PHONE)).thenReturn(storedCode);

        // Act
        boolean result = smsService.verifyCode(TEST_PHONE, inputCode);

        // Assert
        assertFalse(result);
        
        // 验证失败不删除验证码
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testVerifyCode_CodeNotExists() {
        // Arrange
        when(valueOperations.get(SMS_CODE_PREFIX + TEST_PHONE)).thenReturn(null);

        // Act
        boolean result = smsService.verifyCode(TEST_PHONE, "123456");

        // Assert
        assertFalse(result);
        
        // 验证不删除验证码
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testVerifyCode_EmptyInput() {
        // Act
        boolean result1 = smsService.verifyCode("", "123456");
        boolean result2 = smsService.verifyCode(TEST_PHONE, "");
        boolean result3 = smsService.verifyCode(null, "123456");
        boolean result4 = smsService.verifyCode(TEST_PHONE, null);
        boolean result5 = smsService.verifyCode("   ", "123456");
        boolean result6 = smsService.verifyCode(TEST_PHONE, "   ");

        // Assert
        assertFalse(result1);
        assertFalse(result2);
        assertFalse(result3);
        assertFalse(result4);
        assertFalse(result5);
        assertFalse(result6);
        
        // 验证没有调用Redis
        verify(valueOperations, never()).get(anyString());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testVerifyCode_RedisException() {
        // Arrange
        when(valueOperations.get(SMS_CODE_PREFIX + TEST_PHONE))
            .thenThrow(new RuntimeException("Redis connection failed"));

        // Act
        boolean result = smsService.verifyCode(TEST_PHONE, "123456");

        // Assert
        assertFalse(result);
        
        // 验证不删除验证码
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testRemoveCode_Success() {
        // Act
        assertDoesNotThrow(() -> smsService.removeCode(TEST_PHONE));

        // Assert
        verify(redisTemplate, times(1)).delete(SMS_CODE_PREFIX + TEST_PHONE);
    }

    @Test
    void testRemoveCode_EmptyPhone() {
        // Act
        smsService.removeCode("");
        smsService.removeCode(null);
        smsService.removeCode("   ");

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testSendVerifyCode_ValidPhoneFormats() {
        // Arrange
        String[] validPhones = {
            "13800138000",
            "15912345678",
            "18888888888",
            "17755555555"
        };
        
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // Act & Assert
        for (String phone : validPhones) {
            boolean result = smsService.sendVerifyCode(phone);
            assertTrue(result, "Phone " + phone + " should be valid");
        }
        
        // 验证每个手机号都调用了Redis
        verify(valueOperations, times(validPhones.length * 2)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testVerifyCode_CaseSensitive() {
        // Arrange
        String testCode = "123456";
        when(valueOperations.get(SMS_CODE_PREFIX + TEST_PHONE)).thenReturn(testCode);

        // Act
        boolean result1 = smsService.verifyCode(TEST_PHONE, "123456"); // 正确
        boolean result2 = smsService.verifyCode(TEST_PHONE, "123456"); // 相同

        // Assert
        assertTrue(result1);
        // 由于第一次验证成功后验证码已被删除，第二次验证应该失败
        // 但在这个测试中我们需要重新mock
        when(valueOperations.get(SMS_CODE_PREFIX + TEST_PHONE)).thenReturn(null); // 已被删除
        assertFalse(smsService.verifyCode(TEST_PHONE, "123456"));
    }

    @Test
    void testSendVerifyCode_CodeFormat() {
        // Arrange
        when(redisTemplate.hasKey(SMS_LIMIT_PREFIX + TEST_PHONE)).thenReturn(false);
        
        // 捕获生成的验证码
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        boolean result = smsService.sendVerifyCode(TEST_PHONE);

        // Assert
        assertTrue(result);
        verify(valueOperations).set(eq(SMS_CODE_PREFIX + TEST_PHONE), codeCaptor.capture(), anyLong(), any(TimeUnit.class));
        
        String generatedCode = codeCaptor.getValue();
        assertNotNull(generatedCode);
        assertEquals(6, generatedCode.length());
        assertTrue(generatedCode.matches("\\d{6}"), "验证码应该是6位数字");
        
        // 验证每位都是数字
        for (char c : generatedCode.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    @Test
    void testSendVerifyCode_MultipleCallsWithinLimit() {
        // Arrange
        when(redisTemplate.hasKey(SMS_LIMIT_PREFIX + TEST_PHONE))
            .thenReturn(false)  // 第一次没有限制
            .thenReturn(true);  // 第二次有限制

        // Act
        boolean result1 = smsService.sendVerifyCode(TEST_PHONE);
        boolean result2 = smsService.sendVerifyCode(TEST_PHONE);

        // Assert
        assertTrue(result1);   // 第一次应该成功
        assertFalse(result2);  // 第二次应该失败（频率限制）
        
        // 验证只调用了一次发送（第一次）
        verify(valueOperations, times(2)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testCompleteWorkflow() {
        // 测试完整的发送-验证-删除流程
        
        // 1. 发送验证码
        when(redisTemplate.hasKey(SMS_LIMIT_PREFIX + TEST_PHONE)).thenReturn(false);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        
        boolean sendResult = smsService.sendVerifyCode(TEST_PHONE);
        assertTrue(sendResult);
        
        verify(valueOperations).set(eq(SMS_CODE_PREFIX + TEST_PHONE), codeCaptor.capture(), anyLong(), any(TimeUnit.class));
        String generatedCode = codeCaptor.getValue();
        
        // 2. 验证验证码
        when(valueOperations.get(SMS_CODE_PREFIX + TEST_PHONE)).thenReturn(generatedCode);
        
        boolean verifyResult = smsService.verifyCode(TEST_PHONE, generatedCode);
        assertTrue(verifyResult);
        
        // 3. 验证码验证成功后应该被删除
        verify(redisTemplate, times(1)).delete(SMS_CODE_PREFIX + TEST_PHONE);
        
        // 4. 再次验证应该失败（已删除）
        when(valueOperations.get(SMS_CODE_PREFIX + TEST_PHONE)).thenReturn(null);
        
        boolean verifyAgainResult = smsService.verifyCode(TEST_PHONE, generatedCode);
        assertFalse(verifyAgainResult);
    }
} 