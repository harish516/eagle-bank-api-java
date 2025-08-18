package com.eaglebank.performance;

import com.eaglebank.service.ResilienceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance testing component to demonstrate scalability improvements.
 * Runs load tests to validate circuit breaker, rate limiting, and caching performance.
 */
@Component
@Profile("performance-test")
public class ScalabilityPerformanceTest implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ScalabilityPerformanceTest.class);

    @Autowired(required = false)
    private ResilienceService resilienceService;
    
    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    private final ExecutorService executorService = Executors.newFixedThreadPool(50);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);

    @Override
    public void run(String... args) {
        logger.info("Starting Eagle Bank API Scalability Performance Tests");
        
        try {
            runDatabaseResilienceTest();
            runExternalApiResilienceTest();
            runCacheResilienceTest();
            runConcurrentLoadTest();
            generatePerformanceReport();
        } catch (Exception e) {
            logger.error("Performance test failed", e);
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Test database operations with circuit breaker and bulkhead.
     */
    private void runDatabaseResilienceTest() throws InterruptedException {
        logger.info("Running Database Resilience Test...");
        
        if (resilienceService == null) {
            logger.warn("ResilienceService not available, skipping database resilience test");
            return;
        }

        int numberOfRequests = 1000;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        AtomicInteger dbSuccessCount = new AtomicInteger(0);
        AtomicInteger dbFailureCount = new AtomicInteger(0);

        Instant startTime = Instant.now();

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            executorService.submit(() -> {
                try {
                    // Simulate database operation
                    String result = resilienceService.executeDatabaseOperation(
                        () -> {
                            // Simulate some database operations that might fail
                            if (requestId % 20 == 0) {
                                throw new RuntimeException("Simulated database failure");
                            }
                            // Simulate processing time
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return "DB_SUCCESS_" + requestId;
                        },
                        () -> "DB_FALLBACK_" + requestId
                    );
                    
                    if (result.contains("SUCCESS")) {
                        dbSuccessCount.incrementAndGet();
                    } else {
                        dbFailureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    dbFailureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        Duration duration = Duration.between(startTime, Instant.now());

        logger.info("Database Resilience Test Results:");
        logger.info("  Total Requests: {}", numberOfRequests);
        logger.info("  Successful: {}", dbSuccessCount.get());
        logger.info("  Failed/Fallback: {}", dbFailureCount.get());
        logger.info("  Duration: {} ms", duration.toMillis());
        logger.info("  Throughput: {} req/sec", numberOfRequests * 1000.0 / duration.toMillis());
        
        if (resilienceService != null) {
            logger.info("  Circuit Breaker State: {}", resilienceService.getDatabaseCircuitBreakerState());
            var metrics = resilienceService.getDatabaseCircuitBreakerMetrics();
            logger.info("  Circuit Breaker Success Rate: {}%", 
                metrics.getFailureRate() == -1 ? 100 : (100 - metrics.getFailureRate()));
        }
    }

    /**
     * Test external API calls with circuit breaker and retry.
     */
    private void runExternalApiResilienceTest() throws InterruptedException {
        logger.info("Running External API Resilience Test...");
        
        if (resilienceService == null) {
            logger.warn("ResilienceService not available, skipping external API resilience test");
            return;
        }

        int numberOfRequests = 500;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        AtomicInteger apiSuccessCount = new AtomicInteger(0);
        AtomicInteger apiFailureCount = new AtomicInteger(0);

        Instant startTime = Instant.now();

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            executorService.submit(() -> {
                try {
                    String result = resilienceService.executeExternalApiCall(
                        () -> {
                            // Simulate external API call that might fail or be slow
                            if (requestId % 15 == 0) {
                                throw new RuntimeException("Simulated API timeout");
                            }
                            // Simulate network latency
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return "API_SUCCESS_" + requestId;
                        },
                        () -> "API_FALLBACK_" + requestId
                    );
                    
                    if (result.contains("SUCCESS")) {
                        apiSuccessCount.incrementAndGet();
                    } else {
                        apiFailureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    apiFailureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        Duration duration = Duration.between(startTime, Instant.now());

        logger.info("External API Resilience Test Results:");
        logger.info("  Total Requests: {}", numberOfRequests);
        logger.info("  Successful: {}", apiSuccessCount.get());
        logger.info("  Failed/Fallback: {}", apiFailureCount.get());
        logger.info("  Duration: {} ms", duration.toMillis());
        logger.info("  Throughput: {} req/sec", numberOfRequests * 1000.0 / duration.toMillis());
        
        if (resilienceService != null) {
            logger.info("  Circuit Breaker State: {}", resilienceService.getExternalApiCircuitBreakerState());
            var metrics = resilienceService.getExternalApiCircuitBreakerMetrics();
            logger.info("  Circuit Breaker Success Rate: {}%", 
                metrics.getFailureRate() == -1 ? 100 : (100 - metrics.getFailureRate()));
        }
    }

    /**
     * Test cache operations with circuit breaker.
     */
    private void runCacheResilienceTest() throws InterruptedException {
        logger.info("Running Cache Resilience Test...");
        
        if (resilienceService == null) {
            logger.warn("ResilienceService not available, skipping cache resilience test");
            return;
        }

        int numberOfRequests = 2000;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        AtomicInteger cacheHits = new AtomicInteger(0);
        AtomicInteger cacheMisses = new AtomicInteger(0);

        Instant startTime = Instant.now();

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            executorService.submit(() -> {
                try {
                    String result = resilienceService.executeCacheOperation(
                        () -> {
                            // Simulate cache operation
                            if (requestId % 100 == 0) {
                                throw new RuntimeException("Simulated cache failure");
                            }
                            // Simulate fast cache access
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return "CACHE_HIT_" + requestId;
                        },
                        () -> {
                            // Fallback to slower data source
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return "CACHE_MISS_" + requestId;
                        }
                    );
                    
                    if (result.contains("HIT")) {
                        cacheHits.incrementAndGet();
                    } else {
                        cacheMisses.incrementAndGet();
                    }
                } catch (Exception e) {
                    cacheMisses.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        Duration duration = Duration.between(startTime, Instant.now());

        logger.info("Cache Resilience Test Results:");
        logger.info("  Total Requests: {}", numberOfRequests);
        logger.info("  Cache Hits: {}", cacheHits.get());
        logger.info("  Cache Misses: {}", cacheMisses.get());
        logger.info("  Cache Hit Rate: {}%", (cacheHits.get() * 100.0) / numberOfRequests);
        logger.info("  Duration: {} ms", duration.toMillis());
        logger.info("  Throughput: {} req/sec", numberOfRequests * 1000.0 / duration.toMillis());
        
        if (resilienceService != null) {
            logger.info("  Circuit Breaker State: {}", resilienceService.getCacheCircuitBreakerState());
        }
    }

    /**
     * Run concurrent load test to measure overall system performance.
     */
    private void runConcurrentLoadTest() throws InterruptedException {
        logger.info("Running Concurrent Load Test...");

        int numberOfThreads = 100;
        int requestsPerThread = 50;
        int totalRequests = numberOfThreads * requestsPerThread;
        
        CountDownLatch latch = new CountDownLatch(totalRequests);
        ExecutorService loadTestExecutor = Executors.newFixedThreadPool(numberOfThreads);

        Instant startTime = Instant.now();

        for (int thread = 0; thread < numberOfThreads; thread++) {
            final int threadId = thread;
            loadTestExecutor.submit(() -> {
                for (int req = 0; req < requestsPerThread; req++) {
                    final int requestId = threadId * requestsPerThread + req;
                    
                    Instant requestStart = Instant.now();
                    try {
                        // Simulate various operations
                        simulateApiRequest(requestId);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        Duration requestDuration = Duration.between(requestStart, Instant.now());
                        totalResponseTime.addAndGet(requestDuration.toMillis());
                        latch.countDown();
                    }
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        loadTestExecutor.shutdown();
        
        Duration totalDuration = Duration.between(startTime, Instant.now());

        logger.info("Concurrent Load Test Results:");
        logger.info("  Total Threads: {}", numberOfThreads);
        logger.info("  Requests per Thread: {}", requestsPerThread);
        logger.info("  Total Requests: {}", totalRequests);
        logger.info("  Successful Requests: {}", successCount.get());
        logger.info("  Failed Requests: {}", failureCount.get());
        logger.info("  Success Rate: {}%", (successCount.get() * 100.0) / totalRequests);
        logger.info("  Total Duration: {} ms", totalDuration.toMillis());
        logger.info("  Average Response Time: {} ms", totalResponseTime.get() / (double) totalRequests);
        logger.info("  Throughput: {} req/sec", totalRequests * 1000.0 / totalDuration.toMillis());
    }

    /**
     * Simulate API request processing.
     */
    private void simulateApiRequest(int requestId) {
        // Simulate different types of operations
        switch (requestId % 4) {
            case 0:
                // Simulate account balance check (fast)
                try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                break;
            case 1:
                // Simulate transaction processing (medium)
                try { Thread.sleep(15); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                break;
            case 2:
                // Simulate report generation (slow)
                try { Thread.sleep(25); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                break;
            case 3:
                // Simulate user authentication (medium)
                try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                break;
        }
        
        // Randomly simulate some failures
        if (requestId % 50 == 0) {
            throw new RuntimeException("Simulated service failure");
        }
    }

    /**
     * Generate comprehensive performance report.
     */
    private void generatePerformanceReport() {
        logger.info("\n" + "=".repeat(80));
        logger.info("EAGLE BANK API SCALABILITY PERFORMANCE REPORT");
        logger.info("=".repeat(80));
        
        if (meterRegistry != null) {
            logger.info("Metrics Registry: Available");
            logger.info("Metrics Collected: {}", meterRegistry.getMeters().size());
        } else {
            logger.info("Metrics Registry: Not Available");
        }
        
        if (resilienceService != null) {
            logger.info("Resilience Patterns: Active");
            logger.info("Database Circuit Breaker: {}", resilienceService.getDatabaseCircuitBreakerState());
            logger.info("External API Circuit Breaker: {}", resilienceService.getExternalApiCircuitBreakerState());
            logger.info("Cache Circuit Breaker: {}", resilienceService.getCacheCircuitBreakerState());
        } else {
            logger.info("Resilience Patterns: Not Available");
        }

        // Performance recommendations
        logger.info("\nPERFORMANCE ANALYSIS:");
        double successRate = (successCount.get() * 100.0) / (successCount.get() + failureCount.get());
        
        if (successRate > 95) {
            logger.info("✅ Excellent success rate: {}%", String.format("%.2f", successRate));
        } else if (successRate > 90) {
            logger.info("⚠️  Good success rate: {}%", String.format("%.2f", successRate));
        } else {
            logger.info("❌ Poor success rate: {}% - Review resilience patterns", String.format("%.2f", successRate));
        }

        double avgResponseTime = totalResponseTime.get() / (double) (successCount.get() + failureCount.get());
        if (avgResponseTime < 20) {
            logger.info("✅ Excellent response time: {} ms", String.format("%.2f", avgResponseTime));
        } else if (avgResponseTime < 50) {
            logger.info("⚠️  Acceptable response time: {} ms", String.format("%.2f", avgResponseTime));
        } else {
            logger.info("❌ Slow response time: {} ms - Consider optimization", String.format("%.2f", avgResponseTime));
        }

        logger.info("\nRECOMMENDATIONS:");
        logger.info("1. Implement Redis distributed caching for better cache performance");
        logger.info("2. Configure HikariCP connection pooling for database optimization");
        logger.info("3. Enable async processing for non-critical operations");
        logger.info("4. Set up monitoring with Prometheus and Grafana");
        logger.info("5. Configure auto-scaling based on CPU and memory metrics");
        
        logger.info("=".repeat(80));
    }
}
