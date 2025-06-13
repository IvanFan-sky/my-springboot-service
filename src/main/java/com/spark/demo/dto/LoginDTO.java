package com.spark.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录请求对象
 *
 * @author spark
 * @date 2025-05-29
 */
@Data
@Schema(description = "登录请求对象")
public class LoginDTO {
    @Schema(description = "用户名（手机号）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String username;
    @Schema(description = "密码", example = "123456")
    private String password;
    @Schema(description = "短信验证码", example = "123456")
    private String code;
}