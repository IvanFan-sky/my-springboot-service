# 性能监控和缓存优化指南

本文档介绍了项目中实现的性能监控和缓存优化功能。

## 目录

- [缓存优化](#缓存优化)
- [性能监控](#性能监控)
- [监控端点](#监控端点)
- [配置说明](#配置说明)
- [使用示例](#使用示例)

## 缓存优化

### 缓存配置

项目使用Redis作为缓存存储，配置了多个缓存区域：

- **userCache**: 用户信息缓存，TTL 1小时
- **authCache**: 认证信息缓存，TTL 30分钟
- **captchaCache**: 验证码缓存，TTL 5分钟
- **sessionCache**: 会话缓存，TTL 2小时

### 缓存注解使用

#### @Cacheable - 缓存查询结果
```java
@Cacheable(value = "userCache", key = "#uuid", condition = "#result != null")
public UserDTO findByUuid(String uuid) {
    // 方法实现
}
```

#### @CacheEvict - 清除缓存
```java
@CacheEvict(value = "userCache", key = "#uuid")
public boolean updateUser(String uuid, UserDTO userDTO) {
    // 方法实现
}
```

#### @Caching - 组合缓存操作
```java
@Caching(evict = {
    @CacheEvict(value = "userCache", key = "#uuid"),
    @CacheEvict(value = "authCache", allEntries = true)
})
public boolean changePassword(String uuid, String oldPassword, String newPassword) {
    // 方法实现
}
```

### 缓存策略

1. **用户信息缓存**: 按UUID缓存用户基本信息
2. **认证缓存**: 缓存用户认证状态和权限信息
3. **验证码缓存**: 缓存短信验证码，防止重复发送
4. **会话缓存**: 缓存用户会话信息

## 性能监控

### 监控组件

#### 1. Spring Boot Actuator
提供应用健康检查、指标收集等功能。

#### 2. Micrometer + Prometheus
收集应用性能指标，支持Prometheus格式导出。

#### 3. 自定义监控切面
- **PerformanceMonitoringAspect**: 监控Service和Controller方法执行时间
- **缓存操作监控**: 监控缓存操作的性能和成功率

### 监控指标

#### 方法执行指标
- `method.calls`: 方法调用总数
- `method.calls.success`: 成功调用数
- `method.calls.error`: 错误调用数
- `method.execution.time`: 方法执行时间

#### API调用指标
- `api.calls`: API调用总数
- `api.calls.success`: 成功API调用数
- `api.calls.error`: 错误API调用数
- `api.execution.time`: API响应时间

#### 缓存操作指标
- `cache.operations`: 缓存操作总数
- `cache.operations.success`: 成功缓存操作数
- `cache.operations.error`: 错误缓存操作数
- `cache.operation.time`: 缓存操作时间

### @Timed注解使用

在关键方法上添加@Timed注解来监控性能：

```java
@Timed(value = "user.service.login", description = "用户登录")
public UserDTO login(String username, String password, String clientIp) {
    // 方法实现
}
```

## 监控端点

### 标准Actuator端点

- `/actuator/health` - 应用健康状态
- `/actuator/metrics` - 应用指标
- `/actuator/prometheus` - Prometheus格式指标
- `/actuator/info` - 应用信息
- `/actuator/env` - 环境变量

### 自定义监控端点

#### `/actuator/performance` - 性能指标总览

返回应用的综合性能指标：

```json
{
  "application": {
    "uptime": 1643723400000,
    "processors": 8,
    "memory": {
      "free": "512.00 MB",
      "total": "1.00 GB",
      "max": "4.00 GB",
      "used": "512.00 MB"
    }
  },
  "methods": {
    "totalCalls": 1250,
    "successCalls": 1200,
    "errorCalls": 50,
    "successRate": "96.00%",
    "averageExecutionTime": "45.30ms",
    "maxExecutionTime": "1200.50ms"
  },
  "apis": {
    "totalCalls": 800,
    "successCalls": 780,
    "errorCalls": 20,
    "successRate": "97.50%",
    "averageResponseTime": "120.80ms",
    "maxResponseTime": "2500.00ms"
  },
  "cache": {
    "totalOperations": 500,
    "successOperations": 495,
    "errorOperations": 5,
    "successRate": "99.00%",
    "averageOperationTime": "5.20ms"
  }
}
```

## 配置说明

### 环境变量配置

在`.env`文件中配置以下变量：

```bash
# 缓存配置
CACHE_TTL=3600
CACHE_NULL_VALUES=false
CACHE_KEY_PREFIX=app:cache:
CACHE_USE_KEY_PREFIX=true

# 监控配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus,performance
MANAGEMENT_ENDPOINTS_WEB_BASE_PATH=/actuator
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
```

### 应用配置文件

#### application.yml
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: ${CACHE_TTL:3600}s
      cache-null-values: ${CACHE_NULL_VALUES:false}
      key-prefix: ${CACHE_KEY_PREFIX:app:cache:}
      use-key-prefix: ${CACHE_USE_KEY_PREFIX:true}

management:
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,metrics,prometheus}
      base-path: ${MANAGEMENT_ENDPOINTS_WEB_BASE_PATH:/actuator}
  endpoint:
    health:
      show-details: ${MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS:when_authorized}
  metrics:
    export:
      prometheus:
        enabled: ${MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED:true}
```

## 使用示例

### 1. 查看应用健康状态

```bash
curl http://localhost:8080/actuator/health
```

### 2. 获取性能指标

```bash
curl http://localhost:8080/actuator/performance
```

### 3. 获取Prometheus指标

```bash
curl http://localhost:8080/actuator/prometheus
```

### 4. 查看特定指标

```bash
# 查看方法调用指标
curl http://localhost:8080/actuator/metrics/method.calls

# 查看API响应时间
curl http://localhost:8080/actuator/metrics/api.execution.time

# 查看缓存操作指标
curl http://localhost:8080/actuator/metrics/cache.operations
```

## 性能优化建议

### 1. 缓存策略优化

- 根据业务场景调整缓存TTL
- 合理使用缓存条件，避免缓存无效数据
- 及时清理相关缓存，保证数据一致性

### 2. 监控告警

- 设置方法执行时间阈值告警（如超过1秒）
- 监控API响应时间，设置SLA告警
- 监控缓存命中率和错误率
- 设置系统资源使用率告警

### 3. 性能调优

- 根据监控数据识别性能瓶颈
- 优化慢查询和慢方法
- 调整缓存策略和Redis配置
- 优化数据库连接池配置

## 故障排查

### 1. 缓存问题

- 检查Redis连接状态
- 查看缓存命中率和错误日志
- 验证缓存键的正确性

### 2. 性能问题

- 查看慢方法和慢API日志
- 分析方法执行时间分布
- 检查系统资源使用情况

### 3. 监控问题

- 检查Actuator端点是否正常
- 验证Micrometer指标收集
- 查看监控切面是否生效

## 扩展功能

### 1. 集成Grafana

可以将Prometheus指标导入Grafana进行可视化展示。

### 2. 添加自定义指标

```java
@Component
public class CustomMetrics {
    
    private final Counter customCounter;
    private final Timer customTimer;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.customCounter = Counter.builder("custom.operations")
                .description("自定义操作计数")
                .register(meterRegistry);
        this.customTimer = Timer.builder("custom.execution.time")
                .description("自定义操作执行时间")
                .register(meterRegistry);
    }
    
    public void recordCustomOperation() {
        Timer.Sample sample = Timer.start();
        try {
            // 业务逻辑
            customCounter.increment();
        } finally {
            sample.stop(customTimer);
        }
    }
}
```

### 3. 分布式追踪

可以集成Spring Cloud Sleuth进行分布式请求追踪。

---

更多详细信息请参考Spring Boot Actuator和Micrometer官方文档。