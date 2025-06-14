package com.spark.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户角色关联实体类
 * @author spark
 * @date 2025-06-14
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联实体类")
public class UserRole implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色ID")
    private Long roleId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updatedTime;
} 