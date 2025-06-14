package com.spark.demo.modules.auth.controller;

import com.spark.demo.common.annotation.RequireAuth;
import com.spark.demo.common.context.UserContext;
import com.spark.demo.common.result.Result;
import com.spark.demo.dto.LoginDTO;
import com.spark.demo.dto.PasswordLoginDTO;
import com.spark.demo.dto.SmsLoginDTO;
import com.spark.demo.dto.UserDTO;
import com.spark.demo.entity.User;
import com.spark.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * @author spark
 * @date 2025-06-14
 */
@Slf4j
@Tag(name = "用户认证", description = "用户注册、登录、登出等认证相关接口")
@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册", description = "提供用户信息进行注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Validated UserDTO userDTO) {
        userService.register(userDTO);
        return Result.success();
    }

    @Operation(summary = "用户登录", description = "提供用户名和密码进行登录，成功返回SessionId")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginDTO loginDTO) {
        String sessionId = userService.login(loginDTO);
        return Result.success(sessionId);
    }

    @Operation(summary = "密码登录", description = "使用用户名/手机号和密码进行登录，成功返回SessionId")
    @PostMapping("/password-login")
    public Result<String> passwordLogin(@RequestBody @Validated PasswordLoginDTO passwordLoginDTO) {
        String sessionId = userService.passwordLogin(passwordLoginDTO);
        return Result.success(sessionId);
    }

    @Operation(summary = "短信验证码登录", description = "使用手机号和短信验证码进行登录，成功返回SessionId")
    @PostMapping("/sms-login")
    public Result<String> smsLogin(@RequestBody @Validated SmsLoginDTO smsLoginDTO) {
        String sessionId = userService.smsLogin(smsLoginDTO);
        return Result.success(sessionId);
    }

    @Operation(summary = "用户登出", description = "用户登出，清除Session")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Long currentUserId = UserContext.getCurrentUserId();
                String sessionId = session.getId();
                
                log.info("用户登出，用户ID: {}, SessionId: {}", currentUserId, sessionId);
                
                // 清除Session
                session.invalidate();
                
                // 清除用户上下文
                UserContext.clear();
                
                log.info("用户Session已清除，SessionId: {}", sessionId);
            } else {
                log.info("用户登出，但Session已不存在");
            }
            
            return Result.success();
        } catch (Exception e) {
            log.error("用户登出时发生错误", e);
            return Result.success(); // 即使出错也返回成功，确保前端能正常处理
        }
    }
    
    @RequireAuth
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @Parameter(description = "旧密码", required = true)
            @RequestParam @NotBlank(message = "旧密码不能为空") String oldPassword,
            
            @Parameter(description = "新密码", required = true)
            @RequestParam @NotBlank(message = "新密码不能为空") String newPassword) {
        
        Long currentUserId = UserContext.getCurrentUserId();
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            return Result.fail(401, "用户未登录");
        }
        
        userService.changePassword(currentUser.getUuid(), oldPassword, newPassword);
        return Result.success();
    }
} 