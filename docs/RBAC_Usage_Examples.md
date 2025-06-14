# RBAC权限管理系统 使用示例

## 1. 快速开始

### 1.1 系统初始化

首先运行数据库初始化脚本，创建基础的角色和权限数据：

```sql
-- 执行 src/main/resources/sql/rbac_init.sql
-- 这将创建超级管理员、管理员、普通用户等基础角色
-- 以及相应的权限和菜单数据
```

### 1.2 创建第一个管理员用户

```java
@Service
public class InitService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    public void createAdminUser() {
        // 1. 创建用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
        admin.setStatus(1);
        
        userService.save(admin);
        
        // 2. 分配超级管理员角色
        Role superAdminRole = roleService.getRoleByCode("super_admin");
        userService.assignRolesToUser(admin.getId(), 
                Arrays.asList(superAdminRole.getId()));
    }
}
```

## 2. 基础使用示例

### 2.1 角色管理示例

```java
@RestController
@RequestMapping("/api/v1/admin")
@RequireRole("admin") // 需要管理员角色
public class AdminController {
    
    @Autowired
    private RoleService roleService;
    
    // 创建新角色
    @PostMapping("/roles")
    @RequirePermission("role:create")
    public Result<Role> createRole(@RequestBody CreateRoleRequest request) {
        Role role = new Role();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(1);
        
        boolean success = roleService.createRole(role);
        if (success) {
            return Result.success(role);
        } else {
            return Result.fail("角色创建失败");
        }
    }
    
    // 为角色分配权限
    @PostMapping("/roles/{roleId}/permissions")
    @RequirePermission("role:assign_permission")
    public Result<String> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        
        boolean success = roleService.assignPermissionsToRole(roleId, permissionIds);
        return success ? Result.success("权限分配成功") : Result.fail("权限分配失败");
    }
}
```

### 2.2 用户权限检查示例

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RbacCacheService rbacCacheService;
    
    // 查看用户列表 - 需要读取权限
    @GetMapping
    @RequirePermission("user:read")
    public Result<Page<User>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<User> userPage = userService.page(new Page<>(page, size));
        return Result.success(userPage);
    }
    
    // 创建用户 - 需要创建权限
    @PostMapping
    @RequirePermission("user:create")
    public Result<User> createUser(@RequestBody CreateUserRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        
        boolean success = userService.save(user);
        return success ? Result.success(user) : Result.fail("用户创建失败");
    }
    
    // 删除用户 - 需要删除权限，且不能删除超级管理员
    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    public Result<String> deleteUser(@PathVariable Long id) {
        // 检查是否为超级管理员
        if (rbacCacheService.hasRole(id, "super_admin")) {
            return Result.fail("不能删除超级管理员");
        }
        
        boolean success = userService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }
    
    // 批量操作 - 需要多个权限
    @PostMapping("/batch")
    @RequirePermission(value = {"user:create", "user:update"}, logical = Logical.OR)
    public Result<String> batchOperation(@RequestBody BatchRequest request) {
        // 批量处理逻辑
        return Result.success("批量操作成功");
    }
}
```

### 2.3 菜单权限控制示例

```java
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private UserService userService;
    
    // 获取当前用户的菜单树
    @GetMapping("/current")
    public Result<List<Menu>> getCurrentUserMenus(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Menu> menuTree = userService.getUserMenuTree(userId);
        return Result.success(menuTree);
    }
    
    // 管理员查看所有菜单
    @GetMapping("/all")
    @RequireRole("admin")
    public Result<List<Menu>> getAllMenus() {
        List<Menu> allMenus = menuService.getMenuTree();
        return Result.success(allMenus);
    }
    
    private Long getCurrentUserId(HttpServletRequest request) {
        // 从Session或Token中获取用户ID
        HttpSession session = request.getSession();
        return (Long) session.getAttribute("userId");
    }
}
```

## 3. 高级使用示例

### 3.1 动态权限控制

```java
@Service
public class DynamicPermissionService {
    
    @Autowired
    private RbacCacheService rbacCacheService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 动态检查用户是否有访问特定资源的权限
     */
    public boolean checkResourceAccess(Long userId, String resourceType, Long resourceId) {
        // 1. 检查基础权限
        String basePermission = resourceType + ":read";
        if (!rbacCacheService.hasPermission(userId, basePermission)) {
            return false;
        }
        
        // 2. 检查资源所有权（如果是用户自己的资源）
        if ("user".equals(resourceType) && userId.equals(resourceId)) {
            return true;
        }
        
        // 3. 检查部门权限（如果是同部门资源）
        if (isSameDepartment(userId, resourceId)) {
            return rbacCacheService.hasPermission(userId, resourceType + ":read_dept");
        }
        
        // 4. 检查全局权限
        return rbacCacheService.hasPermission(userId, resourceType + ":read_all");
    }
    
