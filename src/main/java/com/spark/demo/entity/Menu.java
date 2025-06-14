package com.spark.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 菜单实体类
 * @author spark
 * @date 2025-06-14
 */
@Data
@TableName("sys_menu")
@Schema(description = "菜单实体类")
public class Menu implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "菜单编码", example = "user_management")
    private String menuCode;

    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    @Schema(description = "父菜单ID", example = "1")
    private Long parentId;

    @Schema(description = "菜单路径", example = "/user")
    private String path;

    @Schema(description = "组件路径", example = "user/UserList")
    private String component;

    @Schema(description = "菜单图标", example = "user")
    private String icon;

    @Schema(description = "菜单类型 (1-目录, 2-菜单, 3-按钮)", example = "2")
    private Integer type;

    @Schema(description = "是否隐藏 (0-显示, 1-隐藏)", example = "0")
    private Integer hidden;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态 (0-禁用, 1-正常)", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "用户管理菜单")
    private String remark;

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
    @Schema(description = "子菜单列表")
    private List<Menu> children;
} 