package com.spark.demo.modules.rbac.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spark.demo.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 * @author spark
 * @date 2025-06-14
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 根据用户ID查询用户权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);

    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);

    /**
     * 分页查询权限列表
     */
    IPage<Permission> getPermissionPageWithCondition(Page<Permission> page, String permissionCode, 
                                                   String permissionName, Integer type, Integer status);

    /**
     * 查询权限树结构
     */
    List<Permission> getPermissionTree();

    /**
     * 根据权限编码和路径查询权限
     */
    List<Permission> getPermissionsByCodeOrPath(String permissionCode, String path, String method);

    /**
     * 创建权限
     */
    boolean createPermission(Permission permission);

    /**
     * 更新权限
     */
    boolean updatePermission(Permission permission);

    /**
     * 删除权限（逻辑删除）
     */
    boolean deletePermission(Long permissionId);

    /**
     * 批量删除权限
     */
    boolean batchDeletePermissions(List<Long> permissionIds);

    /**
     * 检查权限编码是否存在
     */
    boolean existsByPermissionCode(String permissionCode);

    /**
     * 检查权限编码是否存在（排除指定ID）
     */
    boolean existsByPermissionCodeExcludeId(String permissionCode, Long excludeId);

    /**
     * 根据父权限ID查询子权限
     */
    List<Permission> getPermissionsByParentId(Long parentId);

    /**
     * 获取所有可用权限
     */
    List<Permission> getAllAvailablePermissions();

    /**
     * 根据权限编码查询权限
     */
    Permission getPermissionByCode(String permissionCode);

    /**
     * 检查用户是否拥有指定权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否拥有指定路径权限
     */
    boolean hasPathPermission(Long userId, String path, String method);
} 