package com.eaglebank.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;

/**
 * Configuration for advanced monitoring and metrics collection.
 * Provides comprehensive observability for production scalability monitoring.
 */
@Configuration
@Profile("prod")
public class MonitoringConfig {

    // Business metrics counters
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    private final AtomicLong totalTransactions = new AtomicLong(0);
    private final AtomicLong totalTransactionAmount = new AtomicLong(0);

    @ConfigurationProperties(prefix = "eagle-bank.monitoring")
    public static class MonitoringProperties {
        private String applicationName = "eagle-bank-api";
        private String environment = "production";
        private Duration metricsCollectionInterval = Duration.ofSeconds(30);
        
        // Getters and setters
        public String getApplicationName() { return applicationName; }
        public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public Duration getMetricsCollectionInterval() { return metricsCollectionInterval; }
        public void setMetricsCollectionInterval(Duration metricsCollectionInterval) { this.metricsCollectionInterval = metricsCollectionInterval; }
    }

    /**
     * Prometheus registry for metrics collection.
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * Customize meter registry with common tags and configurations.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config()
                    .commonTags("application", "eagle-bank-api")
                    .commonTags("environment", "production")
                    .commonTags("version", "1.0.0");
        };
    }

    /**
     * Counter for API request tracking.
     */
    @Bean
    public Counter apiRequestCounter(MeterRegistry registry) {
        return Counter.builder("eagle_bank_api_requests_total")
                .description("Total number of API requests")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Counter for authentication attempts.
     */
    @Bean
    public Counter authenticationCounter(MeterRegistry registry) {
        return Counter.builder("eagle_bank_authentication_attempts_total")
                .description("Total number of authentication attempts")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Counter for successful transactions.
     */
    @Bean
    public Counter transactionSuccessCounter(MeterRegistry registry) {
        return Counter.builder("eagle_bank_transactions_success_total")
                .description("Total number of successful transactions")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Counter for failed transactions.
     */
    @Bean
    public Counter transactionFailureCounter(MeterRegistry registry) {
        return Counter.builder("eagle_bank_transactions_failure_total")
                .description("Total number of failed transactions")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Timer for API response times.
     */
    @Bean
    public Timer apiResponseTimer(MeterRegistry registry) {
        return Timer.builder("eagle_bank_api_response_time")
                .description("API response time in milliseconds")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Timer for database operation durations.
     */
    @Bean
    public Timer databaseOperationTimer(MeterRegistry registry) {
        return Timer.builder("eagle_bank_database_operation_time")
                .description("Database operation time in milliseconds")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Gauge for active user sessions.
     */
    @Bean
    public Gauge activeSessionsGauge(MeterRegistry registry) {
        return Gauge.builder("eagle_bank_active_sessions", activeSessions, AtomicInteger::get)
                .description("Current number of active user sessions")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Gauge for active users.
     */
    @Bean
    public Gauge activeUsersGauge(MeterRegistry registry) {
        return Gauge.builder("eagle_bank_active_users", activeUsers, AtomicInteger::get)
                .description("Current number of active users")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Gauge for total transaction count.
     */
    @Bean
    public Gauge totalTransactionsGauge(MeterRegistry registry) {
        return Gauge.builder("eagle_bank_total_transactions", totalTransactions, AtomicLong::get)
                .description("Total number of transactions processed")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Gauge for total transaction amount.
     */
    @Bean
    public Gauge totalTransactionAmountGauge(MeterRegistry registry) {
        return Gauge.builder("eagle_bank_total_transaction_amount", totalTransactionAmount, AtomicLong::get)
                .description("Total amount of all transactions")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Counter for rate limit violations.
     */
    @Bean
    public Counter rateLimitViolationCounter(MeterRegistry registry) {
        return Counter.builder("eagle_bank_rate_limit_violations_total")
                .description("Total number of rate limit violations")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Counter for cache hits.
     */
    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("eagle_bank_cache_hits_total")
                .description("Total number of cache hits")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Counter for cache misses.
     */
    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("eagle_bank_cache_misses_total")
                .description("Total number of cache misses")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    /**
     * Timer for cache operation durations.
     */
    @Bean
    public Timer cacheOperationTimer(MeterRegistry registry) {
        return Timer.builder("eagle_bank_cache_operation_time")
                .description("Cache operation time in milliseconds")
                .tag("application", "eagle-bank-api")
                .register(registry);
    }

    // Methods to update business metrics
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }

    public void incrementActiveSessions() {
        activeSessions.incrementAndGet();
    }

    public void decrementActiveSessions() {
        activeSessions.decrementAndGet();
    }

    public void incrementTotalTransactions(long amount) {
        totalTransactions.incrementAndGet();
        totalTransactionAmount.addAndGet(amount);
    }
}
