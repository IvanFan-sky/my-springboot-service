package com.spark.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Knife4j API文档配置类
 * 提供完整的API文档配置，包括认证、示例、最佳实践等
 * 
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Configuration
public class Knife4jConfig {

    @Value("${app.name:Spring Boot通用后台服务}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:基于Spring Boot 3.2.0构建的通用后台服务}")
    private String appDescription;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("初始化Knife4j API文档配置");
        
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .components(buildComponents())
                .security(buildSecurityRequirements())
                .externalDocs(buildExternalDocs());
    }

    /**
     * 构建API基本信息
     */
    private Info buildApiInfo() {
        return new Info()
                .title(appName + " API文档")
                .version(appVersion)
                .description(buildApiDescription())
                .termsOfService("http://localhost:8080" + contextPath + "/terms")
                .contact(new Contact()
                        .name("Spark开发团队")
                        .url("https://github.com/spark-demo")
                        .email("spark@example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    /**
     * 构建API描述信息
     */
    private String buildApiDescription() {
        return appDescription + "\n\n" +
                "## 🚀 功能特性\n" +
                "- ✅ **用户管理**: 注册、登录、信息管理\n" +
                "- ✅ **认证授权**: Session + Redis认证机制\n" +
                "- ✅ **短信验证**: 手机验证码功能\n" +
                "- ✅ **权限控制**: 基于角色的权限管理\n" +
                "- ✅ **安全防护**: 接口限流、数据脱敏\n" +
                "- ✅ **监控日志**: 操作日志、性能监控\n\n" +
                
                "## 🔐 认证说明\n" +
                "### 方式一：Cookie认证（推荐）\n" +
                "1. 调用登录接口（如 `/v1/auth/login`）获取SessionId\n" +
                "2. 点击页面右上角 `🔒 Authorize` 按钮\n" +
                "3. 在 **CookieAuth** 中输入：`SESSION=your-session-id`\n" +
                "4. 点击 `Authorize` 完成认证\n\n" +
                
                "### 方式二：Header认证\n" +
                "1. 调用登录接口获取SessionId\n" +
                "2. 在 **HeaderAuth** 中输入：`SESSION=your-session-id`\n" +
                "3. 系统会自动在请求头中添加Cookie\n\n" +
                
                "## 📋 测试账号\n" +
                "| 用户名 | 密码 | 角色 | 手机号 |\n" +
                "|--------|------|------|--------|\n" +
                "| admin | 123456 | admin | 13800138000 |\n" +
                "| testuser | 123456 | user | 13800138001 |\n\n" +
                
                "## 🔄 API版本说明\n" +
                "- **v1**: 当前稳定版本，推荐使用\n" +
                "- **v2**: 开发中版本，功能可能变更\n\n" +
                
                "## 📊 响应格式\n" +
                "所有接口统一返回格式：\n" +
                "```json\n" +
                "{\n" +
                "  \"code\": 200,\n" +
                "  \"msg\": \"操作成功\",\n" +
                "  \"data\": {},\n" +
                "  \"timestamp\": \"2025-01-27T10:30:00\"\n" +
                "}\n" +
                "```\n\n" +
                
                "## ⚠️ 注意事项\n" +
                "- 所有时间字段均为ISO 8601格式\n" +
                "- 分页查询默认页码从1开始\n" +
                "- 敏感信息已进行脱敏处理\n" +
                "- 接口调用频率受限流保护\n\n" +
                
                "## 🆘 常见问题\n" +
                "- **401错误**: 请先登录获取有效Session\n" +
                "- **403错误**: 权限不足，请检查用户角色\n" +
                "- **429错误**: 请求过于频繁，请稍后重试\n" +
                "- **500错误**: 服务器内部错误，请联系管理员";
    }

    /**
     * 构建服务器列表
     */
    private java.util.List<Server> buildServers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:8080" + contextPath)
                        .description("🔧 本地开发环境"),
                new Server()
                        .url("https://test-api.example.com" + contextPath)
                        .description("🧪 测试环境"),
                new Server()
                        .url("https://api.example.com" + contextPath)
                        .description("🚀 生产环境")
        );
    }

    /**
     * 构建组件配置
     */
    private Components buildComponents() {
        return new Components()
                // Cookie认证方案（推荐）
                .addSecuritySchemes("CookieAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("SESSION")
                        .description("🍪 **Cookie认证（推荐）**\n\n" +
                                "使用Spring Session Cookie进行认证：\n" +
                                "1. 调用登录接口获取SessionId\n" +
                                "2. 在此处输入：`SESSION=your-session-id`\n" +
                                "3. 系统会自动处理Cookie认证\n\n" +
                                "**示例**: `SESSION=12345678-1234-1234-1234-123456789012`"))
                
                // Header认证方案（备选）
                .addSecuritySchemes("HeaderAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("Cookie")
                        .description("📋 **Header认证（备选）**\n\n" +
                                "通过请求头传递Cookie信息：\n" +
                                "1. 调用登录接口获取SessionId\n" +
                                "2. 在此处输入：`SESSION=your-session-id`\n" +
                                "3. 系统会在请求头中添加Cookie\n\n" +
                                "**示例**: `SESSION=12345678-1234-1234-1234-123456789012`"))
                
                // JWT认证方案（未来扩展）
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("🔑 **JWT认证（未来版本）**\n\n" +
                                "Bearer Token认证方式：\n" +
                                "1. 调用登录接口获取JWT Token\n" +
                                "2. 在此处输入Token（无需Bearer前缀）\n" +
                                "3. 系统会自动添加Bearer前缀\n\n" +
                                "**注意**: 当前版本暂未启用"))
                
                // 通用响应模式
                .addSchemas("Result", new Schema<>()
                        .type("object")
                        .description("统一响应结果")
                        .addProperty("code", new Schema<>().type("integer").description("响应码").example(200))
                        .addProperty("msg", new Schema<>().type("string").description("响应消息").example("操作成功"))
                        .addProperty("data", new Schema<>().description("响应数据"))
                        .addProperty("timestamp", new Schema<>().type("string").description("响应时间").example("2025-01-27T10:30:00")))
                
                // 分页响应模式
                .addSchemas("PageResult", new Schema<>()
                        .type("object")
                        .description("分页响应结果")
                        .addProperty("records", new Schema<>().type("array").description("数据列表"))
                        .addProperty("total", new Schema<>().type("integer").description("总记录数").example(100))
                        .addProperty("size", new Schema<>().type("integer").description("每页大小").example(10))
                        .addProperty("current", new Schema<>().type("integer").description("当前页码").example(1))
                        .addProperty("pages", new Schema<>().type("integer").description("总页数").example(10)));
    }

    /**
     * 构建安全要求
     */
    private java.util.List<SecurityRequirement> buildSecurityRequirements() {
        return Arrays.asList(
                new SecurityRequirement().addList("CookieAuth"),
                new SecurityRequirement().addList("HeaderAuth")
        );
    }

    /**
     * 构建外部文档链接
     */
    private ExternalDocumentation buildExternalDocs() {
        return new ExternalDocumentation()
                .description("📚 项目文档和更多信息")
                .url("https://github.com/spark-demo/springboot-service/wiki");
    }
} 