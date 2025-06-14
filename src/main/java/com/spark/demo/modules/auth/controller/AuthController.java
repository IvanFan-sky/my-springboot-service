package com.spark.demo.modules.auth.controller;

import com.spark.demo.common.annotation.RequireAuth;
import com.spark.demo.common.context.UserContext;
import com.spark.demo.common.result.Result;
import com.spark.demo.common.util.ApiDocUtil;
import com.spark.demo.dto.LoginDTO;
import com.spark.demo.dto.PasswordLoginDTO;
import com.spark.demo.dto.SmsLoginDTO;
import com.spark.demo.dto.UserDTO;
import com.spark.demo.entity.User;
import com.spark.demo.modules.auth.service.AuthService;
import com.spark.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户注册、登录、登出等认证相关功能
 * 
 * @author spark
 * @date 2025-06-14
 */
@Slf4j
@Tag(name = "🔐 用户认证", description = "用户注册、登录、登出等认证相关接口")
@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;

    @Operation(
        summary = "用户注册",
        description = """
            **功能说明**：新用户注册接口
            
            **业务规则**：
            - 用户名必须唯一，长度3-20位
            - 密码长度6-20位，建议包含字母和数字
            - 手机号必须是有效的中国大陆手机号
            - 邮箱格式必须正确且唯一
            
            **注意事项**：
            - 注册成功后需要调用登录接口获取Session
            - 系统会自动为新用户分配默认角色
            - 敏感信息会进行加密存储
            """,
        tags = {"用户认证"}
    )
    @ApiDocUtil.CompleteApiResponses
    @PostMapping("/register")
    public Result<Void> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "用户注册信息",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(
                        name = "注册示例",
                        value = ApiDocUtil.Examples.REGISTER_REQUEST
                    )
                )
            )
            @RequestBody @Validated UserDTO userDTO) {
        userService.register(userDTO);
        return Result.success();
    }

    @Operation(
        summary = "用户登录",
        description = """
            **功能说明**：用户名密码登录接口
            
            **业务规则**：
            - 支持用户名或手机号登录
            - 密码错误超过5次将锁定账户30分钟
            - 登录成功返回SessionId，有效期24小时
            
            **使用方式**：
            1. 调用此接口获取SessionId
            2. 在后续请求中携带Cookie: SESSION=返回的SessionId
            3. 或在Swagger中点击🔒Authorize按钮进行认证
            
            **安全特性**：
            - 密码传输加密
            - 登录频率限制
            - 异常登录检测
            """,
        tags = {"用户认证"}
    )
    @ApiDocUtil.AuthApiResponses
    @PostMapping("/login")
    public Result<String> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "登录信息",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginDTO.class),
                    examples = @ExampleObject(
                        name = "登录示例",
                        value = ApiDocUtil.Examples.LOGIN_REQUEST
                    )
                )
            )
            @RequestBody @Validated LoginDTO loginDTO) {
        String sessionId = userService.login(loginDTO);
        return Result.success(sessionId);
    }

    @Operation(
        summary = "密码登录（增强版）",
        description = """
            **功能说明**：增强版密码登录接口，支持更多登录方式
            
            **支持的登录方式**：
            - 用户名 + 密码
            - 手机号 + 密码
            - 邮箱 + 密码
            
            **与普通登录的区别**：
            - 支持更多的登录标识符
            - 更严格的安全验证
            - 更详细的登录日志
            
            **推荐使用场景**：
            - 移动端应用
            - 需要多种登录方式的场景
            - 对安全性要求较高的场景
            """,
        tags = {"用户认证"}
    )
    @ApiDocUtil.AuthApiResponses
    @PostMapping("/password-login")
    public Result<String> passwordLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "密码登录信息",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PasswordLoginDTO.class),
                    examples = @ExampleObject(
                        name = "密码登录示例",
                        value = """
                            {
                              "identifier": "testuser",
                              "password": "123456",
                              "loginType": "username"
                            }
                            """
                    )
                )
            )
            @RequestBody @Validated PasswordLoginDTO passwordLoginDTO) {
        String sessionId = userService.passwordLogin(passwordLoginDTO);
        return Result.success(sessionId);
    }

    @Operation(
        summary = "短信验证码登录",
        description = """
            **功能说明**：使用手机号和短信验证码进行登录
            
            **使用流程**：
            1. 先调用 `/v1/sms/send` 发送验证码
            2. 用户收到验证码后调用此接口登录
            3. 验证码有效期5分钟，每个手机号每天限制10次
            
            **业务规则**：
            - 验证码必须是6位数字
            - 验证码5分钟内有效
            - 验证码使用后立即失效
            - 如果用户不存在，会自动创建新用户
            
            **安全特性**：
            - 验证码加密存储
            - 防刷机制
            - 异常检测
            """,
        tags = {"用户认证"}
    )
    @ApiDocUtil.AuthApiResponses
    @PostMapping("/sms-login")
    public Result<String> smsLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "短信登录信息",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SmsLoginDTO.class),
                    examples = @ExampleObject(
                        name = "短信登录示例",
                        value = ApiDocUtil.Examples.SMS_LOGIN_REQUEST
                    )
                )
            )
            @RequestBody @Validated SmsLoginDTO smsLoginDTO) {
        String sessionId = userService.smsLogin(smsLoginDTO);
        return Result.success(sessionId);
    }

    @RequireAuth
    @Operation(
        summary = "用户登出",
        description = """
            **功能说明**：用户登出接口，清除登录状态
            
            **业务规则**：
            - 清除服务器端Session信息
            - 清除Redis中的用户缓存
            - 记录登出日志
            
            **注意事项**：
            - 登出后需要重新登录才能访问需要认证的接口
            - 建议客户端同时清除本地存储的Session信息
            - 登出操作不可撤销
            
            **安全特性**：
            - 强制清除所有相关缓存
            - 防止Session劫持
            """,
        tags = {"用户认证"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "登出成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "登出成功示例",
                value = """
                    {
                      "code": 200,
                      "msg": "登出成功",
                      "data": null,
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 记录登出日志
            User currentUser = UserContext.getCurrentUser();
            if (currentUser != null) {
                log.info("用户登出 - 用户ID: {}, 用户名: {}, SessionId: {}", 
                    currentUser.getId(), currentUser.getUsername(), session.getId());
            }
            
            // 清除Session
            session.invalidate();
        }
        return Result.success();
    }

    @RequireAuth
    @Operation(
        summary = "检查登录状态",
        description = """
            **功能说明**：检查当前用户的登录状态
            
            **返回信息**：
            - 用户基本信息
            - Session有效期
            - 登录时间
            - 权限信息
            
            **使用场景**：
            - 前端页面初始化时检查登录状态
            - 定期检查Session是否过期
            - 获取当前用户权限信息
            """,
        tags = {"用户认证"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "获取登录状态成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "登录状态示例",
                value = """
                    {
                      "code": 200,
                      "msg": "操作成功",
                      "data": {
                        "userId": 1,
                        "username": "testuser",
                        "role": "user",
                        "loginTime": "2025-01-27T09:30:00",
                        "sessionExpireTime": "2025-01-28T09:30:00",
                        "permissions": ["user:read", "user:update"]
                      },
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @GetMapping("/status")
    public Result<Object> getLoginStatus(HttpServletRequest request) {
        User currentUser = UserContext.getCurrentUser();
        HttpSession session = request.getSession(false);
        
        if (currentUser != null && session != null) {
            java.util.Map<String, Object> statusData = java.util.Map.of(
                "userId", currentUser.getId(),
                "username", currentUser.getUsername(),
                "role", currentUser.getRole(),
                "loginTime", session.getCreationTime(),
                "sessionExpireTime", session.getLastAccessedTime() + session.getMaxInactiveInterval() * 1000L,
                "permissions", java.util.List.of("user:read", "user:update")
            );
            return Result.success(statusData);
        }
        
        return Result.fail(401, "用户未登录");
    }

    // ==================== JWT认证接口 ====================

    @Operation(
        summary = "JWT密码登录",
        description = """
            **功能说明**：使用Spring Security的JWT密码登录接口
            
            **与传统登录的区别**：
            - 返回JWT token而不是Session
            - 支持无状态认证
            - 更适合前后端分离和移动端
            
            **返回内容**：
            - accessToken: 访问令牌（24小时有效）
            - refreshToken: 刷新令牌（7天有效）
            - tokenType: Bearer
            - expiresIn: 过期时间（秒）
            - user: 用户基本信息
            
            **使用方式**：
            在请求头中携带：Authorization: Bearer {accessToken}
            """,
        tags = {"JWT认证"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "JWT登录成功",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "JWT登录成功示例",
                value = """
                    {
                      "code": 200,
                      "msg": "登录成功",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "tokenType": "Bearer",
                        "expiresIn": 86400,
                        "user": {
                          "id": 1,
                          "uuid": "550e8400-e29b-41d4-a716-446655440000",
                          "username": "testuser",
                          "phone": "138****5678",
                          "email": "te***@example.com",
                          "role": "user",
                          "status": 1
                        }
                      }
                    }
                    """
            )
        )
    )
    @PostMapping("/jwt/password-login")
    public Result<Map<String, Object>> jwtPasswordLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "JWT密码登录信息",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PasswordLoginDTO.class),
                    examples = @ExampleObject(
                        name = "JWT密码登录示例",
                        value = """
                            {
                              "username": "testuser",
                              "password": "123456"
                            }
                            """
                    )
                )
            )
            @RequestBody @Validated PasswordLoginDTO passwordLoginDTO) {
        Map<String, Object> tokenData = authService.passwordLogin(passwordLoginDTO);
        return Result.success(tokenData);
    }

    @Operation(
        summary = "JWT短信登录",
        description = """
            **功能说明**：使用短信验证码进行JWT登录
            
            **流程说明**：
            1. 先调用短信发送接口获取验证码
            2. 使用手机号和验证码进行登录
            3. 返回JWT token
            
            **返回内容**：与JWT密码登录相同
            """,
        tags = {"JWT认证"}
    )
    @PostMapping("/jwt/sms-login")
    public Result<Map<String, Object>> jwtSmsLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "JWT短信登录信息",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SmsLoginDTO.class),
                    examples = @ExampleObject(
                        name = "JWT短信登录示例",
                        value = """
                            {
                              "phone": "13812345678",
                              "code": "123456"
                            }
                            """
                    )
                )
            )
            @RequestBody @Validated SmsLoginDTO smsLoginDTO) {
        Map<String, Object> tokenData = authService.smsLogin(smsLoginDTO);
        return Result.success(tokenData);
    }

    @Operation(
        summary = "刷新JWT Token",
        description = """
            **功能说明**：使用刷新令牌获取新的访问令牌
            
            **使用场景**：
            - 访问令牌即将过期时
            - 访问令牌已过期但刷新令牌仍有效时
            
            **注意事项**：
            - 刷新令牌有效期为7天
            - 每次刷新都会返回新的访问令牌和刷新令牌
            - 旧的刷新令牌将失效
            """,
        tags = {"JWT认证"}
    )
    @PostMapping("/jwt/refresh")
    public Result<Map<String, Object>> refreshToken(
            @Parameter(description = "刷新令牌", required = true)
            @RequestParam String refreshToken) {
        Map<String, Object> tokenData = authService.refreshToken(refreshToken);
        return Result.success(tokenData);
    }
} 