    private boolean isSameDepartment(Long userId, Long resourceId) {
        // 实现部门检查逻辑
        return false;
    }
}
```

### 3.2 权限缓存预热

```java
@Component
public class RbacCacheWarmer {
    
    @Autowired
    private RbacCacheService rbacCacheService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 系统启动时预热活跃用户的权限缓存
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        // 获取活跃用户列表
        List<User> activeUsers = userService.getActiveUsers();
        
        // 并行预热缓存
        activeUsers.parallelStream().forEach(user -> {
            try {
                rbacCacheService.warmUpUserCache(user.getId());
                log.info("预热用户 {} 的权限缓存", user.getUsername());
            } catch (Exception e) {
                log.error("预热用户 {} 权限缓存失败", user.getUsername(), e);
            }
        });
    }
    
    /**
     * 定时刷新权限缓存
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void refreshCache() {
        try {
            rbacCacheService.warmUpAllActiveUsersCache();
        } catch (Exception e) {
            log.error("定时刷新权限缓存失败", e);
        }
    }
}
```

### 3.3 权限变更监听

```java
@Component
public class PermissionChangeListener {
    
    @Autowired
    private RbacCacheService rbacCacheService;
    
    /**
     * 监听角色权限变更事件
     */
    @EventListener
    public void handleRolePermissionChange(RolePermissionChangeEvent event) {
        // 清除相关用户的权限缓存
        rbacCacheService.clearCacheByRoleChange(event.getRoleId());
        
        log.info("角色 {} 权限发生变更，已清除相关缓存", event.getRoleId());
    }
    
    /**
     * 监听用户角色变更事件
     */
    @EventListener
    public void handleUserRoleChange(UserRoleChangeEvent event) {
        // 清除用户权限缓存
        rbacCacheService.clearUserAllCache(event.getUserId());
        
        log.info("用户 {} 角色发生变更，已清除权限缓存", event.getUserId());
    }
}
```

## 4. 前端集成示例

### 4.1 Vue.js 权限指令

```javascript
// permission.js
import store from '@/store'

/**
 * 权限检查指令
 * v-permission="'user:read'"
 * v-permission="['user:read', 'user:write']"
 */
