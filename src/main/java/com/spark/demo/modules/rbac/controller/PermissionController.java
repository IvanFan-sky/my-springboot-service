package com.spark.demo.modules.rbac.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.demo.common.result.Result;
import com.spark.demo.entity.Permission;
import com.spark.demo.modules.rbac.annotation.RequirePermission;
import com.spark.demo.modules.rbac.annotation.RequireRole;
import com.spark.demo.modules.rbac.service.PermissionService;
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
 * 权限管理API控制器
 * 提供权限的CRUD操作和树形结构查询
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rbac/permissions")
@Tag(name = "权限管理", description = "权限管理相关API")
@Validated
@RequireRole({"admin", "super_admin"}) // 类级别权限控制
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    // ==================== 权限基础操作 ====================

    @GetMapping("/page")
    @Operation(summary = "分页查询权限列表", description = "支持按权限名称、编码等条件分页查询")
    @RequirePermission("permission:read")
    public Result<IPage<Permission>> getPermissionsPage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "权限名称") @RequestParam(required = false) String permissionName,
            @Parameter(description = "权限编码") @RequestParam(required = false) String permissionCode,
            @Parameter(description = "权限类型") @RequestParam(required = false) String permissionType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        log.info("分页查询权限列表, current: {}, size: {}, permissionName: {}, permissionCode: {}, permissionType: {}, status: {}", 
                current, size, permissionName, permissionCode, permissionType, status);
        
        Page<Permission> page = new Page<>(current, size);
        // 将permissionType转换为Integer类型，如果为空则传null
        Integer typeInt = null;
        if (permissionType != null && !permissionType.isEmpty()) {
            try {
                typeInt = Integer.parseInt(permissionType);
            } catch (NumberFormatException e) {
                log.warn("权限类型转换失败: {}", permissionType);
            }
        }
        IPage<Permission> result = permissionService.getPermissionPageWithCondition(page, permissionCode, permissionName, typeInt, status);
        
        return Result.success(result);
    }

    @GetMapping("/tree")
    @Operation(summary = "查询权限树形结构", description = "获取权限的树形结构列表")
    @RequirePermission("permission:read")
    public Result<List<Permission>> getPermissionTree() {
        log.info("查询权限树形结构");
        
        List<Permission> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有权限列表", description = "获取所有可用权限的简单列表")
    @RequirePermission("permission:read")
    public Result<List<Permission>> getAllPermissions() {
        log.info("查询所有权限列表");
        
        List<Permission> permissions = permissionService.getAllAvailablePermissions();
        return Result.success(permissions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询权限详情", description = "获取权限详细信息")
    @RequirePermission("permission:read")
    public Result<Permission> getPermissionById(
            @Parameter(description = "权限ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("根据ID查询权限详情, id: {}", id);
        
        Permission permission = permissionService.getById(id);
        return Result.success(permission);
    }

    @PostMapping
    @Operation(summary = "创建新权限", description = "创建新的权限")
    @RequirePermission("permission:create")
    public Result<String> createPermission(@Valid @RequestBody Permission permission) {
        log.info("创建新权限, permissionCode: {}, permissionName: {}", permission.getPermissionCode(), permission.getPermissionName());
        
        boolean success = permissionService.createPermission(permission);
        if (success) {
            return Result.success("权限创建成功");
        } else {
            return Result.fail("权限创建失败");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限信息", description = "更新指定权限的基本信息")
    @RequirePermission("permission:update")
    public Result<String> updatePermission(
            @Parameter(description = "权限ID", required = true) @PathVariable @NotNull Long id,
            @Valid @RequestBody Permission permission) {
        
        log.info("更新权限信息, id: {}, permissionCode: {}, permissionName: {}", id, permission.getPermissionCode(), permission.getPermissionName());
        
        permission.setId(id);
        boolean success = permissionService.updatePermission(permission);
        if (success) {
            return Result.success("权限更新成功");
        } else {
            return Result.fail("权限更新失败");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "逻辑删除指定权限")
    @RequirePermission("permission:delete")
    public Result<String> deletePermission(
            @Parameter(description = "权限ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("删除权限, id: {}", id);
        
        boolean success = permissionService.deletePermission(id);
        if (success) {
            return Result.success("权限删除成功");
        } else {
            return Result.fail("权限删除失败");
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除权限", description = "批量逻辑删除多个权限")
    @RequirePermission("permission:delete")
    public Result<String> batchDeletePermissions(@RequestBody List<Long> ids) {
        log.info("批量删除权限, ids: {}", ids);
        
        boolean success = permissionService.batchDeletePermissions(ids);
        if (success) {
            return Result.success("权限批量删除成功");
        } else {
            return Result.fail("权限批量删除失败");
        }
    }

    // ==================== 用户权限操作 ====================

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户权限", description = "获取指定用户的所有权限")
    @RequirePermission("permission:read")
    public Result<List<Permission>> getUserPermissions(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户权限, userId: {}", userId);
        
        List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
        return Result.success(permissions);
    }

    @GetMapping("/user/{userId}/codes")
    @Operation(summary = "查询用户权限编码", description = "获取指定用户的所有权限编码")
    @RequirePermission("permission:read")
    public Result<List<String>> getUserPermissionCodes(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户权限编码, userId: {}", userId);
        
        // 通过获取权限列表然后提取编码
        List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .toList();
        return Result.success(permissionCodes);
    }

    @GetMapping("/check/{userId}/{permissionCode}")
    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    @RequirePermission("permission:read")
    public Result<Boolean> checkUserPermission(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @Parameter(description = "权限编码", required = true) @PathVariable String permissionCode) {
        
        log.info("检查用户权限, userId: {}, permissionCode: {}", userId, permissionCode);
        
        boolean hasPermission = permissionService.hasPermission(userId, permissionCode);
        return Result.success(hasPermission);
    }

    // ==================== 权限验证操作 ====================

    @GetMapping("/check-code/{permissionCode}")
    @Operation(summary = "检查权限编码是否存在", description = "验证权限编码的唯一性")
    @RequirePermission("permission:read")
    public Result<Boolean> checkPermissionCode(
            @Parameter(description = "权限编码", required = true) @PathVariable String permissionCode) {
        
        log.info("检查权限编码是否存在, permissionCode: {}", permissionCode);
        
        boolean exists = permissionService.existsByPermissionCode(permissionCode);
        return Result.success(exists);
    }

    @GetMapping("/code/{permissionCode}")
    @Operation(summary = "根据编码查询权限", description = "通过权限编码获取权限信息")
    @RequirePermission("permission:read")
    public Result<Permission> getPermissionByCode(
            @Parameter(description = "权限编码", required = true) @PathVariable String permissionCode) {
        
        log.info("根据编码查询权限, permissionCode: {}", permissionCode);
        
        Permission permission = permissionService.getPermissionByCode(permissionCode);
        return Result.success(permission);
    }

    // ==================== 权限路径操作 ====================

    @GetMapping("/check-path")
    @Operation(summary = "检查路径权限", description = "检查用户是否有访问指定路径的权限")
    @RequirePermission("permission:read")
    public Result<Boolean> checkPathPermission(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Parameter(description = "请求路径", required = true) @RequestParam String path,
            @Parameter(description = "请求方法", required = true) @RequestParam String method) {
        
        log.info("检查路径权限, userId: {}, path: {}, method: {}", userId, path, method);
        
        boolean hasPermission = permissionService.hasPathPermission(userId, path, method);
        return Result.success(hasPermission);
    }

    @GetMapping("/path")
    @Operation(summary = "根据路径查询权限", description = "通过请求路径获取对应的权限信息")
    @RequirePermission("permission:read")
    public Result<List<Permission>> getPermissionsByPath(
            @Parameter(description = "请求路径", required = true) @RequestParam String path,
            @Parameter(description = "请求方法") @RequestParam(required = false) String method) {
        
        log.info("根据路径查询权限, path: {}, method: {}", path, method);
        
        // 使用现有的方法
        List<Permission> permissions = permissionService.getPermissionsByCodeOrPath(null, path, method);
        return Result.success(permissions);
    }

    // ==================== 权限父子关系操作 ====================

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "查询子权限列表", description = "获取指定父权限下的所有子权限")
    @RequirePermission("permission:read")
    public Result<List<Permission>> getPermissionsByParentId(
            @Parameter(description = "父权限ID", required = true) @PathVariable @NotNull Long parentId) {
        
        log.info("查询子权限列表, parentId: {}", parentId);
        
        List<Permission> permissions = permissionService.getPermissionsByParentId(parentId);
        return Result.success(permissions);
    }
} 