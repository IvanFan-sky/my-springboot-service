package com.spark.demo.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存防护工具类
 * 防止缓存穿透、击穿、雪崩
 * 
 * @author spark
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String NULL_VALUE = "NULL";
    private static final String LOCK_PREFIX = "lock:";
    private static final Random RANDOM = new Random();

    /**
     * 防穿透查询
     * 如果缓存和数据库都没有数据，会缓存空值防止穿透
     * 
     * @param key 缓存key
     * @param dataLoader 数据加载器
     * @param timeout 缓存超时时间
     * @param unit 时间单位
     * @param nullTimeout 空值缓存超时时间（秒）
     * @return 查询结果
     */
    public <T> T getWithPassThrough(String key, Supplier<T> dataLoader, 
                                   long timeout, TimeUnit unit, long nullTimeout) {
        // 1. 从缓存查询
        Object cached = redisTemplate.opsForValue().get(key);
        
        // 2. 缓存命中
        if (cached != null) {
            // 如果是空值标记，返回null
            if (NULL_VALUE.equals(cached)) {
                log.debug("缓存命中空值，key: {}", key);
                return null;
            }
            log.debug("缓存命中，key: {}", key);
            return (T) cached;
        }
        
        // 3. 缓存未命中，查询数据库
        log.debug("缓存未命中，查询数据库，key: {}", key);
        T data = dataLoader.get();
        
        // 4. 数据存在，写入缓存
        if (data != null) {
            // 添加随机过期时间，防止雪崩
            long randomTimeout = timeout + RANDOM.nextInt(300); // 随机增加0-5分钟
            redisTemplate.opsForValue().set(key, data, randomTimeout, unit);
            log.debug("数据写入缓存，key: {}, timeout: {}", key, randomTimeout);
        } else {
            // 5. 数据不存在，缓存空值防止穿透
            redisTemplate.opsForValue().set(key, NULL_VALUE, nullTimeout, TimeUnit.SECONDS);
            log.debug("缓存空值防穿透，key: {}, timeout: {}s", key, nullTimeout);
        }
        
        return data;
    }

    /**
     * 防击穿查询（互斥锁）
     * 使用分布式锁防止缓存击穿
     * 
     * @param key 缓存key
     * @param dataLoader 数据加载器
     * @param timeout 缓存超时时间
     * @param unit 时间单位
     * @param lockTimeout 锁超时时间（秒）
     * @return 查询结果
     */
    public <T> T getWithMutex(String key, Supplier<T> dataLoader, 
                             long timeout, TimeUnit unit, long lockTimeout) {
        // 1. 从缓存查询
        Object cached = redisTemplate.opsForValue().get(key);
        
        // 2. 缓存命中
        if (cached != null && !NULL_VALUE.equals(cached)) {
            log.debug("缓存命中，key: {}", key);
            return (T) cached;
        }
        
        // 3. 缓存未命中，尝试获取锁
        String lockKey = LOCK_PREFIX + key;
        boolean lockAcquired = tryLock(lockKey, lockTimeout);
        
        try {
            if (lockAcquired) {
                // 4. 获取锁成功，再次检查缓存（双重检查）
                cached = redisTemplate.opsForValue().get(key);
                if (cached != null && !NULL_VALUE.equals(cached)) {
                    log.debug("双重检查缓存命中，key: {}", key);
                    return (T) cached;
                }
                
                // 5. 查询数据库
                log.debug("获取锁成功，查询数据库，key: {}", key);
                T data = dataLoader.get();
                
                // 6. 写入缓存
                if (data != null) {
                    long randomTimeout = timeout + RANDOM.nextInt(300);
                    redisTemplate.opsForValue().set(key, data, randomTimeout, unit);
                    log.debug("数据写入缓存，key: {}", key);
                } else {
                    redisTemplate.opsForValue().set(key, NULL_VALUE, 60, TimeUnit.SECONDS);
                    log.debug("缓存空值，key: {}", key);
                }
                
                return data;
            } else {
                // 7. 获取锁失败，等待后重试
                log.debug("获取锁失败，等待重试，key: {}", key);
                try {
                    Thread.sleep(50); // 等待50ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // 重试查询缓存
                cached = redisTemplate.opsForValue().get(key);
                if (cached != null && !NULL_VALUE.equals(cached)) {
                    return (T) cached;
                }
                
                // 如果还是没有，直接查询数据库（降级策略）
                log.warn("缓存重建超时，直接查询数据库，key: {}", key);
                return dataLoader.get();
            }
        } finally {
            if (lockAcquired) {
                releaseLock(lockKey);
            }
        }
    }

    /**
     * 防雪崩批量预热
     * 
     * @param keys 缓存key列表
     * @param dataLoader 数据加载器
     * @param baseTimeout 基础超时时间
     * @param unit 时间单位
     */
    public <T> void warmUp(String[] keys, java.util.function.Function<String, T> dataLoader, 
                          long baseTimeout, TimeUnit unit) {
        for (String key : keys) {
            try {
                T data = dataLoader.apply(key);
                if (data != null) {
                    // 为每个key添加不同的随机过期时间
                    long randomTimeout = baseTimeout + RANDOM.nextInt(600); // 随机增加0-10分钟
                    redisTemplate.opsForValue().set(key, data, randomTimeout, unit);
                    log.debug("预热缓存，key: {}, timeout: {}", key, randomTimeout);
                }
            } catch (Exception e) {
                log.error("预热缓存失败，key: {}", key, e);
            }
        }
    }

    /**
     * 尝试获取分布式锁
     * 
     * @param lockKey 锁key
     * @param timeout 超时时间（秒）
     * @return 是否获取成功
     */
    private boolean tryLock(String lockKey, long timeout) {
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁key
     */
    private void releaseLock(String lockKey) {
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("释放锁失败，key: {}", lockKey, e);
        }
    }

    /**
     * 删除缓存
     * 
     * @param key 缓存key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("删除缓存，key: {}", key);
    }

    /**
     * 批量删除缓存
     * 
     * @param pattern 匹配模式
     */
    public void deleteByPattern(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量删除缓存，pattern: {}, count: {}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败，pattern: {}", pattern, e);
        }
    }

    /**
     * 设置缓存过期时间
     * 
     * @param key 缓存key
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, Duration.of(timeout, unit.toChronoUnit()));
    }

    /**
     * 检查key是否存在
     * 
     * @param key 缓存key
     * @return 是否存在
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
} 