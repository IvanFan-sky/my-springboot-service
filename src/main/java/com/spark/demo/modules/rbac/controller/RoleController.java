package com.spark.demo.modules.rbac.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.demo.common.result.Result;
import com.spark.demo.entity.Role;
import com.spark.demo.modules.rbac.annotation.RequirePermission;
import com.spark.demo.modules.rbac.annotation.RequireRole;
import com.spark.demo.modules.rbac.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色管理API控制器
 * 提供角色的CRUD操作和权限分配功能
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rbac/roles")
@Tag(name = "角色管理", description = "角色管理相关API")
@Validated
@RequireRole({"admin", "super_admin"}) // 类级别权限控制
public class RoleController {

    @Autowired
    private RoleService roleService;

    // ==================== 角色基础操作 ====================

    @GetMapping("/page")
    @Operation(summary = "分页查询角色列表", description = "支持按角色名称、编码等条件分页查询")
    @RequirePermission("role:read")
    public Result<IPage<Role>> getRolesPage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "角色名称") @RequestParam(required = false) String roleName,
            @Parameter(description = "角色编码") @RequestParam(required = false) String roleCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        log.info("分页查询角色列表, current: {}, size: {}, roleName: {}, roleCode: {}, status: {}", 
                current, size, roleName, roleCode, status);
        
        Page<Role> page = new Page<>(current, size);
        IPage<Role> result = roleService.getRolePageWithCondition(page, roleCode, roleName, status);
        
        return Result.success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有角色列表", description = "获取所有可用角色的简单列表")
    @RequirePermission("role:read")
    public Result<List<Role>> getAllRoles() {
        log.info("查询所有角色列表");
        
        List<Role> roles = roleService.getAllAvailableRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色详情", description = "获取角色详细信息")
    @RequirePermission("role:read")
    public Result<Role> getRoleById(
            @Parameter(description = "角色ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("根据ID查询角色详情, id: {}", id);
        
        Role role = roleService.getById(id);
        return Result.success(role);
    }

    @GetMapping("/{id}/with-permissions")
    @Operation(summary = "查询角色及其权限", description = "获取角色详细信息，包括关联的权限")
    @RequirePermission("role:read")
    public Result<Role> getRoleWithPermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("查询角色及其权限, id: {}", id);
        
        Role role = roleService.getRoleWithPermissions(id);
        return Result.success(role);
    }

    @GetMapping("/{id}/with-menus")
    @Operation(summary = "查询角色及其菜单", description = "获取角色详细信息，包括关联的菜单")
    @RequirePermission("role:read")
    public Result<Role> getRoleWithMenus(
            @Parameter(description = "角色ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("查询角色及其菜单, id: {}", id);
        
        Role role = roleService.getRoleWithMenus(id);
        return Result.success(role);
    }

    @PostMapping
    @Operation(summary = "创建新角色", description = "创建新的角色")
    @RequirePermission("role:create")
    public Result<String> createRole(@Valid @RequestBody Role role) {
        log.info("创建新角色, roleCode: {}, roleName: {}", role.getRoleCode(), role.getRoleName());
        
        boolean success = roleService.createRole(role);
        if (success) {
            return Result.success("角色创建成功");
        } else {
            return Result.fail("角色创建失败");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色信息", description = "更新指定角色的基本信息")
    @RequirePermission("role:update")
    public Result<String> updateRole(
            @Parameter(description = "角色ID", required = true) @PathVariable @NotNull Long id,
            @Valid @RequestBody Role role) {
        
        log.info("更新角色信息, id: {}, roleCode: {}, roleName: {}", id, role.getRoleCode(), role.getRoleName());
        
        role.setId(id);
        boolean success = roleService.updateRole(role);
        if (success) {
            return Result.success("角色更新成功");
        } else {
            return Result.fail("角色更新失败");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "逻辑删除指定角色")
    @RequirePermission("role:delete")
    public Result<String> deleteRole(
            @Parameter(description = "角色ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("删除角色, id: {}", id);
        
        boolean success = roleService.deleteRole(id);
        if (success) {
            return Result.success("角色删除成功");
        } else {
            return Result.fail("角色删除失败");
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除角色", description = "批量逻辑删除多个角色")
    @RequirePermission("role:delete")
    public Result<String> batchDeleteRoles(@RequestBody List<Long> ids) {
        log.info("批量删除角色, ids: {}", ids);
        
        boolean success = roleService.batchDeleteRoles(ids);
        if (success) {
            return Result.success("角色批量删除成功");
        } else {
            return Result.fail("角色批量删除失败");
        }
    }

    // ==================== 权限分配操作 ====================

    @PostMapping("/{id}/permissions")
    @Operation(summary = "分配角色权限", description = "为指定角色分配权限")
    @RequirePermission("role:assign_permission")
    public Result<String> assignPermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable @NotNull Long id,
            @RequestBody List<Long> permissionIds) {
        
        log.info("分配角色权限, roleId: {}, permissionIds: {}", id, permissionIds);
        
        boolean success = roleService.assignPermissionsToRole(id, permissionIds);
        if (success) {
            return Result.success("权限分配成功");
        } else {
            return Result.fail("权限分配失败");
        }
    }

    // ==================== 菜单分配操作 ====================

    @PostMapping("/{id}/menus")
    @Operation(summary = "分配角色菜单", description = "为指定角色分配菜单")
    @RequirePermission("role:assign_menu")
    public Result<String> assignMenus(
            @Parameter(description = "角色ID", required = true) @PathVariable @NotNull Long id,
            @RequestBody List<Long> menuIds) {
        
        log.info("分配角色菜单, roleId: {}, menuIds: {}", id, menuIds);
        
        boolean success = roleService.assignMenusToRole(id, menuIds);
        if (success) {
            return Result.success("菜单分配成功");
        } else {
            return Result.fail("菜单分配失败");
        }
    }

    // ==================== 用户角色操作 ====================

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户角色", description = "获取指定用户的所有角色")
    @RequirePermission("role:read")
    public Result<List<Role>> getUserRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户角色, userId: {}", userId);
        
        List<Role> roles = roleService.getRolesByUserId(userId);
        return Result.success(roles);
    }

    // ==================== 角色验证操作 ====================

    @GetMapping("/check-code/{roleCode}")
    @Operation(summary = "检查角色编码是否存在", description = "验证角色编码的唯一性")
    @RequirePermission("role:read")
    public Result<Boolean> checkRoleCode(
            @Parameter(description = "角色编码", required = true) @PathVariable String roleCode) {
        
        log.info("检查角色编码是否存在, roleCode: {}", roleCode);
        
        boolean exists = roleService.existsByRoleCode(roleCode);
        return Result.success(exists);
    }

    @GetMapping("/code/{roleCode}")
    @Operation(summary = "根据编码查询角色", description = "通过角色编码获取角色信息")
    @RequirePermission("role:read")
    public Result<Role> getRoleByCode(
            @Parameter(description = "角色编码", required = true) @PathVariable String roleCode) {
        
        log.info("根据编码查询角色, roleCode: {}", roleCode);
        
        Role role = roleService.getRoleByCode(roleCode);
        return Result.success(role);
    }
} 