export default {
  inserted(el, binding) {
    const { value } = binding
    const permissions = store.getters.permissions

    if (value) {
      const hasPermission = checkPermission(value, permissions)
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}

function checkPermission(value, permissions) {
  if (Array.isArray(value)) {
    return value.some(permission => permissions.includes(permission))
  } else {
    return permissions.includes(value)
  }
}
```

### 4.2 React 权限组件

```jsx
// PermissionWrapper.jsx
import React from 'react'
import { useSelector } from 'react-redux'

const PermissionWrapper = ({ 
  permission, 
  permissions, 
  role, 
  roles, 
  children, 
  fallback = null 
}) => {
  const userPermissions = useSelector(state => state.user.permissions)
  const userRoles = useSelector(state => state.user.roles)

  const hasPermission = () => {
    if (permission) {
      return Array.isArray(permission) 
        ? permission.some(p => userPermissions.includes(p))
        : userPermissions.includes(permission)
    }
    
    if (permissions) {
      return permissions.every(p => userPermissions.includes(p))
    }
    
    if (role) {
      return Array.isArray(role)
        ? role.some(r => userRoles.includes(r))
        : userRoles.includes(role)
    }
    
    if (roles) {
      return roles.every(r => userRoles.includes(r))
    }
    
    return true
  }

  return hasPermission() ? children : fallback
}

export default PermissionWrapper
```

### 4.3 前端路由权限控制

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

const routes = [
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, roles: ['admin', 'super_admin'] },
    children: [
      {
        path: 'users',
        component: UserManagement,
        meta: { permissions: ['user:read'] }
      },
      {
        path: 'roles',
        component: RoleManagement,
        meta: { permissions: ['role:read'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userRoles = store.getters.roles
  const userPermissions = store.getters.permissions
  
  // 检查登录状态
  if (to.meta.requiresAuth && !store.getters.isLoggedIn) {
    next('/login')
    return
  }
  
  // 检查角色权限
  if (to.meta.roles) {
    const hasRole = to.meta.roles.some(role => userRoles.includes(role))
    if (!hasRole) {
      next('/403')
      return
    }
  }
  
  // 检查具体权限
  if (to.meta.permissions) {
    const hasPermission = to.meta.permissions.every(permission => 
      userPermissions.includes(permission)
    )
    if (!hasPermission) {
      next('/403')
      return
    }
  }
  
  next()
})
```

## 5. 测试示例

### 5.1 权限验证测试

```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class RbacPermissionTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private RbacCacheService rbacCacheService;
    
    private Long testUserId;
    private Long testRoleId;
    
    @Test
    @Order(1)
    void testCreateUserAndRole() {
        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        
        userService.save(testUser);
        testUserId = testUser.getId();
        
        // 创建测试角色
        Role testRole = new Role();
        testRole.setRoleCode("test_role");
        testRole.setRoleName("测试角色");
        testRole.setDescription("测试用角色");
        testRole.setStatus(1);
        
        roleService.createRole(testRole);
        testRoleId = testRole.getId();
        
        assertNotNull(testUserId);
        assertNotNull(testRoleId);
    }
    
    @Test
    @Order(2)
    void testAssignRoleToUser() {
        boolean success = userService.assignRolesToUser(testUserId, 
                Arrays.asList(testRoleId));
        assertTrue(success);
        
        // 验证角色分配
        List<Role> userRoles = userService.getUserRoles(testUserId);
        assertTrue(userRoles.stream()
                .anyMatch(role -> role.getId().equals(testRoleId)));
    }
    
    @Test
    @Order(3)
    void testPermissionCheck() {
        // 测试用户权限检查
        boolean hasPermission = rbacCacheService.hasPermission(testUserId, "test:read");
        
        // 根据实际权限配置验证结果
        // 这里假设测试角色没有 test:read 权限
        assertFalse(hasPermission);
    }
}
```

### 5.2 性能测试

```java
@SpringBootTest
class RbacPerformanceTest {
    
    @Autowired
    private RbacCacheService rbacCacheService;
    
    @Test
    void testCachePerformance() {
        Long userId = 1L;
        int iterations = 1000;
        
        // 预热缓存
        rbacCacheService.getUserPermissionCodes(userId);
        
        // 测试缓存性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            rbacCacheService.getUserPermissionCodes(userId);
        }
        long endTime = System.currentTimeMillis();
        
        long avgTime = (endTime - startTime) / iterations;
        System.out.println("平均每次权限查询耗时: " + avgTime + "ms");
        
        // 验证性能要求（例如：平均耗时应小于10ms）
        assertTrue(avgTime < 10, "权限查询性能不符合要求");
    }
}
```

## 6. 常见问题和解决方案

### 6.1 权限缓存不一致

**问题**: 权限变更后，用户仍然能访问原本没有权限的资源。

**解决方案**:
```java
// 在权限变更后及时清除缓存
@Transactional
public boolean updateRolePermissions(Long roleId, List<Long> permissionIds) {
    boolean success = roleService.assignPermissionsToRole(roleId, permissionIds);
    if (success) {
        // 清除相关用户的权限缓存
        rbacCacheService.clearCacheByRoleChange(roleId);
    }
    return success;
}
```

### 6.2 循环权限依赖

**问题**: 权限或角色之间存在循环依赖关系。

**解决方案**:
```java
public boolean checkCircularDependency(Long parentId, Long childId) {
    Set<Long> visited = new HashSet<>();
    return hasCircularDependency(parentId, childId, visited);
}

private boolean hasCircularDependency(Long parentId, Long targetId, Set<Long> visited) {
    if (parentId.equals(targetId)) {
        return true;
    }
    
    if (visited.contains(parentId)) {
        return false;
    }
    
    visited.add(parentId);
    
    // 检查所有子节点
    List<Permission> children = permissionService.getChildPermissions(parentId);
    for (Permission child : children) {
        if (hasCircularDependency(child.getId(), targetId, visited)) {
            return true;
        }
    }
    
    return false;
}
```

### 6.3 大量用户权限查询优化

**问题**: 系统用户量大时，权限查询性能下降。

**解决方案**:
```java
// 使用批量查询和缓存预热
@Service
public class OptimizedRbacService {
    
    @Cacheable(value = "userPermissions", key = "#userIds")
    public Map<Long, Set<String>> batchGetUserPermissions(List<Long> userIds) {
        // 批量查询用户权限
        return userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> getUserPermissionCodes(userId)
                ));
    }
    
    @Async
    public void preloadUserPermissions(List<Long> userIds) {
        // 异步预加载用户权限
        userIds.forEach(this::getUserPermissionCodes);
    }
}
```

这些示例展示了RBAC系统在实际项目中的各种使用场景，从基础的权限控制到高级的性能优化，帮助开发者更好地理解和使用权限管理系统。 