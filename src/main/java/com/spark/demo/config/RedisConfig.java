package com.spark.demo.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.time.Duration;

/**
 * Redis 统一配置类
 * 
 * 功能说明：
 * 1. 主RedisTemplate - 使用Jackson2JsonRedisSerializer（避免乱码，推荐日常使用）
 * 2. 通用RedisTemplate - 使用GenericJackson2JsonRedisSerializer（保留类型信息）
 * 3. Spring Session - 使用JSON序列化，解决Hash value乱码问题
 * 4. 缓存管理器 - 统一序列化配置
 * 5. RedisUtil工具类支持
 * 
 * 基于Redis官方文档最佳实践：
 * - Hash操作优先使用字符串方式避免乱码
 * - Session数据使用JSON格式便于调试
 * - 支持多种序列化方式满足不同需求
 * 
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Configuration
@EnableCaching // 启用缓存
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 1800, // Session超时30分钟
    redisNamespace = "spring:session"     // Session命名空间
)
public class RedisConfig {

    /**
     * 主要的RedisTemplate配置 - 使用Jackson2JsonRedisSerializer（不包含类型信息，避免乱码）
     * 推荐日常使用，在Redis客户端中显示清晰可读的JSON格式
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Key序列化器：使用StringRedisSerializer
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // Value序列化器：使用Jackson2JsonRedisSerializer（不含类型信息，避免乱码）
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createCleanJsonSerializer();
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 设置默认序列化器
        template.setDefaultSerializer(jackson2JsonRedisSerializer);
        template.setEnableDefaultSerializer(true);

        template.afterPropertiesSet();
        
        log.info("✅ 主Redis模板配置完成 - 使用清洁JSON序列化器（避免乱码）");
        return template;
    }

    /**
     * 包含类型信息的RedisTemplate配置（用于复杂对象存储）
     * 适用于需要精确反序列化对象类型的场景
     */
    @Bean(name = "genericRedisTemplate")
    public RedisTemplate<String, Object> genericRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 使用GenericJackson2JsonRedisSerializer保留类型信息
        GenericJackson2JsonRedisSerializer genericSerializer = new GenericJackson2JsonRedisSerializer(
            createObjectMapperWithTypeInfo()
        );
        
        template.setValueSerializer(genericSerializer);
        template.setHashValueSerializer(genericSerializer);

        template.afterPropertiesSet();
        
        log.info("✅ 通用Redis模板配置完成 - 包含类型信息（复杂对象专用）");
        return template;
    }

    /**
     * Spring Session专用RedisTemplate配置
     * 关键：解决Session在Redis Hash中value值乱码问题
     * 
     * Spring Session会自动使用这个模板来存储session数据
     */
    @Bean("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // 使用包含类型信息的JSON序列化器，确保Session对象能正确反序列化
        ObjectMapper sessionObjectMapper = createSessionObjectMapper();
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(sessionObjectMapper, Object.class);
        
        log.info("✅ Spring Session序列化器配置完成 - JSON格式，解决Hash乱码问题");
        return serializer;
    }
    
    /**
     * 配置Redis缓存管理器
     * 使用清洁的JSON序列化，缓存数据可读性好
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 配置缓存策略
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认缓存30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        createCleanJsonSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
        
        log.info("✅ Redis缓存管理器配置完成 - 使用JSON序列化");
        return cacheManager;
    }
    
    /**
     * 创建清洁的Jackson2JsonRedisSerializer（不含类型信息，避免乱码）
     * 适用于：日常缓存、简单对象存储、要求Redis数据可读的场景
     */
    private Jackson2JsonRedisSerializer<Object> createCleanJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 🔑 关键：不使用activateDefaultTyping，避免@class字段导致的乱码
        
        // 注册JavaTimeModule以支持Java 8时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 其他配置
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        
        log.debug("创建清洁JSON序列化器完成");
        return serializer;
    }
    
    /**
     * 创建包含类型信息的ObjectMapper实例（用于复杂对象存储）
     * 适用于：需要精确类型信息的复杂对象
     */
    private ObjectMapper createObjectMapperWithTypeInfo() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 启用类型信息以支持复杂对象反序列化
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance, 
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // 注册JavaTimeModule以支持Java 8时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 其他配置
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        log.debug("创建带类型信息的ObjectMapper完成");
        return objectMapper;
    }

    /**
     * 创建Session专用的ObjectMapper
     * 平衡可读性和功能性：既要支持Session对象的完整序列化，又要在Redis中相对可读
     */
    private ObjectMapper createSessionObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // Session需要类型信息以正确反序列化各种session属性
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // 注册时间模块
        objectMapper.registerModule(new JavaTimeModule());
        
        // 配置
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT, false); // 紧凑输出
        
        log.debug("创建Session专用ObjectMapper完成");
        return objectMapper;
    }
}