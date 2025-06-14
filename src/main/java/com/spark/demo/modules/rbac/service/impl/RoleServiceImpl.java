package com.spark.demo.modules.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spark.demo.entity.Role;
import com.spark.demo.entity.RoleMenu;
import com.spark.demo.entity.RolePermission;
import com.spark.demo.modules.rbac.mapper.RoleMapper;
import com.spark.demo.modules.rbac.mapper.RoleMenuMapper;
import com.spark.demo.modules.rbac.mapper.RolePermissionMapper;
import com.spark.demo.modules.rbac.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * @author spark
 * @date 2025-06-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public IPage<Role> getRolePageWithCondition(Page<Role> page, String roleCode, String roleName, Integer status) {
        return roleMapper.selectRolePageWithCondition(page, roleCode, roleName, status);
    }

    @Override
    public Role getRoleWithPermissions(Long roleId) {
        return roleMapper.selectRoleWithPermissions(roleId);
    }

    @Override
    public Role getRoleWithMenus(Long roleId) {
        return roleMapper.selectRoleWithMenus(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(Role role) {
        // 检查角色编码是否已存在
        if (existsByRoleCode(role.getRoleCode())) {
            throw new RuntimeException("角色编码已存在: " + role.getRoleCode());
        }
        
        role.setCreatedTime(new Date());
        role.setUpdatedTime(new Date());
        
        return save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Role role) {
        // 检查角色编码是否已存在（排除当前角色）
        if (existsByRoleCodeExcludeId(role.getRoleCode(), role.getId())) {
            throw new RuntimeException("角色编码已存在: " + role.getRoleCode());
        }
        
        role.setUpdatedTime(new Date());
        
        return updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 删除角色菜单关联
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 逻辑删除角色
        Role role = new Role();
        role.setId(roleId);
        role.setDeletedTime(new Date());
        
        return updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteRoles(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return true;
        }
        
        for (Long roleId : roleIds) {
            deleteRole(roleId);
        }
        
        return true;
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        return roleMapper.countByRoleCode(roleCode) > 0;
    }

    @Override
    public boolean existsByRoleCodeExcludeId(String roleCode, Long excludeId) {
        return roleMapper.countByRoleCodeExcludeId(roleCode, excludeId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 先删除原有的角色权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 如果权限列表为空，则只删除不添加
        if (CollectionUtils.isEmpty(permissionIds)) {
            return true;
        }
        
        // 批量插入新的角色权限关联
        List<RolePermission> rolePermissions = permissionIds.stream()
                .map(permissionId -> {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(roleId);
                    rolePermission.setPermissionId(permissionId);
                    rolePermission.setCreatedTime(new Date());
                    rolePermission.setUpdatedTime(new Date());
                    return rolePermission;
                })
                .collect(Collectors.toList());
        
        return rolePermissionMapper.batchInsert(rolePermissions) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenusToRole(Long roleId, List<Long> menuIds) {
        // 先删除原有的角色菜单关联
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 如果菜单列表为空，则只删除不添加
        if (CollectionUtils.isEmpty(menuIds)) {
            return true;
        }
        
        // 批量插入新的角色菜单关联
        List<RoleMenu> roleMenus = menuIds.stream()
                .map(menuId -> {
                    RoleMenu roleMenu = new RoleMenu();
                    roleMenu.setRoleId(roleId);
                    roleMenu.setMenuId(menuId);
                    roleMenu.setCreatedTime(new Date());
                    roleMenu.setUpdatedTime(new Date());
                    return roleMenu;
                })
                .collect(Collectors.toList());
        
        return roleMenuMapper.batchInsert(roleMenus) > 0;
    }

    @Override
    public List<Role> getAllAvailableRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getStatus, 1)
                   .isNull(Role::getDeletedTime)
                   .orderByAsc(Role::getSort);
        
        return list(queryWrapper);
    }

    @Override
    public Role getRoleByCode(String roleCode) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleCode, roleCode)
                   .eq(Role::getStatus, 1)
                   .isNull(Role::getDeletedTime);
        
        return getOne(queryWrapper);
    }
} 