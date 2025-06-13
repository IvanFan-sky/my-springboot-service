# Spring Boot 通用后台服务

基于 Spring Boot 3.2.0 构建的通用后台服务，提供用户管理、认证授权、短信验证码等功能。

## 功能特性

### 🚀 核心功能
- ✅ 用户注册、登录、管理
- ✅ 基于 Session + Redis 的认证机制
- ✅ 手机验证码功能（本地生成）
- ✅ 自定义注解权限控制
- ✅ 操作日志记录
- ✅ API文档（Knife4j）
- ✅ 逻辑删除功能
- ✅ 日志系统
- ✅ 数据验证
- ✅ 安全审计
- ✅ 单元测试

### 🛠 技术栈
- **Spring Boot 3.2.0** - 主框架
- **MyBatis Plus 3.5.5** - ORM框架
- **Redis** - 缓存和Session存储
- **MySQL 8.0** - 数据库
- **Knife4j 4.3.0** - API文档
- **MapStruct 1.5.5** - 对象映射
- **Lombok** - 代码简化

## 项目结构

```
src/main/java/com/spark/demo/
├── annotation/          # 自定义注解
│   ├── RequireAuth.java    # 登录认证注解
│   └── RequireRole.java    # 角色权限注解
├── aspect/              # 切面
│   ├── AuthAspect.java     # 权限检查切面
│   └── LogAspect.java      # 日志切面
├── common/              # 公共模块
│   ├── context/            # 上下文管理
│   ├── exception/          # 异常处理
│   └── result/             # 统一响应结果
├── config/              # 配置类
│   ├── FilterConfig.java   # 过滤器配置
│   ├── Knife4jConfig.java  # API文档配置
│   ├── MybatisPlusConfig.java # MyBatis配置
│   └── RedisConfig.java    # Redis配置
├── controller/          # 控制器
│   ├── SmsController.java  # 短信验证码
│   └── UserController.java # 用户管理
├── converter/           # 对象转换器
├── dto/                 # 数据传输对象
├── entity/              # 实体类
├── filter/              # 过滤器
│   └── AuthFilter.java     # 认证过滤器
├── mapper/              # 数据访问层
├── service/             # 业务逻辑层
├── vo/                  # 视图对象
└── DemoApplication.java # 启动类
```

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 5.0+
- Maven 3.6+

### 1. 克隆项目
```bash
git clone <repository-url>
cd my-springboot-service
```

### 2. 数据库配置
```bash
# 创建数据库
mysql -u root -p < src/main/resources/sql/init.sql
```

### 3. 修改配置
编辑 `src/main/resources/application.yml` 文件：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password # 如果没有密码则留空
```

### 4. 启动应用
```bash
mvn clean install
mvn spring-boot:run
```

### 5. 访问应用
- 应用地址: http://localhost:8080/api
- API文档: http://localhost:8080/api/doc.html

## API接口

### 认证相关
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户注册 | POST | `/api/v1/users/register` | 用户注册 |
| 用户登录 | POST | `/api/v1/users/login` | 用户登录（兼容接口，支持密码或验证码） |
| 密码登录 | POST | `/api/v1/users/password-login` | 密码登录（推荐使用） |
| 短信登录 | POST | `/api/v1/users/sms-login` | 短信验证码登录（推荐使用） |
| 用户登出 | POST | `/api/v1/users/logout` | 用户登出 |
| 获取当前用户信息 | GET | `/api/v1/users/me` | 获取当前登录用户信息 |

### 用户管理（需要管理员权限）
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户列表 | GET | `/api/v1/users` | 分页查询用户列表 |
| 新增用户 | POST | `/api/v1/users` | 新增用户 |
| 获取用户信息 | GET | `/api/v1/users/{id}` | 根据ID获取用户信息 |
| 更新用户信息 | PUT | `/api/v1/users/{id}` | 更新用户信息 |
| 删除用户 | DELETE | `/api/v1/users/{id}` | 删除用户（逻辑删除） |

### 短信验证码
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发送验证码 | POST | `/api/v1/sms/send` | 发送手机验证码 |
| 验证验证码 | POST | `/api/v1/sms/verify` | 验证手机验证码 |

## 权限控制

### 自定义注解
- `@RequireAuth` - 需要登录认证
- `@RequireRole({"admin", "user"})` - 需要特定角色

### 使用示例
```java
@RestController
@RequireAuth // 类级别，整个Controller都需要登录
public class UserController {
    
