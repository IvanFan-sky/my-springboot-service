package com.spark.demo.modules.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spark.demo.entity.Menu;
import com.spark.demo.modules.rbac.mapper.MenuMapper;
import com.spark.demo.modules.rbac.mapper.RoleMenuMapper;
import com.spark.demo.modules.rbac.mapper.UserRoleMapper;
import com.spark.demo.modules.rbac.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 * @author spark
 * @date 2025-06-14
 */
@Slf4j
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List<Menu> getMenusByUserId(Long userId) {
        log.info("查询用户菜单列表, userId: {}", userId);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            List<Menu> menus = menuMapper.selectMenusByUserId(userId);
            log.info("用户菜单查询成功, userId: {}, 菜单数量: {}", userId, menus.size());
            return menus;
        } catch (Exception e) {
            log.error("查询用户菜单列表失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Menu> getMenusByRoleId(Long roleId) {
        log.info("查询角色菜单列表, roleId: {}", roleId);
        if (roleId == null) {
            return new ArrayList<>();
        }
        
        try {
            List<Menu> menus = menuMapper.selectMenusByRoleId(roleId);
            log.info("角色菜单查询成功, roleId: {}, 菜单数量: {}", roleId, menus.size());
            return menus;
        } catch (Exception e) {
            log.error("查询角色菜单列表失败, roleId: {}", roleId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public IPage<Menu> getMenuPageWithCondition(Page<Menu> page, String menuName, 
                                              String menuCode, Integer type, Integer status) {
        log.info("分页查询菜单列表, page: {}, size: {}, menuName: {}, menuCode: {}, type: {}, status: {}", 
                page.getCurrent(), page.getSize(), menuName, menuCode, type, status);
        
        try {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(menuName)) {
                queryWrapper.like(Menu::getMenuName, menuName);
            }
            if (StringUtils.hasText(menuCode)) {
                queryWrapper.like(Menu::getMenuCode, menuCode);
            }
            if (type != null) {
                queryWrapper.eq(Menu::getType, type);
            }
            if (status != null) {
                queryWrapper.eq(Menu::getStatus, status);
            }
            
            queryWrapper.orderByAsc(Menu::getSort)
                       .orderByDesc(Menu::getCreatedTime);
            
            IPage<Menu> result = menuMapper.selectPage(page, queryWrapper);
            log.info("菜单分页查询成功, 总数: {}, 当前页数据: {}", result.getTotal(), result.getRecords().size());
            return result;
        } catch (Exception e) {
            log.error("分页查询菜单列表失败", e);
            return new Page<>();
        }
    }

    @Override
    public List<Menu> getMenuTree() {
        log.info("查询菜单树结构");
        
        try {
            // 查询所有可用菜单
            List<Menu> allMenus = getAllAvailableMenus();
            
            if (CollectionUtils.isEmpty(allMenus)) {
                return new ArrayList<>();
            }
            
            // 构建菜单树
            List<Menu> tree = buildMenuTree(allMenus, 0L);
            log.info("菜单树构建成功, 根节点数量: {}", tree.size());
            return tree;
        } catch (Exception e) {
            log.error("查询菜单树结构失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Menu> getUserMenuTree(Long userId) {
        log.info("查询用户菜单树, userId: {}", userId);
        
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            // 查询用户菜单列表
            List<Menu> userMenus = getMenusByUserId(userId);
            
            if (CollectionUtils.isEmpty(userMenus)) {
                return new ArrayList<>();
            }
            
            // 构建用户菜单树
            List<Menu> tree = buildMenuTree(userMenus, 0L);
            log.info("用户菜单树构建成功, userId: {}, 根节点数量: {}", userId, tree.size());
            return tree;
        } catch (Exception e) {
            log.error("查询用户菜单树失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMenu(Menu menu) {
        log.info("创建菜单, menuCode: {}, menuName: {}", menu.getMenuCode(), menu.getMenuName());
        
        try {
            // 检查菜单编码是否已存在
            if (existsByMenuCode(menu.getMenuCode())) {
                log.warn("菜单编码已存在, menuCode: {}", menu.getMenuCode());
                return false;
            }
            
            // 设置创建时间
            menu.setCreatedTime(new Date());
            menu.setUpdatedTime(new Date());
            
            // 如果没有设置状态，默认启用
            if (menu.getStatus() == null) {
                menu.setStatus(1);
            }
            
            // 如果没有设置排序，默认为0
            if (menu.getSort() == null) {
                menu.setSort(0);
            }
            
            // 如果没有设置隐藏状态，默认显示
            if (menu.getHidden() == null) {
                menu.setHidden(0);
            }
            
            int result = menuMapper.insert(menu);
            boolean success = result > 0;
            
            if (success) {
                log.info("菜单创建成功, menuId: {}", menu.getId());
            } else {
                log.warn("菜单创建失败");
            }
            
            return success;
        } catch (Exception e) {
            log.error("创建菜单失败", e);
            throw new RuntimeException("创建菜单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(Menu menu) {
        log.info("更新菜单, menuId: {}, menuCode: {}", menu.getId(), menu.getMenuCode());
        
        try {
            // 检查菜单是否存在
            Menu existingMenu = menuMapper.selectById(menu.getId());
            if (existingMenu == null) {
                log.warn("菜单不存在, menuId: {}", menu.getId());
                return false;
            }
            
            // 检查菜单编码是否被其他菜单使用
            if (StringUtils.hasText(menu.getMenuCode()) && 
                existsByMenuCodeExcludeId(menu.getMenuCode(), menu.getId())) {
                log.warn("菜单编码已被其他菜单使用, menuCode: {}", menu.getMenuCode());
                return false;
            }
            
            // 设置更新时间
            menu.setUpdatedTime(new Date());
            
            int result = menuMapper.updateById(menu);
            boolean success = result > 0;
            
            if (success) {
                log.info("菜单更新成功, menuId: {}", menu.getId());
            } else {
                log.warn("菜单更新失败");
            }
            
            return success;
        } catch (Exception e) {
            log.error("更新菜单失败", e);
            throw new RuntimeException("更新菜单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long menuId) {
        log.info("删除菜单, menuId: {}", menuId);
        
        try {
            // 检查菜单是否存在
            Menu menu = menuMapper.selectById(menuId);
            if (menu == null) {
                log.warn("菜单不存在, menuId: {}", menuId);
                return false;
            }
            
            // 检查是否有子菜单
            List<Menu> children = getMenusByParentId(menuId);
            if (!CollectionUtils.isEmpty(children)) {
                log.warn("菜单存在子菜单，无法删除, menuId: {}, 子菜单数量: {}", menuId, children.size());
                return false;
            }
            
            // 逻辑删除：设置状态为已删除
            menu.setStatus(0);
            menu.setUpdatedTime(new Date());
            
            int result = menuMapper.updateById(menu);
            boolean success = result > 0;
            
            if (success) {
                log.info("菜单删除成功, menuId: {}", menuId);
            } else {
                log.warn("菜单删除失败");
            }
            
            return success;
        } catch (Exception e) {
            log.error("删除菜单失败", e);
            throw new RuntimeException("删除菜单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteMenus(List<Long> menuIds) {
        log.info("批量删除菜单, 数量: {}", menuIds.size());
        
        if (CollectionUtils.isEmpty(menuIds)) {
            return true;
        }
        
        try {
            int successCount = 0;
            for (Long menuId : menuIds) {
                if (deleteMenu(menuId)) {
                    successCount++;
                }
            }
            
            boolean allSuccess = successCount == menuIds.size();
            log.info("批量删除菜单完成, 成功: {}, 总数: {}", successCount, menuIds.size());
            return allSuccess;
        } catch (Exception e) {
            log.error("批量删除菜单失败", e);
            throw new RuntimeException("批量删除菜单失败: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByMenuCode(String menuCode) {
        if (!StringUtils.hasText(menuCode)) {
            return false;
        }
        
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Menu::getMenuCode, menuCode);
        
        return menuMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByMenuCodeExcludeId(String menuCode, Long excludeId) {
        if (!StringUtils.hasText(menuCode)) {
            return false;
        }
        
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Menu::getMenuCode, menuCode);
        
        if (excludeId != null) {
            queryWrapper.ne(Menu::getId, excludeId);
        }
        
        return menuMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<Menu> getMenusByParentId(Long parentId) {
        log.info("查询子菜单, parentId: {}", parentId);
        
        try {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Menu::getParentId, parentId == null ? 0L : parentId)
                       .eq(Menu::getStatus, 1)
                       .orderByAsc(Menu::getSort)
                       .orderByDesc(Menu::getCreatedTime);
            
            List<Menu> menus = menuMapper.selectList(queryWrapper);
            log.info("子菜单查询成功, parentId: {}, 数量: {}", parentId, menus.size());
            return menus;
        } catch (Exception e) {
            log.error("查询子菜单失败, parentId: {}", parentId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Menu> getAllAvailableMenus() {
        log.info("查询所有可用菜单");
        
        try {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Menu::getStatus, 1)
                       .orderByAsc(Menu::getSort)
                       .orderByDesc(Menu::getCreatedTime);
            
            List<Menu> menus = menuMapper.selectList(queryWrapper);
            log.info("可用菜单查询成功, 数量: {}", menus.size());
            return menus;
        } catch (Exception e) {
            log.error("查询所有可用菜单失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Menu getMenuByCode(String menuCode) {
        log.info("根据菜单编码查询菜单, menuCode: {}", menuCode);
        
        if (!StringUtils.hasText(menuCode)) {
            return null;
        }
        
        try {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Menu::getMenuCode, menuCode)
                       .eq(Menu::getStatus, 1);
            
            Menu menu = menuMapper.selectOne(queryWrapper);
            if (menu != null) {
                log.info("菜单查询成功, menuCode: {}, menuId: {}", menuCode, menu.getId());
            } else {
                log.warn("菜单不存在, menuCode: {}", menuCode);
            }
            return menu;
        } catch (Exception e) {
            log.error("根据菜单编码查询菜单失败, menuCode: {}", menuCode, e);
            return null;
        }
    }

    @Override
    public boolean hasMenuAccess(Long userId, String menuCode) {
        log.debug("检查用户菜单访问权限, userId: {}, menuCode: {}", userId, menuCode);
        
        if (userId == null || !StringUtils.hasText(menuCode)) {
            return false;
        }
        
        try {
            List<Menu> userMenus = getMenusByUserId(userId);
            boolean hasAccess = userMenus.stream()
                    .anyMatch(menu -> menuCode.equals(menu.getMenuCode()));
            
            log.debug("用户菜单访问权限检查结果, userId: {}, menuCode: {}, hasAccess: {}", 
                     userId, menuCode, hasAccess);
            return hasAccess;
        } catch (Exception e) {
            log.error("检查用户菜单访问权限失败, userId: {}, menuCode: {}", userId, menuCode, e);
            return false;
        }
    }

    @Override
    public Menu getMenuByPath(String path) {
        log.info("根据路径查询菜单, path: {}", path);
        
        if (!StringUtils.hasText(path)) {
            return null;
        }
        
        try {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Menu::getPath, path)
                       .eq(Menu::getStatus, 1);
            
            Menu menu = menuMapper.selectOne(queryWrapper);
            if (menu != null) {
                log.info("菜单查询成功, path: {}, menuId: {}", path, menu.getId());
            } else {
                log.warn("菜单不存在, path: {}", path);
            }
            return menu;
        } catch (Exception e) {
            log.error("根据路径查询菜单失败, path: {}", path, e);
            return null;
        }
    }

    @Override
    public List<String> getUserAccessibleMenuPaths(Long userId) {
        log.info("获取用户可访问的菜单路径列表, userId: {}", userId);
        
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            List<Menu> userMenus = getMenusByUserId(userId);
            List<String> paths = userMenus.stream()
                    .filter(menu -> StringUtils.hasText(menu.getPath()))
                    .map(Menu::getPath)
                    .distinct()
                    .collect(Collectors.toList());
            
            log.info("用户可访问菜单路径查询成功, userId: {}, 路径数量: {}", userId, paths.size());
            return paths;
        } catch (Exception e) {
            log.error("获取用户可访问菜单路径失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建菜单树结构
     */
    private List<Menu> buildMenuTree(List<Menu> allMenus, Long parentId) {
        List<Menu> tree = new ArrayList<>();
        
        for (Menu menu : allMenus) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                // 递归查找子菜单
                List<Menu> children = buildMenuTree(allMenus, menu.getId());
                menu.setChildren(children);
                tree.add(menu);
            }
        }
        
        // 按排序字段排序
        tree.sort(Comparator.comparing(Menu::getSort, Comparator.nullsLast(Integer::compareTo)));
        
        return tree;
    }
} 