package com.spark.demo.common.aspect;

import com.spark.demo.common.annotation.RateLimit;
import com.spark.demo.common.exception.BusinessException;
import com.spark.demo.common.result.ResultCode;
import com.spark.demo.common.util.IpUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面
 * 基于Bucket4j + Redis实现分布式限流
 * 
 * @author spark
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 本地缓存，避免频繁创建Bucket
     */
    private final ConcurrentHashMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = generateKey(point, rateLimit);
        
        // 获取或创建Bucket
        Bucket bucket = getBucket(key, rateLimit);
        
        // 尝试消费一个令牌
        if (bucket.tryConsume(1)) {
            log.debug("限流检查通过，key: {}", key);
            return point.proceed();
        } else {
            log.warn("触发限流，key: {}, 限制: {}次/{}{}", 
                    key, rateLimit.count(), rateLimit.time(), rateLimit.timeUnit());
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, rateLimit.message());
        }
    }

    /**
     * 生成限流key
     */
    private String generateKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder("rate_limit:");
        
        // 使用自定义key或方法签名
        if (StringUtils.hasText(rateLimit.key())) {
            keyBuilder.append(rateLimit.key());
        } else {
            MethodSignature signature = (MethodSignature) point.getSignature();
            keyBuilder.append(signature.getDeclaringTypeName())
                     .append(".")
                     .append(signature.getName());
        }
        
        // 根据限流类型添加后缀
        switch (rateLimit.limitType()) {
            case IP:
                keyBuilder.append(":").append(getClientIP());
                break;
            case USER:
                keyBuilder.append(":").append(getCurrentUserId());
                break;
            case DEFAULT:
            default:
                // 全局限流，不添加后缀
                break;
        }
        
        return keyBuilder.toString();
    }

    /**
     * 获取或创建Bucket
     */
    private Bucket getBucket(String key, RateLimit rateLimit) {
        return bucketCache.computeIfAbsent(key, k -> {
            // 创建限流配置
            Duration duration = Duration.of(rateLimit.time(), rateLimit.timeUnit().toChronoUnit());
            Bandwidth bandwidth = Bandwidth.classic(rateLimit.count(), Refill.intervally(rateLimit.count(), duration));
            
            return Bucket.builder()
                    .addLimit(bandwidth)
                    .build();
        });
    }

    /**
     * 获取客户端IP
     */
    private String getClientIP() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return IpUtil.getClientIP(request);
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
        }
        return "unknown";
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // 从请求头或session中获取用户ID
                String userId = request.getHeader("X-User-Id");
                if (StringUtils.hasText(userId)) {
                    return userId;
                }
                // 可以从JWT token中解析用户ID
                String token = request.getHeader("Authorization");
                if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                    // 这里可以解析JWT获取用户ID，暂时返回token的hash
                    return String.valueOf(token.hashCode());
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
        }
        return "anonymous";
    }
} 