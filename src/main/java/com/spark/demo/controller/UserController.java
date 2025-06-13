package com.spark.demo.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.demo.annotation.RequireAuth;
import com.spark.demo.annotation.RequireRole;
import com.spark.demo.common.context.UserContext;
import com.spark.demo.common.result.Result;
import com.spark.demo.dto.LoginDTO;
import com.spark.demo.dto.PasswordLoginDTO;
import com.spark.demo.dto.SmsLoginDTO;
import com.spark.demo.dto.UserDTO;
import com.spark.demo.entity.User;
import com.spark.demo.service.UserService;
import com.spark.demo.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Tag(name = "用户管理", description = "用户注册、登录、信息获取及管理接口")
@RestController
@RequestMapping("/v1/users")
@Validated
public class UserController {

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

    @RequireAuth
    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        UserVO userVO = userService.getCurrentUserInfo(userId);
        return Result.success(userVO);
    }

    @RequireAuth
    @RequireRole("admin")
    @Operation(summary = "新增用户", description = "管理员权限：新增用户")
    @PostMapping
    public Result<UserVO> addUser(@RequestBody @Validated UserDTO userDTO) {
        UserVO newUser = userService.addUser(userDTO);
        return Result.success(newUser);
    }

    @RequireAuth
    @RequireRole("admin")
    @Operation(summary = "删除用户", description = "管理员权限：根据UUID逻辑删除用户")
    @DeleteMapping("/{uuid}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户UUID", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable String uuid) {
        userService.deleteUserByUuid(uuid);
        return Result.success();
    }

    @RequireAuth
    @Operation(summary = "更新用户信息", description = "管理员可以修改任何用户，普通用户只能修改自己")
    @PutMapping("/{uuid}")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户UUID", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable String uuid, 
            @RequestBody @Validated UserDTO userDTO) {
        
        // 检查权限：管理员可以修改任何用户，普通用户只能修改自己
        String currentUserRole = UserContext.getCurrentUserRole();
        
        if (!"admin".equals(currentUserRole)) {
            // 普通用户只能修改自己的信息，需要验证UUID
            Long currentUserId = UserContext.getCurrentUserId();
            User currentUser = userService.getById(currentUserId);
            if (currentUser == null || !uuid.equals(currentUser.getUuid())) {
                log.warn("普通用户尝试修改他人信息 - 用户ID: {}, 目标UUID: {}", currentUserId, uuid);
                return Result.fail(403, "权限不足，只能修改自己的信息");
            }
            
            // 普通用户不能修改角色和状态字段
            if (userDTO.getRole() != null && !userDTO.getRole().equals(currentUser.getRole())) {
                return Result.fail(403, "普通用户不能修改角色");
            }
            if (userDTO.getStatus() != null && !userDTO.getStatus().equals(currentUser.getStatus())) {
                return Result.fail(403, "普通用户不能修改状态");
            }
        }
        
        UserVO updatedUser = userService.updateUserByUuid(uuid, userDTO);
        log.info("用户信息更新成功 - 操作者: {}, 目标用户: {}", 
            UserContext.getCurrentUser().getUsername(), uuid);
        return Result.success(updatedUser);
    }

    @RequireAuth
    @Operation(summary = "获取指定用户信息", description = "管理员可以查看任何用户，普通用户只能查看自己")
    @GetMapping("/{uuid}")
    public Result<UserVO> getUserByUuid(
            @Parameter(description = "用户UUID", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable String uuid) {
        
        String currentUserRole = UserContext.getCurrentUserRole();
        
        // 普通用户只能查看自己的信息
        if (!"admin".equals(currentUserRole)) {
            Long currentUserId = UserContext.getCurrentUserId();
            User currentUser = userService.getById(currentUserId);
            if (currentUser == null || !uuid.equals(currentUser.getUuid())) {
                log.warn("普通用户尝试查看他人信息 - 用户ID: {}, 目标UUID: {}", currentUserId, uuid);
                return Result.fail(403, "权限不足，只能查看自己的信息");
            }
        }
        
        UserVO userVO = userService.getUserByUuid(uuid);
        return Result.success(userVO);
    }

    @RequireAuth
    @RequireRole("admin")
    @Operation(summary = "分页查询用户列表", description = "管理员权限：分页获取用户列表，可带查询条件")
    @Parameters({
            @Parameter(name = "current", description = "当前页码", example = "1"),
            @Parameter(name = "size", description = "每页显示条数", example = "10"),
            @Parameter(name = "username", description = "用户名 (模糊查询)"),
            @Parameter(name = "phone", description = "手机号 (模糊查询)"),
            @Parameter(name = "email", description = "邮箱 (模糊查询)"),
            @Parameter(name = "status", description = "状态 (0-禁用, 1-正常)"),
            @Parameter(name = "role", description = "角色")
    })
    @GetMapping("/list")
    public Result<Page<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            UserDTO userFilter
    ) {
        Page<User> pageRequest = new Page<>(current, size);
        Page<UserVO> userVOPage = userService.listUsers(pageRequest, userFilter);
        return Result.success(userVOPage);
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
    @RequireRole("admin")
    @Operation(summary = "修改用户状态", description = "管理员权限：快速修改用户状态（启用/禁用）")
    @PatchMapping("/{uuid}/status")
    public Result<UserVO> updateUserStatus(
            @Parameter(description = "用户UUID", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable String uuid,
            @Parameter(description = "新状态 (0-禁用, 1-正常)", required = true, example = "1")
            @RequestParam Integer status) {
        
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail(400, "状态值无效，只能是0（禁用）或1（正常）");
        }
        
        try {
            UserVO updatedUser = userService.updateUserStatus(uuid, status);
            log.info("用户状态修改成功 - 操作者: {}, 目标用户UUID: {}, 新状态: {}", 
                UserContext.getCurrentUser().getUsername(), uuid, status);
            return Result.success(updatedUser);
        } catch (Exception e) {
            log.error("修改用户状态失败 - UUID: {}, 状态: {}", uuid, status, e);
            return Result.fail(500, "修改用户状态失败：" + e.getMessage());
        }
    }
    
}