package com.spark.demo.modules.rbac.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.demo.common.result.Result;
import com.spark.demo.entity.Menu;
import com.spark.demo.modules.rbac.annotation.RequirePermission;
import com.spark.demo.modules.rbac.annotation.RequireRole;
import com.spark.demo.modules.rbac.service.MenuService;
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
 * 菜单管理API控制器
 * 提供菜单的CRUD操作和树形结构查询
 * 
 * @author spark
 * @date 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rbac/menus")
@Tag(name = "菜单管理", description = "菜单管理相关API")
@Validated
@RequireRole({"admin", "super_admin"}) // 类级别权限控制
public class MenuController {

    @Autowired
    private MenuService menuService;

    // ==================== 菜单基础操作 ====================

    @GetMapping("/page")
    @Operation(summary = "分页查询菜单列表", description = "支持按菜单名称、路径等条件分页查询")
    @RequirePermission("menu:read")
    public Result<IPage<Menu>> getMenusPage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "菜单名称") @RequestParam(required = false) String menuName,
            @Parameter(description = "菜单路径") @RequestParam(required = false) String path,
            @Parameter(description = "菜单类型") @RequestParam(required = false) String menuType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        log.info("分页查询菜单列表, current: {}, size: {}, menuName: {}, path: {}, menuType: {}, status: {}", 
                current, size, menuName, path, menuType, status);
        
        Page<Menu> page = new Page<>(current, size);
        // 将menuType转换为Integer类型
        Integer typeInt = null;
        if (menuType != null && !menuType.isEmpty()) {
            try {
                typeInt = Integer.parseInt(menuType);
            } catch (NumberFormatException e) {
                log.warn("菜单类型转换失败: {}", menuType);
            }
        }
        IPage<Menu> result = menuService.getMenuPageWithCondition(page, menuName, path, typeInt, status);
        
        return Result.success(result);
    }

    @GetMapping("/tree")
    @Operation(summary = "查询菜单树形结构", description = "获取菜单的树形结构列表")
    @RequirePermission("menu:read")
    public Result<List<Menu>> getMenuTree() {
        log.info("查询菜单树形结构");
        
        List<Menu> tree = menuService.getMenuTree();
        return Result.success(tree);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有菜单列表", description = "获取所有可用菜单的简单列表")
    @RequirePermission("menu:read")
    public Result<List<Menu>> getAllMenus() {
        log.info("查询所有菜单列表");
        
        List<Menu> menus = menuService.getAllAvailableMenus();
        return Result.success(menus);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜单详情", description = "获取菜单详细信息")
    @RequirePermission("menu:read")
    public Result<Menu> getMenuById(
            @Parameter(description = "菜单ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("根据ID查询菜单详情, id: {}", id);
        
        Menu menu = menuService.getById(id);
        return Result.success(menu);
    }

    @PostMapping
    @Operation(summary = "创建新菜单", description = "创建新的菜单")
    @RequirePermission("menu:create")
    public Result<String> createMenu(@Valid @RequestBody Menu menu) {
        log.info("创建新菜单, menuName: {}", menu.getMenuName());
        
        boolean success = menuService.createMenu(menu);
        return success ? Result.success("菜单创建成功") : Result.fail("菜单创建失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单信息", description = "更新指定菜单的基本信息")
    @RequirePermission("menu:update")
    public Result<String> updateMenu(
            @Parameter(description = "菜单ID", required = true) @PathVariable @NotNull Long id,
            @Valid @RequestBody Menu menu) {
        
        log.info("更新菜单信息, id: {}", id);
        
        menu.setId(id);
        boolean success = menuService.updateMenu(menu);
        return success ? Result.success("菜单更新成功") : Result.fail("菜单更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单", description = "逻辑删除指定菜单")
    @RequirePermission("menu:delete")
    public Result<String> deleteMenu(
            @Parameter(description = "菜单ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("删除菜单, id: {}", id);
        
        boolean success = menuService.deleteMenu(id);
        return success ? Result.success("菜单删除成功") : Result.fail("菜单删除失败");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除菜单", description = "批量逻辑删除多个菜单")
    @RequirePermission("menu:delete")
    public Result<String> batchDeleteMenus(@RequestBody List<Long> ids) {
        log.info("批量删除菜单, ids: {}", ids);
        
        boolean success = menuService.batchDeleteMenus(ids);
        if (success) {
            return Result.success("菜单批量删除成功");
        } else {
            return Result.fail("菜单批量删除失败");
        }
    }

    // ==================== 用户菜单操作 ====================

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户菜单", description = "获取指定用户的所有菜单")
    @RequirePermission("menu:read")
    public Result<List<Menu>> getUserMenus(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户菜单, userId: {}", userId);
        
        List<Menu> menus = menuService.getMenusByUserId(userId);
        return Result.success(menus);
    }

    @GetMapping("/user/{userId}/tree")
    @Operation(summary = "查询用户菜单树", description = "获取指定用户的菜单树形结构")
    @RequirePermission("menu:read")
    public Result<List<Menu>> getUserMenuTree(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        
        log.info("查询用户菜单树, userId: {}", userId);
        
        List<Menu> menuTree = menuService.getUserMenuTree(userId);
        return Result.success(menuTree);
    }

    // ==================== 角色菜单操作 ====================

    @GetMapping("/role/{roleId}")
    @Operation(summary = "查询角色菜单", description = "获取指定角色的所有菜单")
    @RequirePermission("menu:read")
    public Result<List<Menu>> getRoleMenus(
            @Parameter(description = "角色ID", required = true) @PathVariable @NotNull Long roleId) {
        
        log.info("查询角色菜单, roleId: {}", roleId);
        
        List<Menu> menus = menuService.getMenusByRoleId(roleId);
        return Result.success(menus);
    }

    // ==================== 菜单层级操作 ====================

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "查询子菜单列表", description = "获取指定父菜单下的所有子菜单")
    @RequirePermission("menu:read")
    public Result<List<Menu>> getMenusByParentId(
            @Parameter(description = "父菜单ID", required = true) @PathVariable @NotNull Long parentId) {
        
        log.info("查询子菜单列表, parentId: {}", parentId);
        
        List<Menu> menus = menuService.getMenusByParentId(parentId);
        return Result.success(menus);
    }

    @GetMapping("/root")
    @Operation(summary = "查询根菜单列表", description = "获取所有根级菜单")
    @RequirePermission("menu:read")
    public Result<List<Menu>> getRootMenus() {
        log.info("查询根菜单列表");
        
        List<Menu> menus = menuService.getRootMenus();
        return Result.success(menus);
    }

    // ==================== 菜单验证操作 ====================

    @GetMapping("/check-path")
    @Operation(summary = "检查菜单路径是否存在", description = "验证菜单路径的唯一性")
    @RequirePermission("menu:read")
    public Result<Boolean> checkMenuPath(
            @Parameter(description = "菜单路径", required = true) @RequestParam String path) {
        
        log.info("检查菜单路径是否存在, path: {}", path);
        
        boolean exists = menuService.existsByPath(path);
        return Result.success(exists);
    }

    @GetMapping("/path")
    @Operation(summary = "根据路径查询菜单", description = "通过菜单路径获取菜单信息")
    @RequirePermission("menu:read")
    public Result<Menu> getMenuByPath(
            @Parameter(description = "菜单路径", required = true) @RequestParam String path) {
        
        log.info("根据路径查询菜单, path: {}", path);
        
        Menu menu = menuService.getMenuByPath(path);
        return Result.success(menu);
    }

    // ==================== 菜单排序操作 ====================

    @PutMapping("/{id}/sort")
    @Operation(summary = "更新菜单排序", description = "更新指定菜单的排序值")
    @RequirePermission("menu:update")
    public Result<String> updateMenuSort(
            @Parameter(description = "菜单ID", required = true) @PathVariable @NotNull Long id,
            @Parameter(description = "排序值", required = true) @RequestParam @NotNull Integer sortOrder) {
        
        log.info("更新菜单排序, id: {}, sortOrder: {}", id, sortOrder);
        
        boolean success = menuService.updateMenuSort(id, sortOrder);
        if (success) {
            return Result.success("菜单排序更新成功");
        } else {
            return Result.fail("菜单排序更新失败");
        }
    }

    // ==================== 菜单状态操作 ====================

    @PutMapping("/{id}/status")
    @Operation(summary = "更新菜单状态", description = "启用或禁用指定菜单")
    @RequirePermission("menu:update")
    public Result<String> updateMenuStatus(
            @Parameter(description = "菜单ID", required = true) @PathVariable @NotNull Long id,
            @Parameter(description = "状态", required = true) @RequestParam @NotNull Integer status) {
        
        log.info("更新菜单状态, id: {}, status: {}", id, status);
        
        boolean success = menuService.updateMenuStatus(id, status);
        if (success) {
            return Result.success("菜单状态更新成功");
        } else {
            return Result.fail("菜单状态更新失败");
        }
    }
} 