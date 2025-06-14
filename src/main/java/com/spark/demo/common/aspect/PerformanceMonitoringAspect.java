package com.spark.demo.common.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 性能监控切面
 * 监控Service层方法的执行时间和调用次数
 * 
 * @author spark
 * @date 2025-01-27
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitoringAspect {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 监控Service层方法性能
     */
    @Around("execution(* com.spark.demo.service.impl.*.*(..))") 
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String metricName = String.format("%s.%s", className, methodName);
        
        // 创建计时器
        Timer.Sample sample = Timer.start(meterRegistry);
        
        // 方法调用计数器
        Counter methodCounter = Counter.builder("method.calls")
                .tag("class", className)
                .tag("method", methodName)
                .tag("type", "service")
                .register(meterRegistry);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            // 成功调用计数
            Counter.builder("method.calls.success")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .increment();
            
            return result;
            
        } catch (Exception e) {
            // 异常调用计数
            Counter.builder("method.calls.error")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            
            throw e;
            
        } finally {
            // 记录执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            sample.stop(Timer.builder("method.execution.time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("type", "service")
                    .register(meterRegistry));
            
            methodCounter.increment();
            
            // 记录性能日志（超过1秒的慢方法）
            if (executionTime > 1000) {
                log.warn("慢方法检测: {}.{} 执行时间: {}ms", className, methodName, executionTime);
            } else if (log.isDebugEnabled()) {
                log.debug("方法执行: {}.{} 执行时间: {}ms", className, methodName, executionTime);
            }
        }
    }

    /**
     * 监控Controller层方法性能
     */
    @Around("execution(* com.spark.demo.controller.*.*(..))") 
    public Object monitorControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // 创建计时器
        Timer.Sample sample = Timer.start(meterRegistry);
        
        // API调用计数器
        Counter apiCounter = Counter.builder("api.calls")
                .tag("controller", className)
                .tag("method", methodName)
                .register(meterRegistry);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            // 成功API调用计数
            Counter.builder("api.calls.success")
                    .tag("controller", className)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .increment();
            
            return result;
            
        } catch (Exception e) {
            // 异常API调用计数
            Counter.builder("api.calls.error")
                    .tag("controller", className)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            
            throw e;
            
        } finally {
            // 记录API执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            sample.stop(Timer.builder("api.execution.time")
                    .tag("controller", className)
                    .tag("method", methodName)
                    .register(meterRegistry));
            
            apiCounter.increment();
            
            // 记录API性能日志（超过2秒的慢API）
            if (executionTime > 2000) {
                log.warn("慢API检测: {}.{} 执行时间: {}ms", className, methodName, executionTime);
            } else if (log.isDebugEnabled()) {
                log.debug("API执行: {}.{} 执行时间: {}ms", className, methodName, executionTime);
            }
        }
    }

    /**
     * 监控缓存操作性能
     */
    @Around("@annotation(org.springframework.cache.annotation.Cacheable) || " +
            "@annotation(org.springframework.cache.annotation.CacheEvict) || " +
            "@annotation(org.springframework.cache.annotation.CachePut)")
    public Object monitorCachePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // 缓存操作计数器
        Counter cacheCounter = Counter.builder("cache.operations")
                .tag("class", className)
                .tag("method", methodName)
                .register(meterRegistry);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Object result = joinPoint.proceed();
            
            // 缓存操作成功计数
            Counter.builder("cache.operations.success")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .increment();
            
            return result;
            
        } catch (Exception e) {
            // 缓存操作异常计数
            Counter.builder("cache.operations.error")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            
            throw e;
            
        } finally {
            sample.stop(Timer.builder("cache.operation.time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry));
            
            cacheCounter.increment();
        }
    }
}