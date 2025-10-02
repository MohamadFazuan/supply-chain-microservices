package com.supply.chain.microservice.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Logging aspect for performance monitoring and request tracing
 * Add to: common-lib/src/main/java/com/supply/chain/microservice/common/logging/
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.info("🎯 [CONTROLLER] Starting {}.{} with args: {}", 
                className, methodName, Arrays.toString(joinPoint.getArgs()));
        
        try {
            Object result = joinPoint.proceed();
            Duration duration = Duration.between(start, Instant.now());
            
            log.info("✅ [CONTROLLER] Completed {}.{} in {}ms", 
                    className, methodName, duration.toMillis());
            
            return result;
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            
            log.error("❌ [CONTROLLER] Failed {}.{} in {}ms with error: {}", 
                    className, methodName, duration.toMillis(), e.getMessage(), e);
            
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }

    @Around("@annotation(com.supply.chain.microservice.common.logging.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.debug("⏱️ [PERFORMANCE] Starting {}.{}", className, methodName);
        
        try {
            Object result = joinPoint.proceed();
            Duration duration = Duration.between(start, Instant.now());
            
            if (duration.toMillis() > 1000) {
                log.warn("🐌 [SLOW QUERY] {}.{} took {}ms", className, methodName, duration.toMillis());
            } else {
                log.debug("⚡ [PERFORMANCE] {}.{} completed in {}ms", className, methodName, duration.toMillis());
            }
            
            return result;
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            log.error("💥 [ERROR] {}.{} failed after {}ms: {}", className, methodName, duration.toMillis(), e.getMessage());
            throw e;
        }
    }

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object logTransactions(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.debug("🔄 [TRANSACTION] Starting transaction for {}.{}", className, methodName);
        
        try {
            Object result = joinPoint.proceed();
            log.debug("✅ [TRANSACTION] Transaction completed for {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            log.error("🔴 [TRANSACTION] Transaction failed for {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
