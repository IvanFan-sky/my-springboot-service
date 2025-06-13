package com.spark.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Service层测试启动器
 * 用于验证所有Service测试是否可以正常运行
 * 
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class ServiceTestSuite {
    
    @Test
    void testServiceTestsAvailable() {
        // 这个测试用于验证测试类是否可以被正确加载
        log.info("Service测试套件可用，包含以下测试类:");
        log.info("- UserServiceTest: 用户服务单元测试");
        log.info("- SmsServiceTest: 短信服务单元测试");
        log.info("- UserServiceLogicDeleteTest: 用户逻辑删除测试");
        
        // 验证测试类可以被加载
        try {
            Class.forName("com.spark.demo.service.UserServiceTest");
            Class.forName("com.spark.demo.service.SmsServiceTest");
            Class.forName("com.spark.demo.service.UserServiceLogicDeleteTest");
            log.info("所有Service测试类加载成功");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法加载Service测试类", e);
        }
    }
} 