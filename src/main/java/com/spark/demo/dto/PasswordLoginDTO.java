package com.spark.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 密码登录请求对象
 *
 * @author spark
 * @date 2025-05-29
 */
@Data
@Schema(description = "密码登录请求对象")
public class PasswordLoginDTO {
    
    @Schema(description = "用户名或手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "testuser")
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
} 