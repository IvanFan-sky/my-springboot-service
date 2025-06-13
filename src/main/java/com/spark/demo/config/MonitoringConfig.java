package com.spark.demo.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

/**
 * 监控配置类
 * 提供自定义健康检查和性能指标
 * 
 * @author spark
 * @date 2025-01-27
 */
@Slf4j
@Configuration
public class MonitoringConfig {

    @Value("${app.name:SpringBoot通用后台服务}")
    private String appName;

    /**
     * 启用@Timed注解支持
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * 自定义数据库健康检查
     */
    @Bean
    public HealthIndicator dbHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(3)) {
                    return Health.up()
                            .withDetail("database", "MySQL")
                            .withDetail("status", "连接正常")
                            .build();
                } else {
                    return Health.down()
                            .withDetail("database", "MySQL")
                            .withDetail("status", "连接无效")
                            .build();
                }
            } catch (Exception e) {
                log.error("数据库健康检查失败", e);
                return Health.down()
                        .withDetail("database", "MySQL")
                        .withDetail("status", "连接失败")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 自定义Redis健康检查
     */
    @Bean
    public HealthIndicator redisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        return () -> {
            try {
                String pong = redisTemplate.getConnectionFactory().getConnection().ping();
                if ("PONG".equals(pong)) {
                    return Health.up()
                            .withDetail("redis", "连接正常")
                            .withDetail("response", pong)
                            .build();
                } else {
                    return Health.down()
                            .withDetail("redis", "响应异常")
                            .withDetail("response", pong)
                            .build();
                }
            } catch (Exception e) {
                log.error("Redis健康检查失败", e);
                return Health.down()
                        .withDetail("redis", "连接失败")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 自定义应用健康检查
     */
    @Bean
    public HealthIndicator appHealthIndicator() {
        return () -> {
            // 检查应用关键组件状态
            long freeMemory = Runtime.getRuntime().freeMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            long maxMemory = Runtime.getRuntime().maxMemory();
            
            double memoryUsage = (double) (totalMemory - freeMemory) / maxMemory;
            
            Health.Builder builder = Health.up()
                    .withDetail("app", appName)
                    .withDetail("memory.free", formatBytes(freeMemory))
                    .withDetail("memory.total", formatBytes(totalMemory))
                    .withDetail("memory.max", formatBytes(maxMemory))
                    .withDetail("memory.usage", String.format("%.2f%%", memoryUsage * 100));
            
            // 内存使用率超过90%时标记为DOWN
            if (memoryUsage > 0.9) {
                builder = Health.down()
                        .withDetail("reason", "内存使用率过高")
                        .withDetail("memory.usage", String.format("%.2f%%", memoryUsage * 100));
            }
            
            return builder.build();
        };
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