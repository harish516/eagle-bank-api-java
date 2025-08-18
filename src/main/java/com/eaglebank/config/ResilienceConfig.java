package com.eaglebank.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

/**
 * Configuration for resilience patterns including circuit breakers, bulkheads, and retry policies.
 * Implements fault tolerance and system stability patterns for production scalability.
 */
@Configuration
@Profile("prod")
public class ResilienceConfig {

    @ConfigurationProperties(prefix = "eagle-bank.circuit-breaker")
    public static class CircuitBreakerProperties {
        private float failureRateThreshold = 50.0f;
        private Duration waitDurationInOpenState = Duration.ofSeconds(30);
        private int slidingWindowSize = 10;
        private int minimumNumberOfCalls = 5;
        
        // Getters and setters
        public float getFailureRateThreshold() { return failureRateThreshold; }
        public void setFailureRateThreshold(float failureRateThreshold) { this.failureRateThreshold = failureRateThreshold; }
        public Duration getWaitDurationInOpenState() { return waitDurationInOpenState; }
        public void setWaitDurationInOpenState(Duration waitDurationInOpenState) { this.waitDurationInOpenState = waitDurationInOpenState; }
        public int getSlidingWindowSize() { return slidingWindowSize; }
        public void setSlidingWindowSize(int slidingWindowSize) { this.slidingWindowSize = slidingWindowSize; }
        public int getMinimumNumberOfCalls() { return minimumNumberOfCalls; }
        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) { this.minimumNumberOfCalls = minimumNumberOfCalls; }
    }

    /**
     * Circuit breaker for database operations.
     * Prevents cascade failures when database is unavailable.
     */
    @Bean
    public CircuitBreaker databaseCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        
        return CircuitBreaker.of("database", config);
    }

    /**
     * Circuit breaker for external API calls (Keycloak, payment services, etc.).
     */
    @Bean
    public CircuitBreaker externalApiCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .slowCallRateThreshold(60)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();
        
        return CircuitBreaker.of("external-api", config);
    }

    /**
     * Circuit breaker for Redis cache operations.
     */
    @Bean
    public CircuitBreaker cacheCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(70)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .slidingWindowSize(15)
                .minimumNumberOfCalls(5)
                .slowCallRateThreshold(70)
                .slowCallDurationThreshold(Duration.ofMillis(500))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        
        return CircuitBreaker.of("cache", config);
    }

    /**
     * Retry configuration for transient failures.
     */
    @Bean
    public Retry defaultRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryOnException(throwable -> 
                    throwable instanceof java.sql.SQLException ||
                    throwable instanceof org.springframework.dao.TransientDataAccessException
                )
                .build();
        
        return Retry.of("default", config);
    }

    /**
     * Bulkhead for limiting concurrent database connections.
     */
    @Bean
    public Bulkhead databaseBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(50)
                .maxWaitDuration(Duration.ofMillis(1000))
                .build();
        
        return Bulkhead.of("database", config);
    }

    /**
     * Bulkhead for limiting concurrent external API calls.
     */
    @Bean
    public Bulkhead externalApiBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(20)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
        
        return Bulkhead.of("external-api", config);
    }

    /**
     * Time limiter for long-running operations.
     */
    @Bean
    public TimeLimiter defaultTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("default", config);
    }

    /**
     * Time limiter for critical operations with shorter timeout.
     */
    @Bean
    public TimeLimiter criticalTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("critical", config);
    }
}