    @RequireRole("admin") // 方法级别，需要admin角色
    @GetMapping("/users")
    public Result<List<User>> getUsers() {
        // ...
    }
}
```

## 测试数据

系统初始化时会创建以下测试账号：

| 用户名 | 密码 | 角色 | 手机号 |
|--------|------|------|--------|
| admin | 123456 | admin | 13800138000 |
| testuser | 123456 | user | 13800138001 |

## 配置说明

### 日志配置
- 配置文件：`src/main/resources/logback-spring.xml`
- 控制台日志：彩色输出，包含时间、级别、线程、类名
- 文件日志：
  - `logs/application.log` - 应用主日志
  - `logs/business.log` - 业务日志（com.spark.demo包）
  - `logs/error.log` - 错误日志（ERROR级别）
- 异步日志：提高性能，避免日志IO阻塞
- 滚动策略：按日期和大小滚动，保留30天历史
- 环境区分：
  - 开发环境：DEBUG级别，控制台+文件输出
  - 生产环境：INFO级别，仅文件输出

> 📖 详细的日志配置说明请参考：[日志配置文档](docs/logging-config.md)

### Redis配置
- Session存储：30分钟超时
- 验证码存储：5分钟过期
- 发送频率限制：1分钟内只能发送一次
- **序列化配置**：
  - 主RedisTemplate：Jackson2JsonRedisSerializer（避免乱码）
  - 字符串操作：StringRedisTemplate（最佳性能）
  - 复杂对象：GenericJackson2JsonRedisSerializer（保留类型信息）
- **Hash操作优化**：
  - 推荐使用`hashSetString()`避免乱码
  - 提供字符串和对象两种存储方式
  - 批量操作支持

> 📖 详细的Redis序列化配置请参考：[Redis序列化修复文档](docs/redis-serialization-fix.md)  
> 📖 Hash操作优化和日志优化请参考：[Redis Hash优化文档](docs/redis-hash-optimization.md)

### 安全配置
- 密码加密：MD5 + 盐值
- Session安全：HttpOnly Cookie
- CORS：可根据需要配置

## 开发说明

### 添加新的API接口
1. 在对应的Controller中添加方法
2. 添加适当的权限注解（`@RequireAuth`、`@RequireRole`）
3. 使用 `UserContext.getCurrentUser()` 获取当前用户信息

### 自定义权限验证
可以在 `AuthAspect` 中扩展权限验证逻辑，或者在业务代码中使用 `UserContext` 进行权限判断。

### 短信服务集成
在 `SmsServiceImpl` 中的 `sendVerifyCode` 方法里集成真实的短信服务API（如阿里云、腾讯云等）。

## 部署说明

### 生产环境配置
1. 修改 `application-prod.yml` 配置文件
2. 设置 `knife4j.production=true` 关闭API文档
3. 配置 `server.servlet.session.cookie.secure=true` 启用HTTPS
4. 配置适当的日志级别

### Docker部署
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 常见问题

### Q: 登录后接口返回401未授权
A: 检查Session是否正确设置，Redis连接是否正常。

### Q: 验证码发送失败
A: 检查Redis连接，查看控制台日志中的验证码（开发环境）。

### Q: 权限验证不生效
A: 确保在Controller方法上添加了相应的权限注解。

## 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 Apache 2.0 许可证。详情请参见 [LICENSE](LICENSE) 文件。

## 联系方式

- 作者：Spark
- 邮箱：spark@example.com
- GitHub：[https://github.com/spark-demo](https://github.com/spark-demo) 

## 🧪 测试

### 单元测试覆盖

项目已为所有核心Service组件编写了完整的单元测试：

- **UserService**: 用户管理业务逻辑测试
  - 注册、登录、信息管理、权限验证
  - 参数验证、异常处理、边界条件
  
- **SmsService**: 短信验证码服务测试
  - 验证码发送、验证、删除
  - Redis操作、频率限制、异常处理
  
- **逻辑删除**: MyBatis-Plus逻辑删除功能测试
  - 自动填充、时间戳标记、查询过滤

### 运行测试

```bash
# 运行所有Service测试
mvn test -Dtest=*ServiceTest*

# 运行特定测试类
mvn test -Dtest=UserServiceTest
mvn test -Dtest=SmsServiceTest

# 运行逻辑删除测试
mvn test -Dtest=UserServiceLogicDeleteTest
```

### 测试配置

测试环境使用独立的配置文件 `application-test.yml`：
- H2内存数据库
- 关闭Redis Session存储
- 简化的日志配置

> 📖 详细的测试文档请参考：[Service测试文档](docs/service-unit-tests.md) 

> 📖 详细的登录方法说明请参考：[登录方法文档](docs/login-methods.md) 