package com.spark.demo;

import com.spark.demo.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * 测试基类
 * 提供通用的测试配置和环境设置
 * 
 * @author spark
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DemoApplication.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public abstract class BaseTest {

    /**
     * 测试前置处理
     */
    @BeforeEach
    public void setUp() {
        // 可以在这里添加通用的测试前置处理逻辑
        // 如清理缓存、重置数据等
    }

    /**
     * 创建测试用户数据
     */
    protected void createTestUser() {
        // 创建测试用户的通用方法
    }

    /**
     * 清理测试数据
     */
    protected void cleanTestData() {
        // 清理测试数据的通用方法
    }
} 