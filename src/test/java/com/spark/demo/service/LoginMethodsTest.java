package com.spark.demo.service;

import com.spark.demo.common.exception.BusinessException;
import com.spark.demo.dto.PasswordLoginDTO;
import com.spark.demo.dto.SmsLoginDTO;
import com.spark.demo.entity.User;
import com.spark.demo.mapper.UserMapper;
import com.spark.demo.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 登录方法拆分测试
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class LoginMethodsTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private MockHttpServletRequest request;
    private MockHttpSession session;

    /**
     * 加密密码（与UserServiceImpl中的方法一致）
     */
    private String encryptPassword(String plainPassword) {
        String salt = "spark_demo_salt";
        return DigestUtils.md5DigestAsHex((plainPassword + salt).getBytes());
    }

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUuid("test-uuid-1234");
        testUser.setUsername("testuser");
        // 使用与UserServiceImpl相同的密码加密方法
        testUser.setPassword(encryptPassword("123456"));
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setNickname("测试用户");
        testUser.setRole("user");
        testUser.setStatus(1);
        testUser.setCreatedTime(new Date());
        testUser.setUpdatedTime(new Date());

        // Mock HTTP请求和Session
        request = new MockHttpServletRequest();
        session = new MockHttpSession();
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void testPasswordLogin_Success() {
        // Arrange
        PasswordLoginDTO passwordLoginDTO = new PasswordLoginDTO();
        passwordLoginDTO.setUsername("testuser");
        passwordLoginDTO.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        // Act
        String sessionId = userService.passwordLogin(passwordLoginDTO);

        // Assert
        assertNotNull(sessionId);
        assertEquals(testUser.getUuid(), session.getAttribute("userUuid"));
        assertEquals(testUser.getId(), session.getAttribute("userId"));
        assertEquals(testUser.getUsername(), session.getAttribute("username"));
        assertEquals(testUser.getRole(), session.getAttribute("role"));
        
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    void testPasswordLogin_UserNotFound() {
        // Arrange
        PasswordLoginDTO passwordLoginDTO = new PasswordLoginDTO();
        passwordLoginDTO.setUsername("nonexistent");
        passwordLoginDTO.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.passwordLogin(passwordLoginDTO));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testPasswordLogin_WrongPassword() {
        // Arrange
        PasswordLoginDTO passwordLoginDTO = new PasswordLoginDTO();
        passwordLoginDTO.setUsername("testuser");
        passwordLoginDTO.setPassword("wrongpassword");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.passwordLogin(passwordLoginDTO));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testPasswordLogin_UserDisabled() {
        // Arrange
        testUser.setStatus(0); // 禁用状态
        PasswordLoginDTO passwordLoginDTO = new PasswordLoginDTO();
        passwordLoginDTO.setUsername("testuser");
        passwordLoginDTO.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.passwordLogin(passwordLoginDTO));
        assertEquals("账号已被禁用", exception.getMessage());
    }

    @Test
    void testSmsLogin_Success() {
        // Arrange
        SmsLoginDTO smsLoginDTO = new SmsLoginDTO();
        smsLoginDTO.setPhone("13800138000");
        smsLoginDTO.setCode("123456");

        when(smsService.verifyCode("13800138000", "123456")).thenReturn(true);
        when(userMapper.selectOne(any())).thenReturn(testUser);

        // Act
        String sessionId = userService.smsLogin(smsLoginDTO);

        // Assert
        assertNotNull(sessionId);
        assertEquals(testUser.getUuid(), session.getAttribute("userUuid"));
        assertEquals(testUser.getId(), session.getAttribute("userId"));
        assertEquals(testUser.getUsername(), session.getAttribute("username"));
        assertEquals(testUser.getRole(), session.getAttribute("role"));
        
        verify(smsService, times(1)).verifyCode("13800138000", "123456");
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    void testSmsLogin_InvalidCode() {
        // Arrange
        SmsLoginDTO smsLoginDTO = new SmsLoginDTO();
        smsLoginDTO.setPhone("13800138000");
        smsLoginDTO.setCode("wrong123");

        when(smsService.verifyCode("13800138000", "wrong123")).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.smsLogin(smsLoginDTO));
        assertEquals("验证码错误或已过期", exception.getMessage());
        
        verify(smsService, times(1)).verifyCode("13800138000", "wrong123");
        verify(userMapper, never()).selectOne(any()); // 验证码错误时不应该查询用户
    }

    @Test
    void testSmsLogin_PhoneNotRegistered() {
        // Arrange
        SmsLoginDTO smsLoginDTO = new SmsLoginDTO();
        smsLoginDTO.setPhone("13800138888"); // 未注册的手机号
        smsLoginDTO.setCode("123456");

        when(smsService.verifyCode("13800138888", "123456")).thenReturn(true);
        when(userMapper.selectOne(any())).thenReturn(null); // 手机号未注册

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.smsLogin(smsLoginDTO));
        assertEquals("该手机号未注册", exception.getMessage());
        
        verify(smsService, times(1)).verifyCode("13800138888", "123456");
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    void testSmsLogin_UserDisabled() {
        // Arrange
        testUser.setStatus(0); // 禁用状态
        SmsLoginDTO smsLoginDTO = new SmsLoginDTO();
        smsLoginDTO.setPhone("13800138000");
        smsLoginDTO.setCode("123456");

        when(smsService.verifyCode("13800138000", "123456")).thenReturn(true);
        when(userMapper.selectOne(any())).thenReturn(testUser);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.smsLogin(smsLoginDTO));
        assertEquals("账号已被禁用", exception.getMessage());
    }

    @Test
    void testPasswordLogin_WithPhoneNumber() {
        // Arrange
        PasswordLoginDTO passwordLoginDTO = new PasswordLoginDTO();
        passwordLoginDTO.setUsername("13800138000"); // 使用手机号登录
        passwordLoginDTO.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        // Act
        String sessionId = userService.passwordLogin(passwordLoginDTO);

        // Assert
        assertNotNull(sessionId);
        assertEquals(testUser.getUuid(), session.getAttribute("userUuid"));
        
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    void testPasswordLoginAndSmsLogin_DifferentFlows() {
        // 测试密码登录和短信登录的流程独立性
        
        // 首先测试密码登录
        PasswordLoginDTO passwordLoginDTO = new PasswordLoginDTO();
        passwordLoginDTO.setUsername("testuser");
        passwordLoginDTO.setPassword("123456");
        
        when(userMapper.selectOne(any())).thenReturn(testUser);
        
        String passwordSessionId = userService.passwordLogin(passwordLoginDTO);
        assertNotNull(passwordSessionId);
        
        // 清理session
        session.clearAttributes();
        
        // 然后测试短信登录
        SmsLoginDTO smsLoginDTO = new SmsLoginDTO();
        smsLoginDTO.setPhone("13800138000");
        smsLoginDTO.setCode("123456");
        
        when(smsService.verifyCode("13800138000", "123456")).thenReturn(true);
        
        String smsSessionId = userService.smsLogin(smsLoginDTO);
        assertNotNull(smsSessionId);
        
        // 验证两次登录都设置了正确的session
        assertEquals(testUser.getUuid(), session.getAttribute("userUuid"));
        
        // 验证调用次数
        verify(userMapper, times(2)).selectOne(any()); // 两次查询用户
        verify(smsService, times(1)).verifyCode("13800138000", "123456"); // 一次验证码验证
    }
} 