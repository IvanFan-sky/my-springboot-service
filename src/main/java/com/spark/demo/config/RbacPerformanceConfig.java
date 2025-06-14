package com.spark.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * RBAC性能优化配置
 * 
 * @author spark
 * @date 2025-01-01
 */
@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
@ConfigurationProperties(prefix = "rbac.performance")
public class RbacPerformanceConfig {

    /**
     * 缓存TTL（秒）
     */
    private int cacheTtl = 300;

    /**
     * 批量查询大小
     */
    private int batchSize = 100;

    /**
     * 异步线程池核心线程数
     */
    private int asyncCorePoolSize = 5;

    /**
     * 异步线程池最大线程数
     */
    private int asyncMaxPoolSize = 20;

    /**
     * 异步线程池队列容量
     */
    private int asyncQueueCapacity = 200;

    /**
     * 权限缓存管理器
     */
    @Bean("rbacCacheManager")
    @Profile("!redis")
    public CacheManager rbacCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "userRoles",
                "userPermissions", 
                "userMenus",
                "rolePermissions",
                "roleMenus",
                "permissionTree",
                "menuTree"
        ));
        return cacheManager;
    }

    /**
     * RBAC异步任务执行器
     */
    @Bean("rbacAsyncExecutor")
    public Executor rbacAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncCorePoolSize);
        executor.setMaxPoolSize(asyncMaxPoolSize);
        executor.setQueueCapacity(asyncQueueCapacity);
        executor.setThreadNamePrefix("rbac-async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 权限预热任务执行器
     */
    @Bean("rbacWarmupExecutor")
    public Executor rbacWarmupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("rbac-warmup-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }

    // Getters and Setters
    public int getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(int cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getAsyncCorePoolSize() {
        return asyncCorePoolSize;
    }

    public void setAsyncCorePoolSize(int asyncCorePoolSize) {
        this.asyncCorePoolSize = asyncCorePoolSize;
    }

    public int getAsyncMaxPoolSize() {
        return asyncMaxPoolSize;
    }

    public void setAsyncMaxPoolSize(int asyncMaxPoolSize) {
        this.asyncMaxPoolSize = asyncMaxPoolSize;
    }

    public int getAsyncQueueCapacity() {
        return asyncQueueCapacity;
    }

    public void setAsyncQueueCapacity(int asyncQueueCapacity) {
        this.asyncQueueCapacity = asyncQueueCapacity;
    }
} 