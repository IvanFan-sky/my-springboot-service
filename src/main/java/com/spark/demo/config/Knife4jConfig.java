package com.spark.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Knife4j 配置类
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("初始化Knife4j配置");
        
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot通用后台服务 API")
                        .version("1.0.0")
                        .description("这是一个使用Spring Boot构建的通用后台服务API文档。\n\n" +
                                "## 功能特性\n" +
                                "- 用户注册、登录、管理\n" +
                                "- 基于Session+Redis的认证机制\n" +
                                "- 手机验证码功能\n" +
                                "- 角色权限控制\n" +
                                "- 操作日志记录\n\n" +
                                "## 认证说明\n" +
                                "1. 首先调用登录接口（如 `/v1/users/login`）进行登录\n" +
                                "2. 登录成功后，点击页面右上角的 `Authorize` 按钮\n" +
                                "3. 在弹出的对话框中，`Value` 字段输入：`SESSION=登录返回的sessionId`\n" +
                                "4. 点击 `Authorize` 按钮完成认证\n" +
                                "5. 现在可以正常调用需要认证的接口了\n\n" +
                                "**注意**: 请确保在Value中输入完整的Cookie格式：`SESSION=your-session-id`")
                        .termsOfService("http://localhost:8080/api/terms")
                        .contact(new Contact()
                                .name("Spark")
                                .url("https://github.com/spark-demo")
                                .email("spark@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("https://api.example.com").description("生产环境")
                ))
                .components(new Components()
                        // 方案1：Cookie认证（推荐）
                        .addSecuritySchemes("CookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("SESSION")
                                .description("Spring Session Cookie认证。\n" +
                                        "使用方式：\n" +
                                        "1. 先调用登录接口获取sessionId\n" +
                                        "2. 在Authorize中输入: SESSION=your-session-id"))
                        
                        // 方案2：Header认证（备选）
                        .addSecuritySchemes("HeaderAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Cookie")
                                .description("Cookie Header认证。\n" +
                                        "使用方式：\n" +
                                        "1. 先调用登录接口获取sessionId\n" +
                                        "2. 在Authorize中输入: SESSION=your-session-id")))
                
                // 默认使用Cookie认证
                .addSecurityItem(new SecurityRequirement().addList("CookieAuth"))
                .addSecurityItem(new SecurityRequirement().addList("HeaderAuth"));
    }
} 