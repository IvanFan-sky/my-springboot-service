package com.spark.demo.modules.rbac.controller;

import com.spark.demo.common.result.Result;
import com.spark.demo.entity.Role;
import com.spark.demo.entity.Permission;
import com.spark.demo.entity.Menu;
import com.spark.demo.modules.rbac.annotation.RequirePermission;
import com.spark.demo.modules.rbac.annotation.RequireRole;
import com.spark.demo.modules.rbac.service.RbacCacheService;
import com.spark.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 用户权限集成API控制器
 * 提供用户角色分配、权限查询等功能
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rbac/users")
@Tag(name = "用户权限管理", description = "用户权限管理相关API")
@Validated
@RequireRole({"admin", "super_admin"}) // 类级别权限控制
public class UserRbacController {

    @Autowired
    private UserService userService;

    @Autowired
    private RbacCacheService rbacCacheService;

    // ==================== 用户角色管理 ====================

    @GetMapping("/{userId}/roles")
    @Operation(summary = "查询用户角色列表", description = "获取指定用户的所有角色")
    @RequirePermission("user:read")
    public Result<List<Role>> getUserRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户角色列表, userId: {}", userId);
        
        List<Role> roles = userService.getUserRoles(userId);
        return Result.success(roles);
    }

    @PostMapping("/{userId}/roles")
    @Operation(summary = "分配用户角色", description = "为指定用户分配角色")
    @RequirePermission("user:assign_role")
    public Result<String> assignRolesToUser(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @RequestBody List<Long> roleIds) {
        
        log.info("分配用户角色, userId: {}, roleIds: {}", userId, roleIds);
        
        boolean success = userService.assignRolesToUser(userId, roleIds);
        if (success) {
            // 清除用户权限缓存
            rbacCacheService.clearUserAllCache(userId);
            return Result.success("角色分配成功");
        } else {
            return Result.fail("角色分配失败");
        }
    }

    @DeleteMapping("/{userId}/roles")
    @Operation(summary = "移除用户角色", description = "移除指定用户的角色")
    @RequirePermission("user:remove_role")
    public Result<String> removeRolesFromUser(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @RequestBody List<Long> roleIds) {
        
        log.info("移除用户角色, userId: {}, roleIds: {}", userId, roleIds);
        
        boolean success = userService.removeRolesFromUser(userId, roleIds);
        if (success) {
            // 清除用户权限缓存
            rbacCacheService.clearUserAllCache(userId);
            return Result.success("角色移除成功");
        } else {
            return Result.fail("角色移除失败");
        }
    }

    // ==================== 用户权限查询 ====================

    @GetMapping("/{userId}/permissions")
    @Operation(summary = "查询用户权限列表", description = "获取指定用户的所有权限")
    @RequirePermission("user:read")
    public Result<List<Permission>> getUserPermissions(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户权限列表, userId: {}", userId);
        
        List<Permission> permissions = userService.getUserPermissions(userId);
        return Result.success(permissions);
    }

    @GetMapping("/{userId}/permissions/codes")
    @Operation(summary = "查询用户权限编码", description = "获取指定用户的所有权限编码")
    @RequirePermission("user:read")
    public Result<Set<String>> getUserPermissionCodes(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户权限编码, userId: {}", userId);
        
        Set<String> permissionCodes = rbacCacheService.getUserPermissionCodes(userId);
        return Result.success(permissionCodes);
    }

    @GetMapping("/{userId}/permissions/check/{permissionCode}")
    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    @RequirePermission("user:read")
    public Result<Boolean> checkUserPermission(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @Parameter(description = "权限编码", required = true) @PathVariable String permissionCode) {
        
        log.info("检查用户权限, userId: {}, permissionCode: {}", userId, permissionCode);
        
        boolean hasPermission = userService.hasPermission(userId, permissionCode);
        return Result.success(hasPermission);
    }

    @GetMapping("/{userId}/permissions/check-path")
    @Operation(summary = "检查用户路径权限", description = "检查用户是否有访问指定路径的权限")
    @RequirePermission("user:read")
    public Result<Boolean> checkUserPathPermission(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @Parameter(description = "请求路径", required = true) @RequestParam String path,
            @Parameter(description = "请求方法", required = true) @RequestParam String method) {
        
        log.info("检查用户路径权限, userId: {}, path: {}, method: {}", userId, path, method);
        
        boolean hasPermission = userService.hasPathPermission(userId, path, method);
        return Result.success(hasPermission);
    }

    // ==================== 用户菜单查询 ====================

    @GetMapping("/{userId}/menus")
    @Operation(summary = "查询用户菜单列表", description = "获取指定用户的所有菜单")
    @RequirePermission("user:read")
    public Result<List<Menu>> getUserMenus(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户菜单列表, userId: {}", userId);
        
        List<Menu> menus = userService.getUserMenus(userId);
        return Result.success(menus);
    }

    @GetMapping("/{userId}/menus/tree")
    @Operation(summary = "查询用户菜单树", description = "获取指定用户的菜单树形结构")
    @RequirePermission("user:read")
    public Result<List<Menu>> getUserMenuTree(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户菜单树, userId: {}", userId);
        
        List<Menu> menuTree = userService.getUserMenuTree(userId);
        return Result.success(menuTree);
    }

    // ==================== 用户权限验证 ====================

    @PostMapping("/{userId}/validate")
    @Operation(summary = "批量验证用户权限", description = "批量检查用户是否拥有多个权限")
    @RequirePermission("user:read")
    public Result<List<Boolean>> validateUserPermissions(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @RequestBody List<String> permissionCodes) {
        
        log.info("批量验证用户权限, userId: {}, permissionCodes: {}", userId, permissionCodes);
        
        List<Boolean> results = permissionCodes.stream()
                .map(code -> userService.hasPermission(userId, code))
                .toList();
        return Result.success(results);
    }

    @PostMapping("/{userId}/validate-roles")
    @Operation(summary = "批量验证用户角色", description = "批量检查用户是否拥有多个角色")
    @RequirePermission("user:read")
    public Result<List<Boolean>> validateUserRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @RequestBody List<String> roleCodes) {
        
        log.info("批量验证用户角色, userId: {}, roleCodes: {}", userId, roleCodes);
        
        // 获取用户角色编码集合
        Set<String> userRoleCodes = rbacCacheService.getUserRoleCodes(userId);
        List<Boolean> results = roleCodes.stream()
                .map(userRoleCodes::contains)
                .toList();
        return Result.success(results);
    }

    // ==================== 缓存管理 ====================

    @DeleteMapping("/{userId}/cache")
    @Operation(summary = "清除用户权限缓存", description = "清除指定用户的权限缓存")
    @RequirePermission("user:manage_cache")
    public Result<String> clearUserCache(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("清除用户权限缓存, userId: {}", userId);
        
        rbacCacheService.clearUserAllCache(userId);
        return Result.success("用户权限缓存清除成功");
    }

    @DeleteMapping("/cache/all")
    @Operation(summary = "清除所有权限缓存", description = "清除系统中所有的权限缓存")
    @RequirePermission("system:manage_cache")
    public Result<String> clearAllCache() {
        log.info("清除所有权限缓存");
        
        rbacCacheService.clearAllUsersCache();
        return Result.success("所有权限缓存清除成功");
    }

    // ==================== 用户权限统计 ====================

    @GetMapping("/{userId}/stats")
    @Operation(summary = "查询用户权限统计", description = "获取用户的权限统计信息")
    @RequirePermission("user:read")
    public Result<UserPermissionStats> getUserPermissionStats(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户权限统计, userId: {}", userId);
        
        List<Role> roles = userService.getUserRoles(userId);
        List<Permission> permissions = userService.getUserPermissions(userId);
        List<Menu> menus = userService.getUserMenus(userId);
        
        UserPermissionStats stats = new UserPermissionStats();
        stats.setUserId(userId);
        stats.setRoleCount(roles.size());
        stats.setPermissionCount(permissions.size());
        stats.setMenuCount(menus.size());
        stats.setRoles(roles);
        stats.setPermissions(permissions);
        stats.setMenus(menus);
        
        return Result.success(stats);
    }

    /**
     * 用户权限统计信息
     */
    public static class UserPermissionStats {
        private Long userId;
        private Integer roleCount;
        private Integer permissionCount;
        private Integer menuCount;
        private List<Role> roles;
        private List<Permission> permissions;
        private List<Menu> menus;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Integer getRoleCount() { return roleCount; }
        public void setRoleCount(Integer roleCount) { this.roleCount = roleCount; }
        
        public Integer getPermissionCount() { return permissionCount; }
        public void setPermissionCount(Integer permissionCount) { this.permissionCount = permissionCount; }
        
        public Integer getMenuCount() { return menuCount; }
        public void setMenuCount(Integer menuCount) { this.menuCount = menuCount; }
        
        public List<Role> getRoles() { return roles; }
        public void setRoles(List<Role> roles) { this.roles = roles; }
        
        public List<Permission> getPermissions() { return permissions; }
        public void setPermissions(List<Permission> permissions) { this.permissions = permissions; }
        
        public List<Menu> getMenus() { return menus; }
        public void setMenus(List<Menu> menus) { this.menus = menus; }
    }
} 