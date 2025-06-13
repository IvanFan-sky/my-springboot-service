package com.spark.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Date;

/**
 * 用户数据传输对象（用于新增/修改）
 * @author spark
 * @date 2025-05-29
 */
@Data
@Schema(description = "用户数据传输对象（用于新增/修改）")
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2到20个字符之间")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "testuser")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "password123")
    private String password;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "testuser@example.com")
    private String email;

    @Size(max = 20, message = "昵称长度不能超过20个字符")
    @Schema(description = "昵称", example = "Sparky")
    private String nickname;

    @Schema(description = "头像URL", example = "http://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别 (0-未知, 1-男, 2-女)", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;

    @Schema(description = "生日", example = "2000-01-01")
    private Date birthday;

    @Schema(description = "角色", example = "user", allowableValues = {"admin", "user"})
    private String role;

    @Schema(description = "微信OpenID", example = "wxopenid12345")
    private String wechatId;

    @Schema(description = "支付宝UserID", example = "alipayuserid12345")
    private String alipayId;

    @Schema(description = "状态 (0-禁用, 1-正常)", example = "1", allowableValues = {"0", "1"})
    private Integer status;
}