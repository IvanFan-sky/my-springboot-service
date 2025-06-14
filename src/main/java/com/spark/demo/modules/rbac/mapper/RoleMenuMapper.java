package com.spark.demo.modules.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spark.demo.entity.RoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色菜单关联Mapper接口
 * @author spark
 * @date 2025-06-14
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 根据角色ID删除角色菜单关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID删除角色菜单关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuId(@Param("menuId") Long menuId);

    /**
     * 批量插入角色菜单关联
     */
    int batchInsert(@Param("roleMenus") List<RoleMenu> roleMenus);

    /**
     * 根据角色ID查询菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID查询角色ID列表
     */
    @Select("SELECT role_id FROM sys_role_menu WHERE menu_id = #{menuId}")
    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);

    /**
     * 检查角色是否拥有指定菜单
     */
    @Select("SELECT COUNT(1) FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    int countByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 批量删除角色菜单关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id IN (${menuIds})")
    int batchDeleteByRoleIdAndMenuIds(@Param("roleId") Long roleId, @Param("menuIds") String menuIds);
} 