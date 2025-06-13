package com.spark.demo.service;

import com.spark.demo.entity.User;
import com.spark.demo.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务逻辑删除功能测试
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceLogicDeleteTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testLogicDelete() {
        // 1. 创建测试用户
        User testUser = createTestUser();
        userMapper.insert(testUser);
        
        Long userId = testUser.getId();
        String userUuid = testUser.getUuid();
        
        log.info("创建测试用户 - ID: {}, UUID: {}", userId, userUuid);
        
        // 2. 验证用户存在且未被删除
        User foundUser = userMapper.selectById(userId);
        assertNotNull(foundUser, "用户应该存在");
        assertNull(foundUser.getDeletedTime(), "删除时间应该为null");
        log.info("验证用户存在 - 删除时间: {}", foundUser.getDeletedTime());
        
        // 3. 执行逻辑删除
        Date beforeDelete = new Date();
        userMapper.deleteById(userId);
        Date afterDelete = new Date();
        
        log.info("执行逻辑删除 - 删除前时间: {}, 删除后时间: {}", beforeDelete, afterDelete);
        
        // 4. 验证逻辑删除结果
        // 使用原生SQL查询（绕过逻辑删除过滤）
        User deletedUser = userMapper.selectById(userId);
        // 由于逻辑删除，selectById应该返回null（被自动过滤）
        assertNull(deletedUser, "逻辑删除后，selectById应该返回null");
        
        // 5. 使用自定义方法验证删除时间已设置
        User userWithDeleteTime = userMapper.selectByIdIgnoreLogicDelete(userId);
        assertNotNull(userWithDeleteTime, "用户记录应该还存在于数据库中");
        assertNotNull(userWithDeleteTime.getDeletedTime(), "删除时间应该已设置");
        assertTrue(userWithDeleteTime.getDeletedTime().getTime() >= beforeDelete.getTime(), 
                  "删除时间应该在删除操作时间之后");
        assertTrue(userWithDeleteTime.getDeletedTime().getTime() <= afterDelete.getTime(), 
                  "删除时间应该在删除操作时间之前");
        
        log.info("逻辑删除验证成功 - 删除时间: {}", userWithDeleteTime.getDeletedTime());
        
        // 6. 验证逻辑删除对查询的影响
        User searchResult = userService.findByUuid(userUuid);
        assertNull(searchResult, "通过UUID查找已删除用户应该返回null");
        
        log.info("逻辑删除测试完成");
    }

    @Test
    public void testLogicDeleteByUuid() {
        // 1. 创建测试用户
        User testUser = createTestUser();
        userMapper.insert(testUser);
        
        String userUuid = testUser.getUuid();
        log.info("创建测试用户 - UUID: {}", userUuid);
        
        // 2. 通过Service执行逻辑删除
        userService.deleteUserByUuid(userUuid);
        
        // 3. 验证删除结果
        User deletedUser = userService.findByUuid(userUuid);
        assertNull(deletedUser, "通过UUID删除后，用户应该查找不到");
        
        log.info("通过UUID逻辑删除测试完成");
    }

    @Test
    public void testAutoFillOnInsert() {
        // 测试插入时的自动填充
        User testUser = createTestUser();
        
        // 插入前验证字段为空
        assertNull(testUser.getCreatedTime(), "插入前创建时间应该为null");
        assertNull(testUser.getUpdatedTime(), "插入前更新时间应该为null");
        assertNull(testUser.getDeletedTime(), "插入前删除时间应该为null");
        
        userMapper.insert(testUser);
        
        // 插入后验证自动填充
        assertNotNull(testUser.getCreatedTime(), "插入后创建时间应该自动填充");
        assertNotNull(testUser.getUpdatedTime(), "插入后更新时间应该自动填充");
        assertNull(testUser.getDeletedTime(), "插入后删除时间应该保持null");
        
        log.info("自动填充测试完成 - 创建时间: {}, 更新时间: {}, 删除时间: {}", 
                testUser.getCreatedTime(), testUser.getUpdatedTime(), testUser.getDeletedTime());
    }

    @Test
    public void testAutoFillOnUpdate() {
        // 测试更新时的自动填充
        User testUser = createTestUser();
        userMapper.insert(testUser);
        
        Date originalUpdatedTime = testUser.getUpdatedTime();
        
        // 等待1毫秒确保时间差异
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 更新用户信息
        testUser.setNickname("更新后的昵称");
        userMapper.updateById(testUser);
        
        // 验证更新时间已自动更新
        User updatedUser = userMapper.selectById(testUser.getId());
        assertNotNull(updatedUser.getUpdatedTime(), "更新后的更新时间应该不为null");
        assertTrue(updatedUser.getUpdatedTime().getTime() > originalUpdatedTime.getTime(), 
                  "更新时间应该比原来的时间晚");
        
        log.info("更新时自动填充测试完成 - 原更新时间: {}, 新更新时间: {}", 
                originalUpdatedTime, updatedUser.getUpdatedTime());
    }

    /**
     * 创建测试用户
     */
    private User createTestUser() {
        User user = new User();
        user.setUuid("test-uuid-" + System.currentTimeMillis());
        user.setUsername("testuser_" + System.currentTimeMillis());
        user.setPassword("encoded_password");
        user.setPhone("13800138" + String.format("%03d", (int) (Math.random() * 1000)));
        user.setEmail("test" + System.currentTimeMillis() + "@example.com");
        user.setNickname("测试用户");
        user.setRole("user");
        user.setStatus(1);
        return user;
    }
} 