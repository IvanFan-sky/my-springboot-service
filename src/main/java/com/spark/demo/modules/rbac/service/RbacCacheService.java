package com.spark.demo.modules.rbac.service;

import com.spark.demo.entity.Role;
import com.spark.demo.entity.Permission;
import com.spark.demo.entity.Menu;

import java.util.List;
import java.util.Set;

/**
 * RBAC缓存服务接口
 * 提供权限、角色、菜单的缓存管理功能
 * 
 * @author spark
 * @date 2025-01-01
 */
public interface RbacCacheService {

    // ==================== 用户权限缓存 ====================

    /**
     * 获取用户权限列表（缓存）
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getUserPermissions(Long userId);

    /**
     * 获取用户权限编码集合（缓存）
     * @param userId 用户ID
     * @return 权限编码集合
     */
    Set<String> getUserPermissionCodes(Long userId);

    /**
     * 检查用户是否拥有指定权限（缓存）
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 刷新用户权限缓存
     * @param userId 用户ID
     */
    void refreshUserPermissions(Long userId);

    /**
     * 清除用户权限缓存
     * @param userId 用户ID
     */
    void clearUserPermissions(Long userId);

    // ==================== 用户角色缓存 ====================

    /**
     * 获取用户角色列表（缓存）
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 获取用户角色编码集合（缓存）
     * @param userId 用户ID
     * @return 角色编码集合
     */
    Set<String> getUserRoleCodes(Long userId);

    /**
     * 检查用户是否拥有指定角色（缓存）
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否拥有角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 刷新用户角色缓存
     * @param userId 用户ID
     */
    void refreshUserRoles(Long userId);

    /**
     * 清除用户角色缓存
     * @param userId 用户ID
     */
    void clearUserRoles(Long userId);

    // ==================== 用户菜单缓存 ====================

    /**
     * 获取用户菜单列表（缓存）
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<Menu> getUserMenus(Long userId);

    /**
     * 获取用户菜单树（缓存）
     * @param userId 用户ID
     * @return 菜单树
     */
    List<Menu> getUserMenuTree(Long userId);

    /**
     * 获取用户可访问菜单路径集合（缓存）
     * @param userId 用户ID
     * @return 菜单路径集合
     */
    Set<String> getUserMenuPaths(Long userId);

    /**
     * 检查用户是否有菜单访问权限（缓存）
     * @param userId 用户ID
     * @param menuCode 菜单编码
     * @return 是否有访问权限
     */
    boolean hasMenuAccess(Long userId, String menuCode);

    /**
     * 刷新用户菜单缓存
     * @param userId 用户ID
     */
    void refreshUserMenus(Long userId);

    /**
     * 清除用户菜单缓存
     * @param userId 用户ID
     */
    void clearUserMenus(Long userId);

    // ==================== 路径权限缓存 ====================

    /**
     * 获取用户可访问的API路径集合（缓存）
     * @param userId 用户ID
     * @return API路径集合
     */
    Set<String> getUserApiPaths(Long userId);

    /**
     * 检查用户是否有API路径访问权限（缓存）
     * @param userId 用户ID
     * @param path API路径
     * @param method HTTP方法
     * @return 是否有访问权限
     */
    boolean hasApiAccess(Long userId, String path, String method);

    /**
     * 刷新用户API路径缓存
     * @param userId 用户ID
     */
    void refreshUserApiPaths(Long userId);

    /**
     * 清除用户API路径缓存
     * @param userId 用户ID
     */
    void clearUserApiPaths(Long userId);

    // ==================== 综合缓存管理 ====================

    /**
     * 刷新用户所有RBAC缓存
     * @param userId 用户ID
     */
    void refreshUserAllCache(Long userId);

    /**
     * 清除用户所有RBAC缓存
     * @param userId 用户ID
     */
    void clearUserAllCache(Long userId);

    /**
     * 清除所有用户的RBAC缓存
     */
    void clearAllUsersCache();

    /**
     * 当角色权限发生变化时，清除相关用户缓存
     * @param roleId 角色ID
     */
    void clearCacheByRoleChange(Long roleId);

    /**
     * 当权限发生变化时，清除相关用户缓存
     * @param permissionId 权限ID
     */
    void clearCacheByPermissionChange(Long permissionId);

    /**
     * 当菜单发生变化时，清除相关用户缓存
     * @param menuId 菜单ID
     */
    void clearCacheByMenuChange(Long menuId);

    // ==================== 缓存统计 ====================

    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    String getCacheStats();

    /**
     * 预热指定用户的缓存
     * @param userId 用户ID
     */
    void warmUpUserCache(Long userId);

    /**
     * 预热所有活跃用户的缓存
     */
    void warmUpAllActiveUsersCache();
} 