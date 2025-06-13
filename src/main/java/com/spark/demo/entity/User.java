package com.spark.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 用户实体类
 * @author spark
 * @date 2025-05-29
 */
@Data
@TableName("sys_user")
@Schema(description = "用户实体类")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID (自增，内部使用)")
    private Long id;

    @Schema(description = "用户UUID (对外唯一标识)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private String uuid;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "testuser")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "encoded_password", hidden = true)
    private String password;

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

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updatedTime;

    @TableLogic(value = "NULL", delval = "NOW()")
    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "删除时间 (逻辑删除标记, null 未删除, 非null 已删除时间)")
    private Date deletedTime;
    
    /**
     * 生成UUID的前置处理
     */
    public void generateUuid() {
        if (this.uuid == null || this.uuid.trim().isEmpty()) {
            this.uuid = UUID.randomUUID().toString();
        }
    }
}