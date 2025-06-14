# API文档说明

## 📚 概述

本项目使用 **Knife4j** (基于OpenAPI 3.0) 提供完整的API文档，包含详细的接口说明、参数示例、响应格式等。

## 🚀 快速开始

### 访问API文档

启动项目后，访问以下地址查看API文档：

- **Knife4j UI**: http://localhost:8080/api/doc.html
- **Swagger UI**: http://localhost:8080/api/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

### 认证方式

项目支持多种认证方式，推荐使用Cookie认证：

#### 方式一：Cookie认证（推荐）
1. 调用登录接口获取SessionId
2. 点击文档页面右上角 🔒 **Authorize** 按钮
3. 在 **CookieAuth** 中输入：`SESSION=your-session-id`
4. 点击 **Authorize** 完成认证

#### 方式二：Header认证
1. 调用登录接口获取SessionId
2. 在 **HeaderAuth** 中输入：`SESSION=your-session-id`
3. 系统会自动在请求头中添加Cookie

## 🔐 测试账号

| 用户名 | 密码 | 角色 | 手机号 | 说明 |
|--------|------|------|--------|------|
| admin | 123456 | admin | 13800138000 | 管理员账号，拥有所有权限 |
| testuser | 123456 | user | 13800138001 | 普通用户账号，基础权限 |

## 📋 API模块说明

### 🔐 用户认证模块 (`/v1/auth`)

提供用户注册、登录、登出等认证功能：

- `POST /v1/auth/register` - 用户注册
- `POST /v1/auth/login` - 用户登录
- `POST /v1/auth/password-login` - 密码登录（增强版）
- `POST /v1/auth/sms-login` - 短信验证码登录
- `POST /v1/auth/logout` - 用户登出
- `GET /v1/auth/status` - 检查登录状态

### 👥 用户管理模块 (`/v1/users`)

提供用户信息管理功能：

- `GET /v1/users/me` - 获取当前用户信息
- `POST /v1/users` - 新增用户（管理员）
- `GET /v1/users/{uuid}` - 获取指定用户信息
- `PUT /v1/users/{uuid}` - 更新用户信息
- `DELETE /v1/users/{uuid}` - 删除用户（管理员）
- `GET /v1/users/list` - 分页查询用户列表（管理员）

### 📱 短信验证码模块 (`/v1/sms`)

提供短信验证码功能：

- `POST /v1/sms/send` - 发送验证码
- `POST /v1/sms/verify` - 验证验证码
- `GET /v1/sms/status` - 获取发送状态

## 📊 响应格式

所有API接口统一返回格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {},
  "timestamp": "2025-01-27T10:30:00"
}
```

### 状态码说明

| 状态码 | 说明 | 示例场景 |
|--------|------|----------|
| 200 | 操作成功 | 正常业务处理 |
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | 未授权访问 | 用户未登录 |
| 403 | 权限不足 | 角色权限不够 |
| 429 | 请求过于频繁 | 触发限流 |
| 500 | 服务器内部错误 | 系统异常 |

## 🔄 API版本管理

项目支持API版本管理：

- **v1**: 当前稳定版本，推荐使用
- **v2**: 开发中版本，功能可能变更

版本信息通过URL路径区分：`/v1/auth/login` vs `/v2/auth/login`

## 🛡️ 安全特性

### 接口限流
- 登录接口：每分钟最多5次
- 短信发送：每分钟最多1次，每天最多10次
- 其他接口：每分钟最多100次

### 数据脱敏
敏感信息自动脱敏处理：
- 手机号：`138****8000`
- 邮箱：`test****@example.com`
- 身份证：`110101****1234`

### 权限控制
- `@RequireAuth`: 需要登录
- `@RequireRole("admin")`: 需要管理员角色

## 📝 使用示例

### 1. 用户注册
```bash
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "123456",
    "phone": "13800138002",
    "email": "newuser@example.com",
    "nickname": "新用户"
  }'
```

### 2. 用户登录
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

### 3. 获取用户信息（需要认证）
```bash
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "Cookie: SESSION=your-session-id"
```

## 🆘 常见问题

### Q: 如何解决401错误？
A: 401错误表示未授权访问，请先调用登录接口获取有效的SessionId，然后在请求中携带Cookie。

### Q: 如何解决403错误？
A: 403错误表示权限不足，请检查当前用户的角色是否满足接口要求。

### Q: 如何解决429错误？
A: 429错误表示请求过于频繁，请稍后重试。不同接口有不同的限流规则。

### Q: 验证码收不到怎么办？
A: 请检查手机号格式是否正确，确认没有超过发送频率限制，如果仍有问题请联系管理员。

### Q: 如何在Postman中使用？
A: 
1. 先调用登录接口获取SessionId
2. 在Postman的Headers中添加：`Cookie: SESSION=your-session-id`
3. 或者在Cookies中设置SESSION值

## 📖 更多文档

- [项目README](../README.md)
- [开发指南](./DEVELOPMENT.md)
- [部署指南](./DEPLOYMENT.md)
- [API变更日志](./API_CHANGELOG.md)

## 🤝 贡献指南

如果您发现API文档有问题或需要改进，请：

1. 提交Issue描述问题
2. 提交Pull Request修复问题
3. 联系开发团队讨论

---

**注意**: 本文档基于项目当前版本生成，如有更新请以实际API文档为准。 