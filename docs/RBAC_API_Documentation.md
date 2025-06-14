# RBAC权限管理系统 API文档

## 概述

本文档描述了基于角色的访问控制（RBAC）系统的API接口，包括角色管理、权限管理、菜单管理和用户权限集成等功能。

## 基础信息

- **Base URL**: `http://localhost:8080/api/v1`
- **认证方式**: Session或Token
- **数据格式**: JSON
- **字符编码**: UTF-8

## 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-01-01T12:00:00"
}
```

## 1. 角色管理 API

### 1.1 创建角色

**接口地址**: `POST /rbac/roles`

**请求参数**:
```json
{
  "roleCode": "test_role",
  "roleName": "测试角色",
  "description": "测试角色描述",
  "sort": 1,
  "status": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "角色创建成功",
  "data": {
    "id": 1,
    "roleCode": "test_role",
    "roleName": "测试角色",
    "description": "测试角色描述",
    "sort": 1,
    "status": 1,
    "createdTime": "2025-01-01T12:00:00",
    "updatedTime": "2025-01-01T12:00:00"
  }
}
```

### 1.2 更新角色

**接口地址**: `PUT /rbac/roles/{id}`

**请求参数**:
```json
{
  "roleName": "更新后的角色名",
  "description": "更新后的描述",
  "status": 1
}
```

### 1.3 删除角色

**接口地址**: `DELETE /rbac/roles/{id}`

**响应示例**:
```json
{
  "code": 200,
  "message": "角色删除成功"
}
```

### 1.4 查询角色列表

**接口地址**: `GET /rbac/roles`

**查询参数**:
- `page`: 页码（默认1）
- `size`: 每页大小（默认10）
- `roleCode`: 角色编码（可选）
- `roleName`: 角色名称（可选）
- `status`: 状态（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "roleCode": "admin",
        "roleName": "管理员",
        "description": "系统管理员",
        "sort": 1,
        "status": 1
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 1.5 为角色分配权限

**接口地址**: `POST /rbac/roles/{id}/permissions`

**请求参数**:
```json
{
  "permissionIds": [1, 2, 3]
}
```

### 1.6 为角色分配菜单

**接口地址**: `POST /rbac/roles/{id}/menus`

**请求参数**:
```json
{
  "menuIds": [1, 2, 3]
}
```

## 2. 权限管理 API

### 2.1 创建权限

**接口地址**: `POST /rbac/permissions`

**请求参数**:
```json
{
  "permissionCode": "user:read",
  "permissionName": "查看用户",
  "type": 3,
  "parentId": 0,
  "path": "/api/v1/users",
  "method": "GET",
  "description": "查看用户信息的权限",
  "sort": 1,
  "status": 1
}
```

### 2.2 查询权限树

**接口地址**: `GET /rbac/permissions/tree`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "permissionCode": "user:manage",
      "permissionName": "用户管理",
      "type": 1,
      "parentId": 0,
      "children": [
        {
          "id": 2,
          "permissionCode": "user:read",
          "permissionName": "查看用户",
          "type": 3,
          "parentId": 1,
          "path": "/api/v1/users",
          "method": "GET"
        }
      ]
    }
  ]
}
```

### 2.3 查询权限列表

**接口地址**: `GET /rbac/permissions`

**查询参数**:
- `page`: 页码
- `size`: 每页大小
- `permissionCode`: 权限编码
- `permissionName`: 权限名称
- `type`: 权限类型
- `status`: 状态

## 3. 菜单管理 API

### 3.1 创建菜单

**接口地址**: `POST /rbac/menus`

**请求参数**:
```json
{
  "menuCode": "user_management",
  "menuName": "用户管理",
  "parentId": 0,
  "path": "/user",
  "component": "user/UserList",
  "icon": "user",
  "type": 2,
  "hidden": 0,
  "sort": 1,
  "status": 1,
  "remark": "用户管理菜单"
}
```

### 3.2 查询菜单树

