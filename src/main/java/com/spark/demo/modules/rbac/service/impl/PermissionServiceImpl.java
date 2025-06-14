package com.spark.demo.modules.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spark.demo.entity.Permission;
import com.spark.demo.modules.rbac.mapper.PermissionMapper;
import com.spark.demo.modules.rbac.mapper.RolePermissionMapper;
import com.spark.demo.modules.rbac.mapper.UserRoleMapper;
import com.spark.demo.modules.rbac.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Date;

/**
 * 权限服务实现类
 * @author spark
 * @date 2025-06-14
 */
@Slf4j
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        log.info("查询用户权限列表, userId: {}", userId);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
            log.info("用户权限查询成功, userId: {}, 权限数量: {}", userId, permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("查询用户权限列表失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        log.info("查询角色权限列表, roleId: {}", roleId);
        if (roleId == null) {
            return new ArrayList<>();
        }
        
        try {
            List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
            log.info("角色权限查询成功, roleId: {}, 权限数量: {}", roleId, permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("查询角色权限列表失败, roleId: {}", roleId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public IPage<Permission> getPermissionPageWithCondition(Page<Permission> page, String permissionCode, 
                                                          String permissionName, Integer type, Integer status) {
        log.info("分页查询权限列表, page: {}, size: {}, permissionCode: {}, permissionName: {}, type: {}, status: {}", 
                page.getCurrent(), page.getSize(), permissionCode, permissionName, type, status);
        
        try {
            LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(permissionCode)) {
                queryWrapper.like(Permission::getPermissionCode, permissionCode);
            }
            if (StringUtils.hasText(permissionName)) {
                queryWrapper.like(Permission::getPermissionName, permissionName);
            }
            if (type != null) {
                queryWrapper.eq(Permission::getType, type);
            }
            if (status != null) {
                queryWrapper.eq(Permission::getStatus, status);
            }
            
            queryWrapper.orderByAsc(Permission::getSort)
                       .orderByDesc(Permission::getCreatedTime);
            
            IPage<Permission> result = permissionMapper.selectPage(page, queryWrapper);
            log.info("权限分页查询成功, 总数: {}, 当前页数据: {}", result.getTotal(), result.getRecords().size());
            return result;
        } catch (Exception e) {
            log.error("分页查询权限列表失败", e);
            return new Page<>();
        }
    }

    @Override
    public List<Permission> getPermissionTree() {
        log.info("查询权限树结构");
        
        try {
            // 查询所有可用权限
            List<Permission> allPermissions = getAllAvailablePermissions();
            
            if (CollectionUtils.isEmpty(allPermissions)) {
                return new ArrayList<>();
            }
            
            // 构建权限树 - 返回根节点权限
            List<Permission> tree = buildPermissionTree(allPermissions, 0L);
            log.info("权限树构建成功, 根节点数量: {}", tree.size());
            return tree;
        } catch (Exception e) {
            log.error("查询权限树结构失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Permission> getPermissionsByCodeOrPath(String permissionCode, String path, String method) {
        log.info("根据权限编码和路径查询权限, permissionCode: {}, path: {}, method: {}", permissionCode, path, method);
        
        try {
            LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Permission::getStatus, 1); // 只查询启用的权限
            
            if (StringUtils.hasText(permissionCode)) {
                queryWrapper.eq(Permission::getPermissionCode, permissionCode);
            }
            
            if (StringUtils.hasText(path)) {
                queryWrapper.eq(Permission::getPath, path);
                if (StringUtils.hasText(method)) {
                    queryWrapper.eq(Permission::getMethod, method);
                }
            }
            
            List<Permission> permissions = permissionMapper.selectList(queryWrapper);
            log.info("权限查询成功, 数量: {}", permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("根据权限编码和路径查询权限失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPermission(Permission permission) {
        log.info("创建权限, permissionCode: {}, permissionName: {}", 
                permission.getPermissionCode(), permission.getPermissionName());
        
        try {
            // 检查权限编码是否已存在
            if (existsByPermissionCode(permission.getPermissionCode())) {
                log.warn("权限编码已存在, permissionCode: {}", permission.getPermissionCode());
                return false;
            }
            
            // 设置创建时间
            permission.setCreatedTime(new Date());
            permission.setUpdatedTime(new Date());
            
            // 如果没有设置状态，默认启用
            if (permission.getStatus() == null) {
                permission.setStatus(1);
            }
            
            // 如果没有设置排序，默认为0
            if (permission.getSort() == null) {
                permission.setSort(0);
            }
            
            int result = permissionMapper.insert(permission);
            boolean success = result > 0;
            
            if (success) {
                log.info("权限创建成功, permissionId: {}", permission.getId());
            } else {
                log.warn("权限创建失败");
            }
            
            return success;
        } catch (Exception e) {
            log.error("创建权限失败", e);
            throw new RuntimeException("创建权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(Permission permission) {
        log.info("更新权限, permissionId: {}, permissionCode: {}", 
                permission.getId(), permission.getPermissionCode());
        
        try {
            // 检查权限是否存在
            Permission existingPermission = permissionMapper.selectById(permission.getId());
            if (existingPermission == null) {
                log.warn("权限不存在, permissionId: {}", permission.getId());
                return false;
            }
            
            // 检查权限编码是否被其他权限使用
            if (StringUtils.hasText(permission.getPermissionCode()) && 
                existsByPermissionCodeExcludeId(permission.getPermissionCode(), permission.getId())) {
                log.warn("权限编码已被其他权限使用, permissionCode: {}", permission.getPermissionCode());
                return false;
            }
            
            // 设置更新时间
            permission.setUpdatedTime(new Date());
            
            int result = permissionMapper.updateById(permission);
            boolean success = result > 0;
            
            if (success) {
                log.info("权限更新成功, permissionId: {}", permission.getId());
            } else {
                log.warn("权限更新失败");
            }
            
            return success;
        } catch (Exception e) {
            log.error("更新权限失败", e);
            throw new RuntimeException("更新权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long permissionId) {
        log.info("删除权限, permissionId: {}", permissionId);
        
        try {
            // 检查权限是否存在
            Permission permission = permissionMapper.selectById(permissionId);
            if (permission == null) {
                log.warn("权限不存在, permissionId: {}", permissionId);
                return false;
            }
            
            // 检查是否有子权限
            List<Permission> children = getPermissionsByParentId(permissionId);
            if (!CollectionUtils.isEmpty(children)) {
                log.warn("权限存在子权限，无法删除, permissionId: {}, 子权限数量: {}", permissionId, children.size());
                return false;
            }
            
            // 逻辑删除：设置状态为已删除
            permission.setStatus(0);
            permission.setUpdatedTime(new Date());
            
            int result = permissionMapper.updateById(permission);
            boolean success = result > 0;
            
            if (success) {
                log.info("权限删除成功, permissionId: {}", permissionId);
            } else {
                log.warn("权限删除失败");
            }
            
            return success;
        } catch (Exception e) {
            log.error("删除权限失败", e);
            throw new RuntimeException("删除权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeletePermissions(List<Long> permissionIds) {
        log.info("批量删除权限, 数量: {}", permissionIds.size());
        
        if (CollectionUtils.isEmpty(permissionIds)) {
            return true;
        }
        
        try {
            int successCount = 0;
            for (Long permissionId : permissionIds) {
                if (deletePermission(permissionId)) {
                    successCount++;
                }
            }
            
            boolean allSuccess = successCount == permissionIds.size();
            log.info("批量删除权限完成, 成功: {}, 总数: {}", successCount, permissionIds.size());
            return allSuccess;
        } catch (Exception e) {
            log.error("批量删除权限失败", e);
            throw new RuntimeException("批量删除权限失败: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByPermissionCode(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return false;
        }
        
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getPermissionCode, permissionCode);
        
        return permissionMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByPermissionCodeExcludeId(String permissionCode, Long excludeId) {
        if (!StringUtils.hasText(permissionCode)) {
            return false;
        }
        
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getPermissionCode, permissionCode);
        
        if (excludeId != null) {
            queryWrapper.ne(Permission::getId, excludeId);
        }
        
        return permissionMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<Permission> getPermissionsByParentId(Long parentId) {
        log.info("查询子权限, parentId: {}", parentId);
        
        try {
            LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Permission::getParentId, parentId == null ? 0L : parentId)
                       .eq(Permission::getStatus, 1)
                       .orderByAsc(Permission::getSort)
                       .orderByDesc(Permission::getCreatedTime);
            
            List<Permission> permissions = permissionMapper.selectList(queryWrapper);
            log.info("子权限查询成功, parentId: {}, 数量: {}", parentId, permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("查询子权限失败, parentId: {}", parentId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Permission> getAllAvailablePermissions() {
        log.info("查询所有可用权限");
        
        try {
            LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Permission::getStatus, 1)
                       .orderByAsc(Permission::getSort)
                       .orderByDesc(Permission::getCreatedTime);
            
            List<Permission> permissions = permissionMapper.selectList(queryWrapper);
            log.info("可用权限查询成功, 数量: {}", permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("查询所有可用权限失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Permission getPermissionByCode(String permissionCode) {
        log.info("根据权限编码查询权限, permissionCode: {}", permissionCode);
        
        if (!StringUtils.hasText(permissionCode)) {
            return null;
        }
        
        try {
            LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Permission::getPermissionCode, permissionCode)
                       .eq(Permission::getStatus, 1);
            
            Permission permission = permissionMapper.selectOne(queryWrapper);
            if (permission != null) {
                log.info("权限查询成功, permissionCode: {}, permissionId: {}", permissionCode, permission.getId());
            } else {
                log.warn("权限不存在, permissionCode: {}", permissionCode);
            }
            return permission;
        } catch (Exception e) {
            log.error("根据权限编码查询权限失败, permissionCode: {}", permissionCode, e);
            return null;
        }
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        log.debug("检查用户权限, userId: {}, permissionCode: {}", userId, permissionCode);
        
        if (userId == null || !StringUtils.hasText(permissionCode)) {
            return false;
        }
        
        try {
            List<Permission> userPermissions = getPermissionsByUserId(userId);
            boolean hasPermission = userPermissions.stream()
                    .anyMatch(permission -> permissionCode.equals(permission.getPermissionCode()));
            
            log.debug("用户权限检查结果, userId: {}, permissionCode: {}, hasPermission: {}", 
                     userId, permissionCode, hasPermission);
            return hasPermission;
        } catch (Exception e) {
            log.error("检查用户权限失败, userId: {}, permissionCode: {}", userId, permissionCode, e);
            return false;
        }
    }

    @Override
    public boolean hasPathPermission(Long userId, String path, String method) {
        log.debug("检查用户路径权限, userId: {}, path: {}, method: {}", userId, path, method);
        
        if (userId == null || !StringUtils.hasText(path)) {
            return false;
        }
        
        try {
            List<Permission> userPermissions = getPermissionsByUserId(userId);
            boolean hasPermission = userPermissions.stream()
                    .anyMatch(permission -> {
                        boolean pathMatch = path.equals(permission.getPath());
                        boolean methodMatch = !StringUtils.hasText(method) || 
                                            !StringUtils.hasText(permission.getMethod()) ||
                                            method.equalsIgnoreCase(permission.getMethod());
                        return pathMatch && methodMatch;
                    });
            
            log.debug("用户路径权限检查结果, userId: {}, path: {}, method: {}, hasPermission: {}", 
                     userId, path, method, hasPermission);
            return hasPermission;
        } catch (Exception e) {
            log.error("检查用户路径权限失败, userId: {}, path: {}, method: {}", userId, path, method, e);
            return false;
        }
    }

    /**
     * 构建权限树结构（返回根节点权限列表）
     */
    private List<Permission> buildPermissionTree(List<Permission> allPermissions, Long parentId) {
        List<Permission> tree = new ArrayList<>();
        
        for (Permission permission : allPermissions) {
            if (Objects.equals(permission.getParentId(), parentId)) {
                tree.add(permission);
            }
        }
        
        // 按排序字段排序
        tree.sort(Comparator.comparing(Permission::getSort, Comparator.nullsLast(Integer::compareTo)));
        
        return tree;
    }
} 