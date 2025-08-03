package com.eaglebank.service;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing rate limiting using token bucket algorithm.
 * Provides different rate limiting strategies for various endpoint types.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final Cache<String, Bucket> rateLimitCache;
    private final Bandwidth defaultBandwidth;
    private final Bandwidth strictBandwidth;
    private final Bandwidth relaxedBandwidth;

    /**
     * Checks if a request is allowed for the given key using default rate limits.
     *
     * @param key the unique identifier for rate limiting (e.g., user ID, IP address)
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, RateLimitType.DEFAULT);
    }

    /**
     * Checks if a request is allowed for the given key using specified rate limit type.
     *
     * @param key the unique identifier for rate limiting
     * @param type the type of rate limit to apply
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, RateLimitType type) {
        Bucket bucket = getBucket(key, type);
        boolean consumed = bucket.tryConsume(1);
        
        if (!consumed) {
            log.warn("Rate limit exceeded for key: {} with type: {}", key, type);
        }
        
        return consumed;
    }

    /**
     * Gets the number of available tokens for the given key.
     *
     * @param key the unique identifier for rate limiting
     * @param type the type of rate limit to check
     * @return the number of available tokens
     */
    public long getAvailableTokens(String key, RateLimitType type) {
        Bucket bucket = getBucket(key, type);
        return bucket.getAvailableTokens();
    }

    /**
     * Gets or creates a bucket for the given key and rate limit type.
     *
     * @param key the unique identifier for rate limiting
     * @param type the type of rate limit to apply
     * @return the bucket for the given key
     */
    private Bucket getBucket(String key, RateLimitType type) {
        String cacheKey = key + ":" + type.name();
        return rateLimitCache.get(cacheKey, k -> createBucket(type));
    }

    /**
     * Creates a new bucket with the appropriate bandwidth based on rate limit type.
     *
     * @param type the type of rate limit
     * @return a new bucket with configured bandwidth
     */
    private Bucket createBucket(RateLimitType type) {
        Bandwidth bandwidth = switch (type) {
            case STRICT -> strictBandwidth;
            case RELAXED -> relaxedBandwidth;
            default -> defaultBandwidth;
        };
        
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Enum defining different types of rate limits.
     */
    public enum RateLimitType {
        /**
         * Default rate limit: 100 requests per minute with burst of 20
         */
        DEFAULT,
        
        /**
         * Strict rate limit: 20 requests per minute with burst of 5
         * Used for sensitive operations like user creation, account deletion
         */
        STRICT,
        
        /**
         * Relaxed rate limit: 200 requests per minute with burst of 50
         * Used for read-only operations like getting user info, listing accounts
         */
        RELAXED
    }
}
