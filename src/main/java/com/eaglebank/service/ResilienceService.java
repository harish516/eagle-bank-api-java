package com.eaglebank.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Service for implementing resilience patterns across the application.
 * Provides circuit breaker, retry, and bulkhead functionality for improved scalability.
 */
@Service
public class ResilienceService {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceService.class);

    private final CircuitBreaker databaseCircuitBreaker;
    private final CircuitBreaker externalApiCircuitBreaker;
    private final CircuitBreaker cacheCircuitBreaker;
    private final Retry defaultRetry;
    private final Bulkhead databaseBulkhead;
    private final Bulkhead externalApiBulkhead;
    private final Counter circuitBreakerOpenCounter;

    @Autowired
    public ResilienceService(
            CircuitBreaker databaseCircuitBreaker,
            CircuitBreaker externalApiCircuitBreaker,
            CircuitBreaker cacheCircuitBreaker,
            Retry defaultRetry,
            Bulkhead databaseBulkhead,
            Bulkhead externalApiBulkhead,
            @Autowired(required = false) Counter circuitBreakerOpenCounter) {
        this.databaseCircuitBreaker = databaseCircuitBreaker;
        this.externalApiCircuitBreaker = externalApiCircuitBreaker;
        this.cacheCircuitBreaker = cacheCircuitBreaker;
        this.defaultRetry = defaultRetry;
        this.databaseBulkhead = databaseBulkhead;
        this.externalApiBulkhead = externalApiBulkhead;
        this.circuitBreakerOpenCounter = circuitBreakerOpenCounter;
        
        setupCircuitBreakerEvents();
    }

    /**
     * Execute database operations with resilience patterns.
     */
    public <T> T executeDatabaseOperation(Supplier<T> operation, Supplier<T> fallback) {
        Supplier<T> decoratedSupplier = CircuitBreaker
                .decorateSupplier(databaseCircuitBreaker, operation);
        decoratedSupplier = Retry.decorateSupplier(defaultRetry, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(databaseBulkhead, decoratedSupplier);

        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            logger.warn("Database operation failed, using fallback: {}", e.getMessage());
            return fallback.get();
        }
    }

    /**
     * Execute external API calls with resilience patterns.
     */
    public <T> T executeExternalApiCall(Supplier<T> operation, Supplier<T> fallback) {
        Supplier<T> decoratedSupplier = CircuitBreaker
                .decorateSupplier(externalApiCircuitBreaker, operation);
        decoratedSupplier = Retry.decorateSupplier(defaultRetry, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(externalApiBulkhead, decoratedSupplier);

        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            logger.warn("External API call failed, using fallback: {}", e.getMessage());
            return fallback.get();
        }
    }

    /**
     * Execute cache operations with resilience patterns.
     */
    public <T> T executeCacheOperation(Supplier<T> operation, Supplier<T> fallback) {
        Supplier<T> decoratedSupplier = CircuitBreaker
                .decorateSupplier(cacheCircuitBreaker, operation);

        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            logger.warn("Cache operation failed, using fallback: {}", e.getMessage());
            return fallback.get();
        }
    }

    /**
     * Get circuit breaker state for monitoring.
     */
    public CircuitBreaker.State getDatabaseCircuitBreakerState() {
        return databaseCircuitBreaker.getState();
    }

    public CircuitBreaker.State getExternalApiCircuitBreakerState() {
        return externalApiCircuitBreaker.getState();
    }

    public CircuitBreaker.State getCacheCircuitBreakerState() {
        return cacheCircuitBreaker.getState();
    }

    /**
     * Get circuit breaker metrics for monitoring.
     */
    public CircuitBreaker.Metrics getDatabaseCircuitBreakerMetrics() {
        return databaseCircuitBreaker.getMetrics();
    }

    public CircuitBreaker.Metrics getExternalApiCircuitBreakerMetrics() {
        return externalApiCircuitBreaker.getMetrics();
    }

    public CircuitBreaker.Metrics getCacheCircuitBreakerMetrics() {
        return cacheCircuitBreaker.getMetrics();
    }

    /**
     * Setup event listeners for circuit breakers to enable monitoring.
     */
    private void setupCircuitBreakerEvents() {
        databaseCircuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    logger.info("Database circuit breaker state transition: {} -> {}", 
                        event.getStateTransition().getFromState(), 
                        event.getStateTransition().getToState());
                    if (circuitBreakerOpenCounter != null && 
                        event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                        circuitBreakerOpenCounter.increment();
                    }
                });

        externalApiCircuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    logger.info("External API circuit breaker state transition: {} -> {}", 
                        event.getStateTransition().getFromState(), 
                        event.getStateTransition().getToState());
                    if (circuitBreakerOpenCounter != null && 
                        event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                        circuitBreakerOpenCounter.increment();
                    }
                });

        cacheCircuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    logger.info("Cache circuit breaker state transition: {} -> {}", 
                        event.getStateTransition().getFromState(), 
                        event.getStateTransition().getToState());
                    if (circuitBreakerOpenCounter != null && 
                        event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                        circuitBreakerOpenCounter.increment();
                    }
                });

        // Setup failure and success event logging
        databaseCircuitBreaker.getEventPublisher()
                .onFailureRateExceeded(event -> 
                    logger.warn("Database circuit breaker failure rate exceeded: {}%", event.getFailureRate()));

        externalApiCircuitBreaker.getEventPublisher()
                .onFailureRateExceeded(event -> 
                    logger.warn("External API circuit breaker failure rate exceeded: {}%", event.getFailureRate()));

        cacheCircuitBreaker.getEventPublisher()
                .onFailureRateExceeded(event -> 
                    logger.warn("Cache circuit breaker failure rate exceeded: {}%", event.getFailureRate()));
    }
}
