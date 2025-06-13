package com.spark.demo.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
public class RedisConfig implements CachingConfigurer {

    @Value("${spring.cache.redis.time-to-live:PT30M}")
    private Duration defaultCacheTtl;

    @Value("${spring.cache.redis.cache-null-values:false}")
    private boolean cacheNullValues;

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
        
        // 预热连接池
        try {
            template.opsForValue().get("__connection_test__");
            log.info("✅ Redis连接池预热完成");
        } catch (Exception e) {
            log.warn("Redis连接池预热失败，但不影响正常使用: {}", e.getMessage());
        }
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
        
        // 监控连接状态
        try {
            template.opsForValue().get("__generic_connection_test__");
            log.info("✅ 通用Redis模板连接测试成功");
        } catch (Exception e) {
            log.warn("通用Redis模板连接测试失败: {}", e.getMessage());
        }
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
    @Override
    public CacheManager cacheManager() {
        return cacheManager(null);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultCacheTtl) // 使用配置的默认TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        createCleanJsonSerializer()))
                .prefixCacheNameWith("cache:") // 添加缓存前缀
                .computePrefixWith(cacheName -> "app:cache:" + cacheName + ":"); // 自定义前缀计算

        if (!cacheNullValues) {
            defaultConfig = defaultConfig.disableCachingNullValues();
        }

        // 针对不同缓存名称的特定配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户缓存 - 较长TTL
        cacheConfigurations.put("userCache", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // 短期缓存 - 较短TTL
        cacheConfigurations.put("shortCache", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 验证码缓存 - 很短TTL
        cacheConfigurations.put("captchaCache", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        
        // 权限缓存 - 中等TTL
        cacheConfigurations.put("authCache", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 支持事务
                .build();
        
        log.info("✅ Redis缓存管理器配置完成 - 使用JSON序列化，默认TTL: {}", defaultCacheTtl);
        return cacheManager;
    }

    /**
     * 自定义缓存键生成器
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getSimpleName()).append(":");
                sb.append(method.getName()).append(":");
                for (Object param : params) {
                    if (param != null) {
                        sb.append(param.toString()).append(":");
                    } else {
                        sb.append("null:");
                    }
                }
                // 移除最后一个冒号
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ':') {
                    sb.setLength(sb.length() - 1);
                }
                return sb.toString();
            }
        };
    }

    /**
     * 缓存错误处理器
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.error("缓存获取异常 - Cache: {}, Key: {}", cache.getName(), key, exception);
                super.handleCacheGetError(exception, cache, key);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                log.error("缓存存储异常 - Cache: {}, Key: {}", cache.getName(), key, exception);
                super.handleCachePutError(exception, cache, key, value);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.error("缓存清除异常 - Cache: {}, Key: {}", cache.getName(), key, exception);
                super.handleCacheEvictError(exception, cache, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                log.error("缓存清空异常 - Cache: {}", cache.getName(), exception);
                super.handleCacheClearError(exception, cache);
            }
        };
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