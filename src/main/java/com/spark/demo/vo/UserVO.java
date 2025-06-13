package com.spark.demo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

/**
 * 用户视图对象（用于查询返回）
 * @author spark
 * @date 2025-05-29
 */
@Data
@Schema(description = "用户视图对象（用于查询返回）")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户UUID (对外唯一标识)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private String uuid;

    @Schema(description = "用户名", example = "testuser")
    private String username;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "testuser@example.com")
    private String email;

    @Schema(description = "昵称", example = "Sparky")
    private String nickname;

    @Schema(description = "头像URL", example = "http://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别 (0-未知, 1-男, 2-女)", example = "1")
    private Integer gender;

    @Schema(description = "生日", example = "2000-01-01")
    private Date birthday;

    @Schema(description = "角色", example = "user")
    private String role;

    @Schema(description = "微信OpenID", example = "wxopenid12345")
    private String wechatId;

    @Schema(description = "支付宝UserID", example = "alipayuserid12345")
    private String alipayId;

    @Schema(description = "状态 (0-禁用, 1-正常)", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2023-01-01T12:00:00.000+00:00")
    private Date createdTime;

    @Schema(description = "更新时间", example = "2023-01-02T12:00:00.000+00:00")
    private Date updatedTime;
}