package com.spark.demo.controller;

import com.spark.demo.common.result.Result;
import com.spark.demo.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 短信验证码控制器
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Tag(name = "短信验证码", description = "短信验证码发送和验证接口")
@RestController
@RequestMapping("/v1/sms")
@Validated
public class SmsController {
    
    @Autowired
    private SmsService smsService;
    
    @Operation(summary = "发送验证码", description = "向指定手机号发送验证码")
    @PostMapping("/send")
    public Result<Void> sendVerifyCode(
            @Parameter(description = "手机号", required = true, example = "13800138000")
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
    
    @Operation(summary = "验证验证码", description = "验证手机验证码是否正确")
    @PostMapping("/verify")
    public Result<Void> verifyCode(
            @Parameter(description = "手机号", required = true, example = "13800138000")
            @RequestParam 
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
            String phone,
            
            @Parameter(description = "验证码", required = true, example = "123456")
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
} 