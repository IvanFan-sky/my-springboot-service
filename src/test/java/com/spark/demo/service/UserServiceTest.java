package com.spark.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.demo.common.exception.BusinessException;
import com.spark.demo.dto.LoginDTO;
import com.spark.demo.dto.UserDTO;
import com.spark.demo.entity.User;
import com.spark.demo.mapper.UserMapper;
import com.spark.demo.service.impl.UserServiceImpl;
import com.spark.demo.vo.UserVO;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
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

        testUserDTO = new UserDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setPassword("123456");
        testUserDTO.setPhone("13800138000");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setNickname("测试用户");

        // Mock HTTP请求和Session
        request = new MockHttpServletRequest();
        session = new MockHttpSession();
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void testRegister_Success() {
        // Arrange
        // Mock 存在性检查（都返回0表示不存在）
        lenient().when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> userService.register(testUserDTO));

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).insert(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertEquals(testUserDTO.getUsername(), capturedUser.getUsername());
        assertNotNull(capturedUser.getUuid());
        assertNotEquals(testUserDTO.getPassword(), capturedUser.getPassword()); // 密码应该被加密
        assertEquals("user", capturedUser.getRole()); // 默认角色
        assertEquals(1, capturedUser.getStatus()); // 默认状态
    }

    @Test
    void testRegister_UsernameExists() {
        // Arrange
        // 第一次查询（用户名检查）返回1表示存在，后续查询返回0
        when(userMapper.selectCount(any(LambdaQueryWrapper.class)))
            .thenReturn(1L)  // 用户名存在
            .thenReturn(0L)  // 手机号不存在
            .thenReturn(0L); // 邮箱不存在

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(testUserDTO));
        assertEquals("用户名已存在", exception.getMessage());
        
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testRegister_InvalidUsername() {
        // Arrange
        testUserDTO.setUsername("ab"); // 用户名过短

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(testUserDTO));
        assertTrue(exception.getMessage().contains("用户名"));
        
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testRegister_InvalidPhone() {
        // Arrange
        testUserDTO.setPhone("12345"); // 无效手机号

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(testUserDTO));
        assertTrue(exception.getMessage().contains("手机号"));
        
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testRegister_InvalidEmail() {
        // Arrange
        testUserDTO.setEmail("invalid-email"); // 无效邮箱

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(testUserDTO));
        assertTrue(exception.getMessage().contains("邮箱"));
        
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testLogin_PasswordSuccess() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        // Mock findUserByUsernameOrPhone方法（通过selectOne查询）
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // Act
        String sessionId = userService.login(loginDTO);

        // Assert
        assertNotNull(sessionId);
        assertEquals(testUser.getUuid(), session.getAttribute("userUuid"));
        assertEquals(testUser.getId(), session.getAttribute("userId"));
        assertEquals(testUser.getUsername(), session.getAttribute("username"));
        assertEquals(testUser.getRole(), session.getAttribute("role"));
        
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testLogin_SmsCodeSuccess() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setCode("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(smsService.verifyCode(testUser.getPhone(), "123456")).thenReturn(true);

        // Act
        String sessionId = userService.login(loginDTO);

        // Assert
        assertNotNull(sessionId);
        assertEquals(testUser.getUuid(), session.getAttribute("userUuid"));
        
        verify(smsService, times(1)).verifyCode(testUser.getPhone(), "123456");
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistent");
        loginDTO.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.login(loginDTO));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testLogin_UserDisabled() {
        // Arrange
        testUser.setStatus(0); // 禁用状态
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.login(loginDTO));
        assertEquals("账号已被禁用", exception.getMessage());
    }

    @Test
    void testLogin_WrongPassword() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpassword");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.login(loginDTO));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testGetCurrentUserInfo_Success() {
        // Arrange
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // Act
        UserVO userVO = userService.getCurrentUserInfo(1L);

        // Assert
        assertNotNull(userVO);
        assertEquals(testUser.getUuid(), userVO.getUuid());
        assertEquals(testUser.getUsername(), userVO.getUsername());
        assertEquals(testUser.getPhone(), userVO.getPhone());
        assertEquals(testUser.getEmail(), userVO.getEmail());
        
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetCurrentUserInfo_UserNotFound() {
        // Arrange
        when(userMapper.selectById(1L)).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.getCurrentUserInfo(1L));
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testGetUserByUuid_Success() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // Act
        UserVO userVO = userService.getUserByUuid("test-uuid-1234");

        // Assert
        assertNotNull(userVO);
        assertEquals(testUser.getUuid(), userVO.getUuid());
        assertEquals(testUser.getUsername(), userVO.getUsername());
        
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetUserByUuid_UserNotFound() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.getUserByUuid("nonexistent-uuid"));
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testAddUser_Success() {
        // Arrange
        // Mock所有的唯一性检查都返回0（不存在重复）
        lenient().when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // 模拟插入后的ID
            return 1;
        });

        // Act
        UserVO userVO = userService.addUser(testUserDTO);

        // Assert
        assertNotNull(userVO);
        assertEquals(testUserDTO.getUsername(), userVO.getUsername());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testDeleteUserByUuid_Success() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> userService.deleteUserByUuid("test-uuid-1234"));

        // Assert
        verify(userMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUserByUuid_UserNotFound() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.deleteUserByUuid("nonexistent-uuid"));
        assertEquals("用户不存在", exception.getMessage());
        
        verify(userMapper, never()).deleteById(any());
    }

    @Test
    void testUpdateUserByUuid_Success() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        updateDTO.setNickname("更新后的昵称");
        updateDTO.setEmail("newemail@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUuid("test-uuid-1234");
        updatedUser.setUsername("testuser");
        updatedUser.setPassword(encryptPassword("123456"));
        updatedUser.setPhone("13800138000");
        updatedUser.setEmail("newemail@example.com");
        updatedUser.setNickname("更新后的昵称");
        updatedUser.setRole("user");
        updatedUser.setStatus(1);

        // Mock查询用户存在
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
            .thenReturn(testUser); // 第一次：根据UUID查找用户
        // Mock邮箱不重复
        lenient().when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(updatedUser); // Mock更新后的查询

        // Act
        UserVO userVO = userService.updateUserByUuid("test-uuid-1234", updateDTO);

        // Assert
        assertNotNull(userVO);
        assertEquals("更新后的昵称", userVO.getNickname());
        verify(userMapper, times(1)).updateById(any(User.class));
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    void testListUsers_Success() {
        // Arrange
        Page<User> pageRequest = new Page<>(1, 10);
        UserDTO userFilter = new UserDTO();
        userFilter.setUsername("test");

        List<User> userList = Arrays.asList(testUser);
        Page<User> userPage = new Page<>(1, 10);
        userPage.setRecords(userList);
        userPage.setTotal(1);

        when(userMapper.selectPage(eq(pageRequest), any(LambdaQueryWrapper.class))).thenReturn(userPage);

        // Act
        Page<UserVO> result = userService.listUsers(pageRequest, userFilter);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(testUser.getUuid(), result.getRecords().get(0).getUuid());
        
        verify(userMapper, times(1)).selectPage(eq(pageRequest), any(LambdaQueryWrapper.class));
    }

    @Test
    void testFindByUuid_Success() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // Act
        User user = userService.findByUuid("test-uuid-1234");

        // Assert
        assertNotNull(user);
        assertEquals(testUser.getUuid(), user.getUuid());
        assertEquals(testUser.getUsername(), user.getUsername());
        
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testFindByUuid_UserNotFound() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act
        User user = userService.findByUuid("nonexistent-uuid");

        // Assert
        assertNull(user);
    }

    @Test
    void testChangePassword_Success() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> userService.changePassword("test-uuid-1234", "123456", "newpassword123"));

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).updateById(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        // 验证新密码哈希与原密码哈希不同
        String newPasswordHash = encryptPassword("newpassword123");
        String originalPasswordHash = encryptPassword("123456");
        assertEquals(newPasswordHash, capturedUser.getPassword());
        assertNotEquals(originalPasswordHash, capturedUser.getPassword()); // 新密码与原密码不同
    }

    @Test
    void testChangePassword_UserNotFound() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.changePassword("nonexistent-uuid", "123456", "newpassword"));
        assertEquals("用户不存在", exception.getMessage());
        
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.changePassword("test-uuid-1234", "wrongpassword", "newpassword"));
        assertEquals("旧密码错误", exception.getMessage());
        
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void testChangePassword_InvalidNewPassword() {
        // Arrange
        lenient().when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.changePassword("test-uuid-1234", "123456", "123")); // 新密码过短
        assertTrue(exception.getMessage().contains("密码"));
        
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void testLogin_EmptyUsername() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("");
        loginDTO.setPassword("123456");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.login(loginDTO));
        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    void testLogin_EmptyPasswordAndCode() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        // 密码和验证码都为空

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.login(loginDTO));
        assertEquals("密码或验证码不能为空", exception.getMessage());
    }

    @Test
    void testRegister_NullUserDTO() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> userService.register(null));
    }

    @Test
    void testRegister_EmptyPassword() {
        // Arrange
        testUserDTO.setPassword("");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(testUserDTO));
        assertTrue(exception.getMessage().contains("密码"));
    }
} 