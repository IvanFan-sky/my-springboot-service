package com.spark.demo.modules.rbac.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spark.demo.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 * @author spark
 * @date 2025-06-14
 */
public interface MenuService extends IService<Menu> {

    /**
     * 根据用户ID查询用户菜单列表
     */
    List<Menu> getMenusByUserId(Long userId);

    /**
     * 根据角色ID查询菜单列表
     */
    List<Menu> getMenusByRoleId(Long roleId);

    /**
     * 分页查询菜单列表
     */
    IPage<Menu> getMenuPageWithCondition(Page<Menu> page, String menuName, 
                                       String menuCode, Integer type, Integer status);

    /**
     * 查询菜单树结构
     */
    List<Menu> getMenuTree();

    /**
     * 根据用户ID查询用户菜单树
     */
    List<Menu> getUserMenuTree(Long userId);

    /**
     * 创建菜单
     */
    boolean createMenu(Menu menu);

    /**
     * 更新菜单
     */
    boolean updateMenu(Menu menu);

    /**
     * 删除菜单（逻辑删除）
     */
    boolean deleteMenu(Long menuId);

    /**
     * 批量删除菜单
     */
    boolean batchDeleteMenus(List<Long> menuIds);

    /**
     * 检查菜单编码是否存在
     */
    boolean existsByMenuCode(String menuCode);

    /**
     * 检查菜单编码是否存在（排除指定ID）
     */
    boolean existsByMenuCodeExcludeId(String menuCode, Long excludeId);

    /**
     * 根据父菜单ID查询子菜单
     */
    List<Menu> getMenusByParentId(Long parentId);

    /**
     * 获取所有可用菜单
     */
    List<Menu> getAllAvailableMenus();

    /**
     * 根据菜单编码查询菜单
     */
    Menu getMenuByCode(String menuCode);

    /**
     * 检查用户是否有菜单访问权限
     */
    boolean hasMenuAccess(Long userId, String menuCode);

    /**
     * 根据路径查询菜单
     */
    Menu getMenuByPath(String path);

    /**
     * 获取用户可访问的菜单路径列表
     */
    List<String> getUserAccessibleMenuPaths(Long userId);
} 