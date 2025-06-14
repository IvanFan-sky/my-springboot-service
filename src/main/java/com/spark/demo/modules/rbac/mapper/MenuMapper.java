package com.spark.demo.modules.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.demo.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper接口
 * @author spark
 * @date 2025-06-14
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据用户ID查询用户菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.deleted_time IS NULL " +
            "ORDER BY m.sort ASC")
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单列表
     */
    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = 1 AND m.deleted_time IS NULL " +
            "ORDER BY m.sort ASC")
    List<Menu> selectMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 分页查询菜单列表（带条件）
     */
    IPage<Menu> selectMenuPageWithCondition(Page<Menu> page, 
                                           @Param("menuCode") String menuCode,
                                           @Param("menuName") String menuName,
                                           @Param("type") Integer type,
                                           @Param("status") Integer status);

    /**
     * 查询菜单树结构
     */
    @Select("SELECT * FROM sys_menu WHERE status = 1 AND deleted_time IS NULL ORDER BY sort ASC")
    List<Menu> selectMenuTree();

    /**
     * 根据父菜单ID查询子菜单
     */
    @Select("SELECT * FROM sys_menu WHERE parent_id = #{parentId} AND status = 1 AND deleted_time IS NULL ORDER BY sort ASC")
    List<Menu> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 检查菜单编码是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_menu WHERE menu_code = #{menuCode} AND deleted_time IS NULL")
    int countByMenuCode(@Param("menuCode") String menuCode);

    /**
     * 检查菜单编码是否存在（排除指定ID）
     */
    @Select("SELECT COUNT(1) FROM sys_menu WHERE menu_code = #{menuCode} AND id != #{excludeId} AND deleted_time IS NULL")
    int countByMenuCodeExcludeId(@Param("menuCode") String menuCode, @Param("excludeId") Long excludeId);

    /**
     * 根据用户ID查询用户菜单树
     */
    List<Menu> selectUserMenuTree(@Param("userId") Long userId);
} 