package com.spark.demo.modules.sms.controller;

import com.spark.demo.common.result.Result;
import com.spark.demo.common.util.ApiDocUtil;
import com.spark.demo.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 短信验证码控制器
 * 提供短信验证码发送和验证功能
 * 
 * @author spark
 * @date 2025-06-14
 */
@Slf4j
@Tag(name = "📱 短信验证码", description = "短信验证码发送和验证接口")
@RestController
@RequestMapping("/v1/sms")
@Validated
public class SmsController {
    
    @Autowired
    private SmsService smsService;
    
    @Operation(
        summary = "发送验证码",
        description = """
            **功能说明**：向指定手机号发送6位数字验证码
            
            **业务规则**：
            - 验证码有效期5分钟
            - 同一手机号60秒内只能发送一次
            - 同一手机号每天最多发送10次
            - 同一IP每小时最多发送20次
            
            **验证码规则**：
            - 6位随机数字
            - 不包含连续数字（如123456）
            - 不包含重复数字（如111111）
            
            **安全特性**：
            - 发送频率限制
            - IP限制防刷
            - 异常检测和拦截
            - 验证码加密存储
            
            **使用场景**：
            - 用户注册验证
            - 短信登录验证
            - 找回密码验证
            - 重要操作二次验证
            """,
        tags = {"短信验证码"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "验证码发送成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "发送成功示例",
                value = """
                    {
                      "code": 200,
                      "msg": "验证码发送成功",
                      "data": null,
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "请求参数错误",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "参数错误示例",
                value = """
                    {
                      "code": 400,
                      "msg": "手机号码格式不正确",
                      "data": null,
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @ApiResponse(
        responseCode = "429",
        description = "发送过于频繁",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "频率限制示例",
                value = """
                    {
                      "code": 429,
                      "msg": "发送过于频繁，请60秒后再试",
                      "data": null,
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @PostMapping("/send")
    public Result<Void> sendVerifyCode(
            @Parameter(
                description = "手机号码",
                required = true,
                example = ApiDocUtil.Examples.PHONE_NUMBER,
                schema = @Schema(
                    type = "string",
                    pattern = "^1[3-9]\\d{9}$",
                    minLength = 11,
                    maxLength = 11
                )
            )
            @RequestParam 
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
            String phone) {
        
        boolean success = smsService.sendVerifyCode(phone);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("发送验证码失败，请稍后重试");
        }
    }
    
    @Operation(
        summary = "验证验证码",
        description = """
            **功能说明**：验证手机验证码是否正确
            
            **验证规则**：
            - 验证码必须是6位数字
            - 验证码5分钟内有效
            - 验证码只能使用一次
            - 验证失败5次后锁定手机号30分钟
            
            **业务场景**：
            - 注册时验证手机号
            - 登录前验证身份
            - 重要操作前二次验证
            - 找回密码时验证
            
            **安全特性**：
            - 验证次数限制
            - 时间窗口限制
            - 防暴力破解
            - 异常行为检测
            
            **注意事项**：
            - 验证成功后验证码立即失效
            - 验证失败不会立即失效，直到过期
            - 建议验证成功后立即进行后续操作
            """,
        tags = {"短信验证码"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "验证码验证成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "验证成功示例",
                value = """
                    {
                      "code": 200,
                      "msg": "验证码验证成功",
                      "data": null,
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "验证码错误或已过期",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "验证失败示例",
                value = """
                    {
                      "code": 400,
                      "msg": "验证码错误或已过期",
                      "data": null,
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @ApiResponse(
        responseCode = "429",
        description = "验证次数过多",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "验证次数限制示例",
                value = """
                    {
                      "code": 429,
                      "msg": "验证失败次数过多，请30分钟后再试",
                      "data": null,
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @PostMapping("/verify")
    public Result<Void> verifyCode(
            @Parameter(
                description = "手机号码",
                required = true,
                example = ApiDocUtil.Examples.PHONE_NUMBER,
                schema = @Schema(
                    type = "string",
                    pattern = "^1[3-9]\\d{9}$",
                    minLength = 11,
                    maxLength = 11
                )
            )
            @RequestParam 
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
            String phone,
            
            @Parameter(
                description = "6位数字验证码",
                required = true,
                example = ApiDocUtil.Examples.SMS_CODE,
                schema = @Schema(
                    type = "string",
                    pattern = "^\\d{6}$",
                    minLength = 6,
                    maxLength = 6
                )
            )
            @RequestParam 
            @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
            String code) {
        
        boolean valid = smsService.verifyCode(phone, code);
        if (valid) {
            return Result.success();
        } else {
            return Result.fail("验证码错误或已过期");
        }
    }

    @Operation(
        summary = "获取验证码发送状态",
        description = """
            **功能说明**：查询指定手机号的验证码发送状态
            
            **返回信息**：
            - 是否可以发送验证码
            - 距离下次可发送的剩余时间
            - 今日已发送次数
            - 剩余发送次数
            
            **使用场景**：
            - 前端显示发送按钮状态
            - 显示倒计时
            - 防止用户频繁点击
            
            **注意事项**：
            - 此接口不需要认证
            - 不会泄露敏感信息
            - 仅返回发送状态信息
            """,
        tags = {"短信验证码"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "获取发送状态成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "发送状态示例",
                value = """
                    {
                      "code": 200,
                      "msg": "操作成功",
                      "data": {
                        "canSend": false,
                        "remainingTime": 45,
                        "todaySentCount": 3,
                        "remainingCount": 7,
                        "nextSendTime": "2025-01-27T10:31:00"
                      },
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    @GetMapping("/status")
    public Result<Object> getSendStatus(
            @Parameter(
                description = "手机号码",
                required = true,
                example = ApiDocUtil.Examples.PHONE_NUMBER
            )
            @RequestParam 
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
            String phone) {
        
        // 这里应该调用service方法获取实际状态，这里提供示例数据
        java.util.Map<String, Object> statusData = java.util.Map.of(
            "canSend", true,
            "remainingTime", 0,
            "todaySentCount", 2,
            "remainingCount", 8,
            "nextSendTime", java.time.LocalDateTime.now().toString()
        );
        
        return Result.success(statusData);
    }
} 