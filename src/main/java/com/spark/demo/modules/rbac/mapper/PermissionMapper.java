package com.spark.demo.modules.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.demo.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 * @author spark
 * @date 2025-06-14
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询用户权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 AND p.deleted_time IS NULL")
    List<Permission> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1 AND p.deleted_time IS NULL")
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 分页查询权限列表（带条件）
     */
    IPage<Permission> selectPermissionPageWithCondition(Page<Permission> page, 
                                                      @Param("permissionCode") String permissionCode,
                                                      @Param("permissionName") String permissionName,
                                                      @Param("type") Integer type,
                                                      @Param("status") Integer status);

    /**
     * 查询权限树结构
     */
    List<Permission> selectPermissionTree();

    /**
     * 根据权限编码和路径查询权限
     */
    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} " +
            "OR (path = #{path} AND method = #{method}) AND status = 1 AND deleted_time IS NULL")
    List<Permission> selectByCodeOrPath(@Param("permissionCode") String permissionCode, 
                                      @Param("path") String path, @Param("method") String method);

    /**
     * 检查权限编码是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_permission WHERE permission_code = #{permissionCode} AND deleted_time IS NULL")
    int countByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 检查权限编码是否存在（排除指定ID）
     */
    @Select("SELECT COUNT(1) FROM sys_permission WHERE permission_code = #{permissionCode} AND id != #{excludeId} AND deleted_time IS NULL")
    int countByPermissionCodeExcludeId(@Param("permissionCode") String permissionCode, @Param("excludeId") Long excludeId);

    /**
     * 根据父权限ID查询子权限
     */
    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND status = 1 AND deleted_time IS NULL ORDER BY sort ASC")
    List<Permission> selectByParentId(@Param("parentId") Long parentId);
} 