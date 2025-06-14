package com.spark.demo.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.Map;

/**
 * Redis工具类
 * 提供常用的Redis操作方法，避免value乱码问题
 * 
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Component
public class RedisUtil {

    /**
     * 主要的RedisTemplate - 使用Jackson2JsonRedisSerializer（不含类型信息，避免乱码）
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 字符串专用RedisTemplate - 只处理字符串，完全避免序列化问题
     * 使用Spring Boot自动配置的StringRedisTemplate
     */
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    /**
     * 通用RedisTemplate - 包含类型信息，用于复杂对象（可能有乱码但保留类型）
     */
    @Autowired
    @Qualifier("genericRedisTemplate")
    private RedisTemplate<String, Object> genericRedisTemplate;

    // ==================== 字符串操作 ====================

    /**
     * 设置字符串值（推荐用于简单字符串）
     */
    public void setString(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置字符串值并指定过期时间
     */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取字符串值
     */
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // ==================== 对象操作（推荐，避免乱码） ====================

    /**
     * 设置对象值（使用Jackson序列化，不含类型信息）
     */
    public void setObject(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置对象值并指定过期时间
     */
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取对象值
     */
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取对象值并转换为指定类型
     * 注意：使用Jackson反序列化，可能需要默认构造函数
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        
        // 简单类型直接转换
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        
        // 复杂类型需要通过Jackson转换
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.convertValue(value, clazz);
        } catch (Exception e) {
            log.error("对象转换失败: key={}, targetClass={}", key, clazz.getName(), e);
            return null;
        }
    }

    // ==================== 复杂对象操作（包含类型信息） ====================

    /**
     * 设置复杂对象（保留类型信息，可能有乱码但能准确反序列化）
     */
    public void setGenericObject(String key, Object value) {
        genericRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置复杂对象并指定过期时间
     */
    public void setGenericObject(String key, Object value, long timeout, TimeUnit unit) {
        genericRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取复杂对象（自动反序列化为原始类型）
     */
    public Object getGenericObject(String key) {
        return genericRedisTemplate.opsForValue().get(key);
    }

    // ==================== 通用操作 ====================

    /**
     * 删除key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除key
     */
    public void delete(String... keys) {
        if (keys != null && keys.length > 0) {
            redisTemplate.delete(java.util.List.of(keys));
        }
    }

    /**
     * 检查key是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置key的过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 获取key的过期时间
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    // ==================== Hash操作 ====================

    /**
     * 向Hash中存放数据（使用主序列化器）
     */
    public void hashSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 向Hash中存放字符串数据（推荐，避免序列化乱码）
     */
    public void hashSetString(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 从Hash中获取数据
     */
    public Object hashGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 从Hash中获取字符串数据（推荐，避免序列化乱码）
     */
    public String hashGetString(String key, String hashKey) {
        return (String) stringRedisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 删除Hash中的数据
     */
    public void hashDelete(String key, String... hashKeys) {
        redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
    }

    /**
     * 检查Hash中是否存在指定的hashKey
     */
    public boolean hashHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 获取Hash中所有的field和value（字符串版本，避免乱码）
     */
    public Map<String, String> hashGetAllString(String key) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        Map<String, String> result = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String field = entry.getKey() != null ? entry.getKey().toString() : null;
            String value = entry.getValue() != null ? entry.getValue().toString() : null;
            if (field != null) {
                result.put(field, value);
            }
        }
        return result;
    }

    /**
     * 获取Hash中所有的field和value（对象版本）
     */
    public Map<Object, Object> hashGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 批量设置Hash字段（推荐，避免序列化乱码）
     */
    public void hashSetAllString(String key, Map<String, String> hash) {
        stringRedisTemplate.opsForHash().putAll(key, hash);
    }

    /**
     * 批量设置Hash字段（对象版本）
     */
    public void hashSetAll(String key, Map<String, Object> hash) {
        redisTemplate.opsForHash().putAll(key, hash);
    }

    // ==================== 使用建议方法 ====================

    /**
     * 推荐：存储验证码（纯字符串）
     */
    public void setVerifyCode(String phone, String code, int minutes) {
        setString("verify_code:" + phone, code, minutes, TimeUnit.MINUTES);
        log.debug("验证码已存储: phone={}, code={}, 过期时间={}分钟", phone, "******", minutes);
    }

    /**
     * 推荐：获取验证码
     */
    public String getVerifyCode(String phone) {
        return getString("verify_code:" + phone);
    }

    /**
     * 推荐：存储用户Session信息
     */
    public void setUserSession(String sessionId, Object userInfo, int minutes) {
        setObject("session:" + sessionId, userInfo, minutes, TimeUnit.MINUTES);
        log.debug("用户Session已存储: sessionId={}, 过期时间={}分钟", sessionId, minutes);
    }

    /**
     * 推荐：获取用户Session信息
     */
    public Object getUserSession(String sessionId) {
        return getObject("session:" + sessionId);
    }
} 