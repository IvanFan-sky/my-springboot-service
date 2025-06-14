package com.spark.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 角色实体类
 * @author spark
 * @date 2025-06-14
 */
@Data
@TableName("sys_role")
@Schema(description = "角色实体类")
public class Role implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色编码", example = "admin")
    private String roleCode;

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色描述", example = "系统管理员，拥有所有权限")
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

    // 关联字段（不映射到数据库）
    @TableField(exist = false)
    @Schema(description = "角色权限列表")
    private List<Permission> permissions;

    @TableField(exist = false)
    @Schema(description = "角色菜单列表")
    private List<Menu> menus;
} 