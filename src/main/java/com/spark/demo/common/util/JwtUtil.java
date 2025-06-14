package com.spark.demo.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 * 提供JWT token的生成、解析、验证功能
 * 
 * @author spark
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥
     */
    @Value("${app.jwt.secret:mySecretKeyForJWTTokenGenerationThatShouldBeLongEnough}")
    private String secret;

    /**
     * JWT过期时间（小时）
     */
    @Value("${app.jwt.expiration:24}")
    private int expiration;

    /**
     * 刷新token过期时间（天）
     */
    @Value("${app.jwt.refresh-expiration:7}")
    private int refreshExpiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成访问token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param additionalClaims 额外声明
     * @return JWT token
     */
    public String generateAccessToken(Long userId, String username, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofHours(expiration));

        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey());

        // 添加额外声明
        if (additionalClaims != null && !additionalClaims.isEmpty()) {
            additionalClaims.forEach(builder::claim);
        }

        return builder.compact();
    }

    /**
     * 生成刷新token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return 刷新token
     */
    public String generateRefreshToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofDays(refreshExpiration));

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析token
     *
     * @param token JWT token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token已过期: {}", e.getMessage());
            throw new RuntimeException("Token已过期");
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT token: {}", e.getMessage());
            throw new RuntimeException("不支持的Token格式");
        } catch (MalformedJwtException e) {
            log.warn("JWT token格式错误: {}", e.getMessage());
            throw new RuntimeException("Token格式错误");
        } catch (SecurityException e) {
            log.warn("JWT token签名验证失败: {}", e.getMessage());
            throw new RuntimeException("Token签名验证失败");
        } catch (IllegalArgumentException e) {
            log.warn("JWT token参数错误: {}", e.getMessage());
            throw new RuntimeException("Token参数错误");
        }
    }

    /**
     * 验证token是否有效
     *
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取用户名
     *
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从token中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从token中获取token类型
     *
     * @param token JWT token
     * @return token类型
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    /**
     * 检查token是否即将过期（1小时内）
     *
     * @param token JWT token
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long timeUntilExpiry = expiration.getTime() - now.getTime();
            return timeUntilExpiry < Duration.ofHours(1).toMillis();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取token剩余有效时间（秒）
     *
     * @param token JWT token
     * @return 剩余有效时间（秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            return Math.max(0, (expiration.getTime() - now.getTime()) / 1000);
        } catch (Exception e) {
            return 0;
        }
    }
} 