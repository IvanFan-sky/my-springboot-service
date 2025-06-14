package com.spark.demo.common.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务监控指标工具类
 * 提供常用的业务指标统计功能
 * 
 * @author spark
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsUtil {

    private final MeterRegistry meterRegistry;
    
    // 缓存计数器和计时器，避免重复创建
    private final ConcurrentHashMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    /**
     * 增加计数器
     * 
     * @param name 指标名称
     * @param tags 标签（key-value对）
     */
    public void incrementCounter(String name, String... tags) {
        try {
            String key = buildKey(name, tags);
            Counter counter = counterCache.computeIfAbsent(key, k -> 
                Counter.builder(name)
                       .tags(tags)
                       .description("Business counter for " + name)
                       .register(meterRegistry)
            );
            counter.increment();
        } catch (Exception e) {
            log.error("增加计数器失败: {}", name, e);
        }
    }

    /**
     * 增加计数器（指定增量）
     * 
     * @param name 指标名称
     * @param amount 增量
     * @param tags 标签
     */
    public void incrementCounter(String name, double amount, String... tags) {
        try {
            String key = buildKey(name, tags);
            Counter counter = counterCache.computeIfAbsent(key, k -> 
                Counter.builder(name)
                       .tags(tags)
                       .description("Business counter for " + name)
                       .register(meterRegistry)
            );
            counter.increment(amount);
        } catch (Exception e) {
            log.error("增加计数器失败: {}, amount: {}", name, amount, e);
        }
    }

    /**
     * 记录执行时间
     * 
     * @param name 指标名称
     * @param duration 执行时间
     * @param tags 标签
     */
    public void recordTime(String name, Duration duration, String... tags) {
        try {
            String key = buildKey(name, tags);
            Timer timer = timerCache.computeIfAbsent(key, k -> 
                Timer.builder(name)
                     .tags(tags)
                     .description("Business timer for " + name)
                     .register(meterRegistry)
            );
            timer.record(duration);
        } catch (Exception e) {
            log.error("记录执行时间失败: {}, duration: {}", name, duration, e);
        }
    }

    /**
     * 记录执行时间（毫秒）
     * 
     * @param name 指标名称
     * @param milliseconds 执行时间（毫秒）
     * @param tags 标签
     */
    public void recordTime(String name, long milliseconds, String... tags) {
        recordTime(name, Duration.ofMillis(milliseconds), tags);
    }

    /**
     * 获取计时器样本
     * 
     * @param name 指标名称
     * @param tags 标签
     * @return 计时器样本
     */
    public Timer.Sample startTimer(String name, String... tags) {
        try {
            return Timer.start(meterRegistry);
        } catch (Exception e) {
            log.error("启动计时器失败: {}", name, e);
            return null;
        }
    }

    /**
     * 停止计时器并记录
     * 
     * @param sample 计时器样本
     * @param name 指标名称
     * @param tags 标签
     */
    public void stopTimer(Timer.Sample sample, String name, String... tags) {
        if (sample == null) {
            return;
        }
        
        try {
            String key = buildKey(name, tags);
            Timer timer = timerCache.computeIfAbsent(key, k -> 
                Timer.builder(name)
                     .tags(tags)
                     .description("Business timer for " + name)
                     .register(meterRegistry)
            );
            sample.stop(timer);
        } catch (Exception e) {
            log.error("停止计时器失败: {}", name, e);
        }
    }

    /**
     * 记录用户操作
     * 
     * @param operation 操作类型
     * @param userId 用户ID
     * @param result 操作结果（success/failure）
     */
    public void recordUserOperation(String operation, String userId, String result) {
        incrementCounter("user.operation", 
                        "operation", operation,
                        "user_id", userId,
                        "result", result);
    }

    /**
     * 记录API调用
     * 
     * @param method HTTP方法
     * @param uri 请求URI
     * @param status HTTP状态码
     * @param duration 执行时间
     */
    public void recordApiCall(String method, String uri, int status, Duration duration) {
        // 记录API调用次数
        incrementCounter("api.requests", 
                        "method", method,
                        "uri", uri,
                        "status", String.valueOf(status));
        
        // 记录API执行时间
        recordTime("api.duration", duration,
                  "method", method,
                  "uri", uri,
                  "status", String.valueOf(status));
    }

    /**
     * 记录数据库操作
     * 
     * @param operation 操作类型（select/insert/update/delete）
     * @param table 表名
     * @param duration 执行时间
     */
    public void recordDatabaseOperation(String operation, String table, Duration duration) {
        incrementCounter("database.operations",
                        "operation", operation,
                        "table", table);
        
        recordTime("database.duration", duration,
                  "operation", operation,
                  "table", table);
    }

    /**
     * 记录缓存操作
     * 
     * @param operation 操作类型（hit/miss/put/evict）
     * @param cacheName 缓存名称
     */
    public void recordCacheOperation(String operation, String cacheName) {
        incrementCounter("cache.operations",
                        "operation", operation,
                        "cache", cacheName);
    }

    /**
     * 记录业务异常
     * 
     * @param exceptionType 异常类型
     * @param module 模块名称
     */
    public void recordBusinessException(String exceptionType, String module) {
        incrementCounter("business.exceptions",
                        "type", exceptionType,
                        "module", module);
    }

    /**
     * 记录短信发送
     * 
     * @param type 短信类型
     * @param result 发送结果
     */
    public void recordSmsSend(String type, String result) {
        incrementCounter("sms.send",
                        "type", type,
                        "result", result);
    }

    /**
     * 记录登录尝试
     * 
     * @param result 登录结果（success/failure）
     * @param reason 失败原因（可选）
     */
    public void recordLoginAttempt(String result, String reason) {
        if (reason != null) {
            incrementCounter("login.attempts",
                            "result", result,
                            "reason", reason);
        } else {
            incrementCounter("login.attempts",
                            "result", result);
        }
    }

    /**
     * 构建缓存key
     */
    private String buildKey(String name, String... tags) {
        StringBuilder keyBuilder = new StringBuilder(name);
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                keyBuilder.append(":").append(tags[i]).append("=").append(tags[i + 1]);
            }
        }
        return keyBuilder.toString();
    }
} 