**接口地址**: `GET /rbac/menus/tree`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "menuCode": "system",
      "menuName": "系统管理",
      "parentId": 0,
      "path": "/system",
      "icon": "setting",
      "type": 1,
      "children": [
        {
          "id": 2,
          "menuCode": "user_management",
          "menuName": "用户管理",
          "parentId": 1,
          "path": "/system/users",
          "component": "system/UserList",
          "type": 2
        }
      ]
    }
  ]
}
```

## 4. 用户权限集成 API

### 4.1 查询用户角色

**接口地址**: `GET /rbac/users/{userId}/roles`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "roleCode": "admin",
      "roleName": "管理员",
      "description": "系统管理员"
    }
  ]
}
```

### 4.2 为用户分配角色

**接口地址**: `POST /rbac/users/{userId}/roles`

**请求参数**:
```json
[1, 2, 3]
```

### 4.3 查询用户权限

**接口地址**: `GET /rbac/users/{userId}/permissions`

### 4.4 查询用户权限编码

**接口地址**: `GET /rbac/users/{userId}/permissions/codes`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": ["user:read", "user:create", "user:update", "user:delete"]
}
```

### 4.5 检查用户权限

**接口地址**: `GET /rbac/users/{userId}/permissions/check/{permissionCode}`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": true
}
```

### 4.6 查询用户菜单树

**接口地址**: `GET /rbac/users/{userId}/menus/tree`

### 4.7 批量验证用户权限

**接口地址**: `POST /rbac/users/{userId}/validate`

**请求参数**:
```json
["user:read", "user:create", "user:delete"]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "验证成功",
  "data": [true, true, false]
}
```

### 4.8 用户权限统计

**接口地址**: `GET /rbac/users/{userId}/stats`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1,
    "roleCount": 2,
    "permissionCount": 15,
    "menuCount": 8,
    "roles": [...],
    "permissions": [...],
    "menus": [...]
  }
}
```

## 5. 缓存管理 API

### 5.1 清除用户权限缓存

**接口地址**: `DELETE /rbac/users/{userId}/cache`

### 5.2 清除所有权限缓存

**接口地址**: `DELETE /rbac/users/cache/all`

## 6. 权限注解使用

### 6.1 @RequirePermission 注解

```java
@RestController
public class UserController {
    
    @GetMapping("/users")
    @RequirePermission("user:read")
    public Result<List<User>> getUsers() {
        // 需要 user:read 权限
    }
    
    @PostMapping("/users")
    @RequirePermission(value = {"user:create", "user:manage"}, logical = Logical.OR)
    public Result<User> createUser(@RequestBody User user) {
        // 需要 user:create 或 user:manage 权限
    }
    
    @DeleteMapping("/users/{id}")
    @RequirePermission(value = "user:delete", message = "您没有删除用户的权限")
    public Result<String> deleteUser(@PathVariable Long id) {
        // 自定义错误消息
    }
}
```

### 6.2 @RequireRole 注解

```java
@RestController
@RequireRole("admin") // 类级别角色控制
public class AdminController {
    
    @GetMapping("/admin/users")
    @RequireRole(value = {"admin", "super_admin"}, logical = Logical.OR)
    public Result<List<User>> getAdminUsers() {
        // 需要 admin 或 super_admin 角色
    }
}
```

## 7. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 用户未登录 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 8. 权限验证流程

1. **请求拦截**: 通过过滤器和拦截器拦截请求
2. **用户识别**: 从Session或Token中获取用户信息
3. **权限检查**: 查询用户权限并进行验证
4. **缓存优化**: 使用Redis缓存提高性能
5. **结果返回**: 根据验证结果允许或拒绝访问

## 9. 最佳实践

### 9.1 权限设计原则

- **最小权限原则**: 用户只拥有完成工作所需的最小权限
- **职责分离**: 不同角色承担不同职责
- **权限继承**: 子权限继承父权限的特性
- **动态权限**: 支持运行时权限变更

### 9.2 性能优化

- **缓存策略**: 合理使用缓存减少数据库查询
- **批量操作**: 支持批量权限验证
- **异步处理**: 权限变更异步更新缓存
- **索引优化**: 数据库表建立合适索引

### 9.3 安全建议

- **权限校验**: 所有敏感操作都要进行权限校验
- **日志记录**: 记录权限相关的操作日志
- **定期审计**: 定期审计用户权限分配
- **异常监控**: 监控异常的权限访问行为 