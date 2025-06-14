package com.spark.demo.modules.rbac.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spark.demo.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 * @author spark
 * @date 2025-06-14
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据用户ID查询用户角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 分页查询角色列表
     */
    IPage<Role> getRolePageWithCondition(Page<Role> page, String roleCode, String roleName, Integer status);

    /**
     * 查询角色及其权限信息
     */
    Role getRoleWithPermissions(Long roleId);

    /**
     * 查询角色及其菜单信息
     */
    Role getRoleWithMenus(Long roleId);

    /**
     * 创建角色
     */
    boolean createRole(Role role);

    /**
     * 更新角色
     */
    boolean updateRole(Role role);

    /**
     * 删除角色（逻辑删除）
     */
    boolean deleteRole(Long roleId);

    /**
     * 批量删除角色
     */
    boolean batchDeleteRoles(List<Long> roleIds);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色编码是否存在（排除指定ID）
     */
    boolean existsByRoleCodeExcludeId(String roleCode, Long excludeId);

    /**
     * 为角色分配权限
     */
    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 为角色分配菜单
     */
    boolean assignMenusToRole(Long roleId, List<Long> menuIds);

    /**
     * 获取所有可用角色
     */
    List<Role> getAllAvailableRoles();

    /**
     * 根据角色编码查询角色
     */
    Role getRoleByCode(String roleCode);
} 