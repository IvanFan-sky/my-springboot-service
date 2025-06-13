package com.spark.demo.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.demo.dto.PasswordLoginDTO;
import com.spark.demo.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 登录认证集成测试
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private MockMvc getMockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
        return mockMvc;
    }

    @Test
    public void testPasswordLoginAndListUsers() throws Exception {
        // 1. 测试密码登录
        log.info("=== 开始测试密码登录 ===");
        PasswordLoginDTO loginDTO = new PasswordLoginDTO();
        loginDTO.setUsername("admin");  // 假设存在admin用户
        loginDTO.setPassword("123456");

        MvcResult loginResult = getMockMvc().perform(post("/api/v1/users/password-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        log.info("登录响应: {}", loginResponse);
        
        // 从响应中提取sessionId
        Result loginResultObj = objectMapper.readValue(loginResponse, Result.class);
        String sessionId = (String) loginResultObj.getData();
        log.info("获取到SessionId: {}", sessionId);

        // 2. 测试使用Session访问分页查询接口
        log.info("=== 开始测试分页查询 ===");
        MvcResult listResult = getMockMvc().perform(get("/api/v1/users")
                .param("current", "1")
                .param("size", "10")
                .cookie(new jakarta.servlet.http.Cookie("SESSION", sessionId)))
                .andExpect(status().isOk())
                .andReturn();

        String listResponse = listResult.getResponse().getContentAsString();
        log.info("分页查询响应: {}", listResponse);
    }

    @Test
    public void testWithoutLogin() throws Exception {
        // 测试未登录访问分页查询
        log.info("=== 测试未登录访问 ===");
        getMockMvc().perform(get("/api/v1/users")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isUnauthorized())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    log.info("未登录访问响应: {}", response);
                });
    }

    @Test 
    public void testAuthFilterExcludePaths() throws Exception {
        // 测试排除路径
        log.info("=== 测试排除路径 ===");
        
        // 测试登录接口不需要认证
        getMockMvc().perform(post("/api/v1/users/password-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andDo(result -> {
                    log.info("密码登录接口状态: {}", result.getResponse().getStatus());
                });

        // 测试注册接口不需要认证  
        getMockMvc().perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andDo(result -> {
                    log.info("注册接口状态: {}", result.getResponse().getStatus());
                });
    }
} 