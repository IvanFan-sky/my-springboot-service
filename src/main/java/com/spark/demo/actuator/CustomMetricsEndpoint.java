package com.spark.demo.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 自定义性能监控端点
 * 提供应用特定的性能指标和统计信息
 * 
 * @author spark
 * @date 2025-01-27
 */
@Slf4j
@Component
@Endpoint(id = "performance")
public class CustomMetricsEndpoint {

    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取性能指标
     */
    @ReadOperation
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 应用基本信息
            metrics.put("application", getApplicationMetrics());
            
            // 方法执行统计
            metrics.put("methods", getMethodMetrics());
            
            // API调用统计
            metrics.put("apis", getApiMetrics());
            
            // 缓存操作统计
            metrics.put("cache", getCacheMetrics());
            
            // 数据库连接统计
            metrics.put("database", getDatabaseMetrics());
            
            // Redis连接统计
            metrics.put("redis", getRedisMetrics());
            
            // 系统资源统计
            metrics.put("system", getSystemMetrics());
            
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * 获取应用基本指标
     */
    private Map<String, Object> getApplicationMetrics() {
        Map<String, Object> app = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        app.put("uptime", System.currentTimeMillis());
        app.put("processors", runtime.availableProcessors());
        app.put("memory", Map.of(
                "free", formatBytes(runtime.freeMemory()),
                "total", formatBytes(runtime.totalMemory()),
                "max", formatBytes(runtime.maxMemory()),
                "used", formatBytes(runtime.totalMemory() - runtime.freeMemory())
        ));
        
        return app;
    }

    /**
     * 获取方法执行指标
     */
    private Map<String, Object> getMethodMetrics() {
        Map<String, Object> methods = new HashMap<>();
        
        try {
            // 获取方法调用总数
            double totalCalls = meterRegistry.get("method.calls").counter().count();
            methods.put("totalCalls", totalCalls);
            
            // 获取成功调用数
            double successCalls = meterRegistry.get("method.calls.success").counter().count();
            methods.put("successCalls", successCalls);
            
            // 获取错误调用数
            double errorCalls = meterRegistry.get("method.calls.error").counter().count();
            methods.put("errorCalls", errorCalls);
            
            // 计算成功率
            if (totalCalls > 0) {
                methods.put("successRate", String.format("%.2f%%", (successCalls / totalCalls) * 100));
            }
            
            // 获取平均执行时间
            Timer executionTimer = meterRegistry.get("method.execution.time").timer();
            methods.put("averageExecutionTime", String.format("%.2fms", executionTimer.mean(TimeUnit.MILLISECONDS)));
            methods.put("maxExecutionTime", String.format("%.2fms", executionTimer.max(TimeUnit.MILLISECONDS)));
            
        } catch (Exception e) {
            methods.put("error", "指标收集失败: " + e.getMessage());
        }
        
        return methods;
    }

    /**
     * 获取API调用指标
     */
    private Map<String, Object> getApiMetrics() {
        Map<String, Object> apis = new HashMap<>();
        
        try {
            // 获取API调用总数
            double totalApiCalls = meterRegistry.get("api.calls").counter().count();
            apis.put("totalCalls", totalApiCalls);
            
            // 获取成功API调用数
            double successApiCalls = meterRegistry.get("api.calls.success").counter().count();
            apis.put("successCalls", successApiCalls);
            
            // 获取错误API调用数
            double errorApiCalls = meterRegistry.get("api.calls.error").counter().count();
            apis.put("errorCalls", errorApiCalls);
            
            // 计算API成功率
            if (totalApiCalls > 0) {
                apis.put("successRate", String.format("%.2f%%", (successApiCalls / totalApiCalls) * 100));
            }
            
            // 获取API平均响应时间
            Timer apiTimer = meterRegistry.get("api.execution.time").timer();
            apis.put("averageResponseTime", String.format("%.2fms", apiTimer.mean(TimeUnit.MILLISECONDS)));
            apis.put("maxResponseTime", String.format("%.2fms", apiTimer.max(TimeUnit.MILLISECONDS)));
            
        } catch (Exception e) {
            apis.put("error", "API指标收集失败: " + e.getMessage());
        }
        
        return apis;
    }

    /**
     * 获取缓存操作指标
     */
    private Map<String, Object> getCacheMetrics() {
        Map<String, Object> cache = new HashMap<>();
        
        try {
            // 获取缓存操作总数
            double totalCacheOps = meterRegistry.get("cache.operations").counter().count();
            cache.put("totalOperations", totalCacheOps);
            
            // 获取成功缓存操作数
            double successCacheOps = meterRegistry.get("cache.operations.success").counter().count();
            cache.put("successOperations", successCacheOps);
            
            // 获取错误缓存操作数
            double errorCacheOps = meterRegistry.get("cache.operations.error").counter().count();
            cache.put("errorOperations", errorCacheOps);
            
            // 计算缓存操作成功率
            if (totalCacheOps > 0) {
                cache.put("successRate", String.format("%.2f%%", (successCacheOps / totalCacheOps) * 100));
            }
            
            // 获取缓存操作平均时间
            Timer cacheTimer = meterRegistry.get("cache.operation.time").timer();
            cache.put("averageOperationTime", String.format("%.2fms", cacheTimer.mean(TimeUnit.MILLISECONDS)));
            
        } catch (Exception e) {
            cache.put("error", "缓存指标收集失败: " + e.getMessage());
        }
        
        return cache;
    }

    /**
     * 获取数据库连接指标
     */
    private Map<String, Object> getDatabaseMetrics() {
        Map<String, Object> db = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            long startTime = System.currentTimeMillis();
            boolean isValid = connection.isValid(3);
            long connectionTime = System.currentTimeMillis() - startTime;
            
            db.put("connectionValid", isValid);
            db.put("connectionTime", connectionTime + "ms");
            db.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
            db.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            
        } catch (Exception e) {
            db.put("error", "数据库连接检查失败: " + e.getMessage());
        }
        
        return db;
    }

    /**
     * 获取Redis连接指标
     */
    private Map<String, Object> getRedisMetrics() {
        Map<String, Object> redis = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            long pingTime = System.currentTimeMillis() - startTime;
            
            redis.put("ping", pong);
            redis.put("pingTime", pingTime + "ms");
            redis.put("connectionValid", "PONG".equals(pong));
            
        } catch (Exception e) {
            redis.put("error", "Redis连接检查失败: " + e.getMessage());
        }
        
        return redis;
    }

    /**
     * 获取系统资源指标
     */
    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> system = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        
        // CPU核心数
        system.put("availableProcessors", runtime.availableProcessors());
        
        // 内存使用情况
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;
        
        system.put("memory", Map.of(
                "total", formatBytes(totalMemory),
                "free", formatBytes(freeMemory),
                "used", formatBytes(usedMemory),
                "max", formatBytes(maxMemory),
                "usagePercent", String.format("%.2f%%", (double) usedMemory / maxMemory * 100)
        ));
        
        // 系统时间
        system.put("currentTime", System.currentTimeMillis());
        system.put("systemTime", new java.util.Date().toString());
        
        return system;
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}