package com.spark.demo.modules.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.demo.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 * @author spark
 * @date 2025-06-14
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询用户角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted_time IS NULL")
    List<Role> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 分页查询角色列表（带条件）
     */
    IPage<Role> selectRolePageWithCondition(Page<Role> page, @Param("roleCode") String roleCode, 
                                          @Param("roleName") String roleName, @Param("status") Integer status);

    /**
     * 查询角色及其权限信息
     */
    Role selectRoleWithPermissions(@Param("roleId") Long roleId);

    /**
     * 查询角色及其菜单信息
     */
    Role selectRoleWithMenus(@Param("roleId") Long roleId);

    /**
     * 检查角色编码是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_role WHERE role_code = #{roleCode} AND deleted_time IS NULL")
    int countByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 检查角色编码是否存在（排除指定ID）
     */
    @Select("SELECT COUNT(1) FROM sys_role WHERE role_code = #{roleCode} AND id != #{excludeId} AND deleted_time IS NULL")
    int countByRoleCodeExcludeId(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);
} 