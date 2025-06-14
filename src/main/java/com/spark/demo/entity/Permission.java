package com.spark.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 权限实体类
 * @author spark
 * @date 2025-06-14
 */
@Data
@TableName("sys_permission")
@Schema(description = "权限实体类")
public class Permission implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限编码", example = "user:read")
    private String permissionCode;

    @Schema(description = "权限名称", example = "查看用户")
    private String permissionName;

    @Schema(description = "权限类型 (1-菜单, 2-按钮, 3-接口)", example = "3")
    private Integer type;

    @Schema(description = "父权限ID", example = "1")
    private Long parentId;

    @Schema(description = "权限路径", example = "/api/v1/users")
    private String path;

    @Schema(description = "HTTP方法", example = "GET")
    private String method;

    @Schema(description = "权限描述", example = "查看用户信息的权限")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

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
    @Schema(description = "删除时间")
    private Date deletedTime;
} 