package com.eaglebank.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Performance monitoring aspect for tracking method execution times.
 * Automatically logs execution duration for annotated methods.
 */
@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {

    /**
     * Annotation to mark methods for performance monitoring.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MonitorPerformance {
        /**
         * Optional description of the operation being monitored.
         */
        String value() default "";
        
        /**
         * Whether to log method parameters (be careful with sensitive data).
         */
        boolean logParameters() default false;
        
        /**
         * Threshold in milliseconds - only log if execution time exceeds this value.
         */
        long thresholdMs() default 0;
    }

    @Around("@annotation(monitorPerformance)")
    public Object monitorExecutionTime(ProceedingJoinPoint joinPoint, MonitorPerformance monitorPerformance) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + 
                           joinPoint.getSignature().getName();
        String operation = monitorPerformance.value().isEmpty() ? methodName : monitorPerformance.value();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Only log if execution time exceeds threshold
            if (executionTime >= monitorPerformance.thresholdMs()) {
                if (monitorPerformance.logParameters() && joinPoint.getArgs().length > 0) {
                    log.info("PERFORMANCE_MONITOR: {} completed - duration: {}ms, parameters: {}", 
                            operation, executionTime, joinPoint.getArgs());
                } else {
                    log.info("PERFORMANCE_MONITOR: {} completed - duration: {}ms", operation, executionTime);
                }
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("PERFORMANCE_MONITOR: {} failed - duration: {}ms, error: {}", 
                    operation, executionTime, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Monitor all service layer methods for performance (without annotation).
     */
    @Around("execution(* com.eaglebank.service.*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + 
                           joinPoint.getSignature().getName();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log slow operations (>100ms for services)
            if (executionTime > 100) {
                log.warn("SLOW_SERVICE_OPERATION: {} - duration: {}ms", methodName, executionTime);
            } else {
                log.debug("SERVICE_OPERATION: {} - duration: {}ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("SERVICE_OPERATION_FAILED: {} - duration: {}ms, error: {}", 
                    methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}
