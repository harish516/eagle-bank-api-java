package com.eaglebank.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for rate limiting using Bucket4j with Caffeine cache.
 * Implements token bucket algorithm for controlling API request rates.
 */
@Configuration
@Slf4j
public class RateLimitConfig {

    /**
     * Creates a Caffeine cache for storing rate limit buckets.
     * Cache entries expire after 1 hour of inactivity to prevent memory leaks.
     */
    @Bean
    public Cache<String, Bucket> rateLimitCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000) // Maximum number of cached buckets
                .expireAfterWrite(1, TimeUnit.HOURS) // Expire buckets after 1 hour
                .recordStats() // Enable cache statistics for monitoring
                .build();
    }

    /**
     * Creates the default bandwidth configuration for API endpoints.
     * Allows 100 requests per minute with a burst capacity of 20 requests.
     */
    @Bean
    public Bandwidth defaultBandwidth() {
        return Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))
                .withInitialTokens(20); // Allow burst of 20 requests initially
    }

    /**
     * Creates strict bandwidth configuration for sensitive endpoints.
     * Allows 20 requests per minute with a burst capacity of 5 requests.
     */
    @Bean
    public Bandwidth strictBandwidth() {
        return Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)))
                .withInitialTokens(5); // Allow burst of 5 requests initially
    }

    /**
     * Creates relaxed bandwidth configuration for read-only endpoints.
     * Allows 200 requests per minute with a burst capacity of 50 requests.
     */
    @Bean
    public Bandwidth relaxedBandwidth() {
        return Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)))
                .withInitialTokens(50); // Allow burst of 50 requests initially
    }
}
