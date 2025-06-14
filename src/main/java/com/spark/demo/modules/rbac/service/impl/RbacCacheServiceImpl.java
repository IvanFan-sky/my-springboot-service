package com.spark.demo.modules.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spark.demo.entity.Role;
import com.spark.demo.entity.Permission;
import com.spark.demo.entity.Menu;
import com.spark.demo.entity.User;
import com.spark.demo.entity.UserRole;
import com.spark.demo.mapper.UserMapper;
import com.spark.demo.modules.rbac.service.RbacCacheService;
import com.spark.demo.modules.rbac.service.RoleService;
import com.spark.demo.modules.rbac.service.PermissionService;
import com.spark.demo.modules.rbac.service.MenuService;
import com.spark.demo.modules.rbac.mapper.UserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RBAC缓存服务实现类
 * 使用Spring Cache提供高性能的权限缓存
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@Service
public class RbacCacheServiceImpl implements RbacCacheService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    // ==================== 用户权限缓存 ====================

    @Override
    @Cacheable(value = "rbac:user:permissions", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Permission> getUserPermissions(Long userId) {
        log.debug("从数据库加载用户权限, userId: {}", userId);
        
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            return permissionService.getPermissionsByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户权限失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "rbac:user:permission:codes", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public Set<String> getUserPermissionCodes(Long userId) {
        log.debug("获取用户权限编码集合, userId: {}", userId);
        
        List<Permission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .map(Permission::getPermissionCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || !StringUtils.hasText(permissionCode)) {
            return false;
        }
        
        Set<String> permissionCodes = getUserPermissionCodes(userId);
        return permissionCodes.contains(permissionCode);
    }

    @Override
    @CacheEvict(value = {"rbac:user:permissions", "rbac:user:permission:codes"}, key = "#userId")
    public void refreshUserPermissions(Long userId) {
        log.info("刷新用户权限缓存, userId: {}", userId);
    }

    @Override
    @CacheEvict(value = {"rbac:user:permissions", "rbac:user:permission:codes"}, key = "#userId")
    public void clearUserPermissions(Long userId) {
        log.info("清除用户权限缓存, userId: {}", userId);
    }

    // ==================== 用户角色缓存 ====================

    @Override
    @Cacheable(value = "rbac:user:roles", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Role> getUserRoles(Long userId) {
        log.debug("从数据库加载用户角色, userId: {}", userId);
        
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            return roleService.getRolesByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户角色失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "rbac:user:role:codes", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public Set<String> getUserRoleCodes(Long userId) {
        log.debug("获取用户角色编码集合, userId: {}", userId);
        
        List<Role> roles = getUserRoles(userId);
        return roles.stream()
                .map(Role::getRoleCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null || !StringUtils.hasText(roleCode)) {
            return false;
        }
        
        Set<String> roleCodes = getUserRoleCodes(userId);
        return roleCodes.contains(roleCode);
    }

    @Override
    @CacheEvict(value = {"rbac:user:roles", "rbac:user:role:codes"}, key = "#userId")
    public void refreshUserRoles(Long userId) {
        log.info("刷新用户角色缓存, userId: {}", userId);
    }

    @Override
    @CacheEvict(value = {"rbac:user:roles", "rbac:user:role:codes"}, key = "#userId")
    public void clearUserRoles(Long userId) {
        log.info("清除用户角色缓存, userId: {}", userId);
    }

    // ==================== 用户菜单缓存 ====================

    @Override
    @Cacheable(value = "rbac:user:menus", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Menu> getUserMenus(Long userId) {
        log.debug("从数据库加载用户菜单, userId: {}", userId);
        
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            return menuService.getMenusByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户菜单失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "rbac:user:menu:tree", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Menu> getUserMenuTree(Long userId) {
        log.debug("从数据库加载用户菜单树, userId: {}", userId);
        
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            return menuService.getUserMenuTree(userId);
        } catch (Exception e) {
            log.error("获取用户菜单树失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "rbac:user:menu:paths", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public Set<String> getUserMenuPaths(Long userId) {
        log.debug("获取用户菜单路径集合, userId: {}", userId);
        
        List<Menu> menus = getUserMenus(userId);
        return menus.stream()
                .map(Menu::getPath)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasMenuAccess(Long userId, String menuCode) {
        if (userId == null || !StringUtils.hasText(menuCode)) {
            return false;
        }
        
        try {
            return menuService.hasMenuAccess(userId, menuCode);
        } catch (Exception e) {
            log.error("检查菜单访问权限失败, userId: {}, menuCode: {}", userId, menuCode, e);
            return false;
        }
    }

    @Override
    @CacheEvict(value = {"rbac:user:menus", "rbac:user:menu:tree", "rbac:user:menu:paths"}, key = "#userId")
    public void refreshUserMenus(Long userId) {
        log.info("刷新用户菜单缓存, userId: {}", userId);
    }

    @Override
    @CacheEvict(value = {"rbac:user:menus", "rbac:user:menu:tree", "rbac:user:menu:paths"}, key = "#userId")
    public void clearUserMenus(Long userId) {
        log.info("清除用户菜单缓存, userId: {}", userId);
    }

    // ==================== 路径权限缓存 ====================

    @Override
    @Cacheable(value = "rbac:user:api:paths", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public Set<String> getUserApiPaths(Long userId) {
        log.debug("获取用户API路径集合, userId: {}", userId);
        
        if (userId == null) {
            return new HashSet<>();
        }
        
        try {
            List<String> paths = menuService.getUserAccessibleMenuPaths(userId);
            return new HashSet<>(paths);
        } catch (Exception e) {
            log.error("获取用户API路径失败, userId: {}", userId, e);
            return new HashSet<>();
        }
    }

    @Override
    public boolean hasApiAccess(Long userId, String path, String method) {
        if (userId == null || !StringUtils.hasText(path)) {
            return false;
        }
        
        try {
            return permissionService.hasPathPermission(userId, path, method);
        } catch (Exception e) {
            log.error("检查API访问权限失败, userId: {}, path: {}, method: {}", userId, path, method, e);
            return false;
        }
    }

    @Override
    @CacheEvict(value = "rbac:user:api:paths", key = "#userId")
    public void refreshUserApiPaths(Long userId) {
        log.info("刷新用户API路径缓存, userId: {}", userId);
    }

    @Override
    @CacheEvict(value = "rbac:user:api:paths", key = "#userId")
    public void clearUserApiPaths(Long userId) {
        log.info("清除用户API路径缓存, userId: {}", userId);
    }

    // ==================== 综合缓存管理 ====================

    @Override
    @Caching(evict = {
        @CacheEvict(value = "rbac:user:permissions", key = "#userId"),
        @CacheEvict(value = "rbac:user:permission:codes", key = "#userId"),
        @CacheEvict(value = "rbac:user:roles", key = "#userId"),
        @CacheEvict(value = "rbac:user:role:codes", key = "#userId"),
        @CacheEvict(value = "rbac:user:menus", key = "#userId"),
        @CacheEvict(value = "rbac:user:menu:tree", key = "#userId"),
        @CacheEvict(value = "rbac:user:menu:paths", key = "#userId"),
        @CacheEvict(value = "rbac:user:api:paths", key = "#userId")
    })
    public void refreshUserAllCache(Long userId) {
        log.info("刷新用户所有RBAC缓存, userId: {}", userId);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "rbac:user:permissions", key = "#userId"),
        @CacheEvict(value = "rbac:user:permission:codes", key = "#userId"),
        @CacheEvict(value = "rbac:user:roles", key = "#userId"),
        @CacheEvict(value = "rbac:user:role:codes", key = "#userId"),
        @CacheEvict(value = "rbac:user:menus", key = "#userId"),
        @CacheEvict(value = "rbac:user:menu:tree", key = "#userId"),
        @CacheEvict(value = "rbac:user:menu:paths", key = "#userId"),
        @CacheEvict(value = "rbac:user:api:paths", key = "#userId")
    })
    public void clearUserAllCache(Long userId) {
        log.info("清除用户所有RBAC缓存, userId: {}", userId);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "rbac:user:permissions", allEntries = true),
        @CacheEvict(value = "rbac:user:permission:codes", allEntries = true),
        @CacheEvict(value = "rbac:user:roles", allEntries = true),
        @CacheEvict(value = "rbac:user:role:codes", allEntries = true),
        @CacheEvict(value = "rbac:user:menus", allEntries = true),
        @CacheEvict(value = "rbac:user:menu:tree", allEntries = true),
        @CacheEvict(value = "rbac:user:menu:paths", allEntries = true),
        @CacheEvict(value = "rbac:user:api:paths", allEntries = true)
    })
    public void clearAllUsersCache() {
        log.info("清除所有用户RBAC缓存");
    }

    @Override
    public void clearCacheByRoleChange(Long roleId) {
        log.info("角色变更，清除相关用户缓存, roleId: {}", roleId);
        
        if (roleId == null) {
            return;
        }
        
        try {
            // 查找拥有该角色的所有用户
            LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRole::getRoleId, roleId);
            List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
            
            // 清除这些用户的缓存
            for (UserRole userRole : userRoles) {
                clearUserAllCache(userRole.getUserId());
            }
            
            log.info("角色变更缓存清理完成, roleId: {}, 影响用户数: {}", roleId, userRoles.size());
        } catch (Exception e) {
            log.error("角色变更缓存清理失败, roleId: {}", roleId, e);
        }
    }

    @Override
    public void clearCacheByPermissionChange(Long permissionId) {
        log.info("权限变更，清除所有用户缓存, permissionId: {}", permissionId);
        
        // 权限变更可能影响多个角色，为简化处理，清除所有用户缓存
        clearAllUsersCache();
    }

    @Override
    public void clearCacheByMenuChange(Long menuId) {
        log.info("菜单变更，清除所有用户缓存, menuId: {}", menuId);
        
        // 菜单变更可能影响多个角色，为简化处理，清除所有用户缓存
        clearAllUsersCache();
    }

    // ==================== 缓存统计 ====================

    @Override
    public String getCacheStats() {
        // 这里可以集成具体的缓存管理器来获取统计信息
        // 例如：Caffeine、Redis等的统计信息
        return "RBAC缓存统计信息 - 功能待实现";
    }

    @Override
    public void warmUpUserCache(Long userId) {
        log.info("预热用户缓存, userId: {}", userId);
        
        if (userId == null) {
            return;
        }
        
        try {
            // 预加载用户的所有RBAC数据到缓存
            getUserPermissions(userId);
            getUserPermissionCodes(userId);
            getUserRoles(userId);
            getUserRoleCodes(userId);
            getUserMenus(userId);
            getUserMenuTree(userId);
            getUserMenuPaths(userId);
            getUserApiPaths(userId);
            
            log.info("用户缓存预热完成, userId: {}", userId);
        } catch (Exception e) {
            log.error("用户缓存预热失败, userId: {}", userId, e);
        }
    }

    @Override
    public void warmUpAllActiveUsersCache() {
        log.info("开始预热所有活跃用户缓存");
        
        try {
            // 查询所有活跃用户（最近30天登录的用户）
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getStatus, 1) // 正常状态
                   .isNull(User::getDeletedTime) // 未删除
                   .orderByDesc(User::getUpdatedTime)
                   .last("LIMIT 1000"); // 限制数量，避免一次性加载过多
            
            List<User> activeUsers = userMapper.selectList(wrapper);
            
            for (User user : activeUsers) {
                try {
                    warmUpUserCache(user.getId());
                } catch (Exception e) {
                    log.warn("预热用户缓存失败, userId: {}", user.getId(), e);
                }
            }
            
            log.info("所有活跃用户缓存预热完成, 用户数: {}", activeUsers.size());
        } catch (Exception e) {
            log.error("预热所有活跃用户缓存失败", e);
        }
    }
} 