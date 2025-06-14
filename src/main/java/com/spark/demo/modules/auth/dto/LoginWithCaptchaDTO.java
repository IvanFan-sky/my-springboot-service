package com.spark.demo.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 带验证码的登录请求DTO
 */
@Data
@Schema(description = "带验证码的登录请求")
public class LoginWithCaptchaDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名或手机号", example = "testuser")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "验证码ID不能为空")
    @Schema(description = "验证码ID", example = "abc123-def456-ghi789")
    private String captchaId;

    @NotNull(message = "滑动位置不能为空")
    @Schema(description = "用户滑动的X坐标", example = "120")
    private Integer sliderX;
} 