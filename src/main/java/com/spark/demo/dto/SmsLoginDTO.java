package com.spark.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 短信验证码登录请求对象
 *
 * @author spark
 * @date 2025-05-29
 */
@Data
@Schema(description = "短信验证码登录请求对象")
public class SmsLoginDTO {
    
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Schema(description = "短信验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String code;
} 