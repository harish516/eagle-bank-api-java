package com.eaglebank.service;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for RateLimitService.
 * Tests rate limiting functionality using token bucket algorithm.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService Tests")
class RateLimitServiceTest {

    @Mock
    private Cache<String, Bucket> rateLimitCache;

    private Bandwidth defaultBandwidth;
    private Bandwidth strictBandwidth;
    private Bandwidth relaxedBandwidth;
    
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        // Create bandwidth configurations matching production settings
        defaultBandwidth = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))
                .withInitialTokens(20);
        strictBandwidth = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)))
                .withInitialTokens(5);
        relaxedBandwidth = Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)))
                .withInitialTokens(50);

        rateLimitService = new RateLimitService(rateLimitCache, defaultBandwidth, strictBandwidth, relaxedBandwidth);
    }

    @Nested
    @DisplayName("Basic Rate Limiting Tests")
    class BasicRateLimitingTests {

        @Test
        @DisplayName("Should allow request when using default rate limit type")
        void shouldAllowRequestWithDefaultRateLimit() {
            // Given
            String key = "user123";
            Bucket mockBucket = createMockBucket(true);
            when(rateLimitCache.get(eq(key + ":DEFAULT"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key);

            // Then
            assertThat(result).isTrue();
            verify(mockBucket).tryConsume(1);
            verify(rateLimitCache).get(eq(key + ":DEFAULT"), any());
        }

        @Test
        @DisplayName("Should allow request when using explicit default rate limit type")
        void shouldAllowRequestWithExplicitDefaultRateLimit() {
            // Given
            String key = "user123";
            Bucket mockBucket = createMockBucket(true);
            when(rateLimitCache.get(eq(key + ":DEFAULT"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key, RateLimitService.RateLimitType.DEFAULT);

            // Then
            assertThat(result).isTrue();
            verify(mockBucket).tryConsume(1);
        }

        @Test
        @DisplayName("Should deny request when rate limit exceeded")
        void shouldDenyRequestWhenRateLimitExceeded() {
            // Given
            String key = "user123";
            Bucket mockBucket = createMockBucket(false);
            when(rateLimitCache.get(eq(key + ":DEFAULT"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key);

            // Then
            assertThat(result).isFalse();
            verify(mockBucket).tryConsume(1);
        }
    }

    @Nested
    @DisplayName("Rate Limit Type Tests")
    class RateLimitTypeTests {

        @Test
        @DisplayName("Should handle STRICT rate limit type")
        void shouldHandleStrictRateLimit() {
            // Given
            String key = "admin-user";
            Bucket mockBucket = createMockBucket(true);
            when(rateLimitCache.get(eq(key + ":STRICT"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key, RateLimitService.RateLimitType.STRICT);

            // Then
            assertThat(result).isTrue();
            verify(mockBucket).tryConsume(1);
            verify(rateLimitCache).get(eq(key + ":STRICT"), any());
        }

        @Test
        @DisplayName("Should handle RELAXED rate limit type")
        void shouldHandleRelaxedRateLimit() {
            // Given
            String key = "read-only-user";
            Bucket mockBucket = createMockBucket(true);
            when(rateLimitCache.get(eq(key + ":RELAXED"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key, RateLimitService.RateLimitType.RELAXED);

            // Then
            assertThat(result).isTrue();
            verify(mockBucket).tryConsume(1);
            verify(rateLimitCache).get(eq(key + ":RELAXED"), any());
        }

        @Test
        @DisplayName("Should deny STRICT rate limit when exceeded")
        void shouldDenyStrictRateLimitWhenExceeded() {
            // Given
            String key = "admin-user";
            Bucket mockBucket = createMockBucket(false);
            when(rateLimitCache.get(eq(key + ":STRICT"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key, RateLimitService.RateLimitType.STRICT);

            // Then
            assertThat(result).isFalse();
            verify(mockBucket).tryConsume(1);
        }

        @Test
        @DisplayName("Should deny RELAXED rate limit when exceeded")
        void shouldDenyRelaxedRateLimitWhenExceeded() {
            // Given
            String key = "read-only-user";
            Bucket mockBucket = createMockBucket(false);
            when(rateLimitCache.get(eq(key + ":RELAXED"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key, RateLimitService.RateLimitType.RELAXED);

            // Then
            assertThat(result).isFalse();
            verify(mockBucket).tryConsume(1);
        }
    }

    @Nested
    @DisplayName("Token Availability Tests")
    class TokenAvailabilityTests {

        @Test
        @DisplayName("Should return available tokens for DEFAULT rate limit")
        void shouldReturnAvailableTokensForDefault() {
            // Given
            String key = "user123";
            long expectedTokens = 15L;
            Bucket mockBucket = mock(Bucket.class);
            when(mockBucket.getAvailableTokens()).thenReturn(expectedTokens);
            when(rateLimitCache.get(eq(key + ":DEFAULT"), any())).thenReturn(mockBucket);

            // When
            long availableTokens = rateLimitService.getAvailableTokens(key, RateLimitService.RateLimitType.DEFAULT);

            // Then
            assertThat(availableTokens).isEqualTo(expectedTokens);
            verify(mockBucket).getAvailableTokens();
        }

        @Test
        @DisplayName("Should return available tokens for STRICT rate limit")
        void shouldReturnAvailableTokensForStrict() {
            // Given
            String key = "admin-user";
            long expectedTokens = 3L;
            Bucket mockBucket = mock(Bucket.class);
            when(mockBucket.getAvailableTokens()).thenReturn(expectedTokens);
            when(rateLimitCache.get(eq(key + ":STRICT"), any())).thenReturn(mockBucket);

            // When
            long availableTokens = rateLimitService.getAvailableTokens(key, RateLimitService.RateLimitType.STRICT);

            // Then
            assertThat(availableTokens).isEqualTo(expectedTokens);
            verify(mockBucket).getAvailableTokens();
        }

        @Test
        @DisplayName("Should return available tokens for RELAXED rate limit")
        void shouldReturnAvailableTokensForRelaxed() {
            // Given
            String key = "read-only-user";
            long expectedTokens = 45L;
            Bucket mockBucket = mock(Bucket.class);
            when(mockBucket.getAvailableTokens()).thenReturn(expectedTokens);
            when(rateLimitCache.get(eq(key + ":RELAXED"), any())).thenReturn(mockBucket);

            // When
            long availableTokens = rateLimitService.getAvailableTokens(key, RateLimitService.RateLimitType.RELAXED);

            // Then
            assertThat(availableTokens).isEqualTo(expectedTokens);
            verify(mockBucket).getAvailableTokens();
        }

        @Test
        @DisplayName("Should return zero tokens when bucket is empty")
        void shouldReturnZeroTokensWhenBucketEmpty() {
            // Given
            String key = "exhausted-user";
            Bucket mockBucket = mock(Bucket.class);
            when(mockBucket.getAvailableTokens()).thenReturn(0L);
            when(rateLimitCache.get(eq(key + ":DEFAULT"), any())).thenReturn(mockBucket);

            // When
            long availableTokens = rateLimitService.getAvailableTokens(key, RateLimitService.RateLimitType.DEFAULT);

            // Then
            assertThat(availableTokens).isZero();
        }
    }

    @Nested
    @DisplayName("Bucket Creation Tests")
    class BucketCreationTests {

        @Test
        @DisplayName("Should create new bucket when cache miss for DEFAULT type")
        void shouldCreateNewBucketForDefaultType() {
            // Given
            String key = "new-user";
            Bucket newBucket = Bucket.builder()
                    .addLimit(defaultBandwidth)
                    .build();
            when(rateLimitCache.get(eq(key + ":DEFAULT"), any())).thenReturn(newBucket);

            // When
            boolean result = rateLimitService.isAllowed(key);

            // Then
            assertThat(result).isTrue(); // New bucket should have initial tokens
            verify(rateLimitCache).get(eq(key + ":DEFAULT"), any());
        }

        @Test
        @DisplayName("Should create new bucket when cache miss for STRICT type")
        void shouldCreateNewBucketForStrictType() {
            // Given
            String key = "new-admin";
            Bucket newBucket = Bucket.builder()
                    .addLimit(strictBandwidth)
                    .build();
            when(rateLimitCache.get(eq(key + ":STRICT"), any())).thenReturn(newBucket);

            // When
            boolean result = rateLimitService.isAllowed(key, RateLimitService.RateLimitType.STRICT);

            // Then
            assertThat(result).isTrue(); // New bucket should have initial tokens
            verify(rateLimitCache).get(eq(key + ":STRICT"), any());
        }

        @Test
        @DisplayName("Should create new bucket when cache miss for RELAXED type")
        void shouldCreateNewBucketForRelaxedType() {
            // Given
            String key = "new-reader";
            Bucket newBucket = Bucket.builder()
                    .addLimit(relaxedBandwidth)
                    .build();
            when(rateLimitCache.get(eq(key + ":RELAXED"), any())).thenReturn(newBucket);

            // When
            boolean result = rateLimitService.isAllowed(key, RateLimitService.RateLimitType.RELAXED);

            // Then
            assertThat(result).isTrue(); // New bucket should have initial tokens
            verify(rateLimitCache).get(eq(key + ":RELAXED"), any());
        }
    }

    @Nested
    @DisplayName("Cache Key Generation Tests")
    class CacheKeyGenerationTests {

        @Test
        @DisplayName("Should generate correct cache key for different users with same rate limit type")
        void shouldGenerateCorrectCacheKeyForDifferentUsers() {
            // Given
            String user1 = "user123";
            String user2 = "user456";
            Bucket mockBucket1 = createMockBucket(true);
            Bucket mockBucket2 = createMockBucket(true);
            
            when(rateLimitCache.get(eq(user1 + ":DEFAULT"), any())).thenReturn(mockBucket1);
            when(rateLimitCache.get(eq(user2 + ":DEFAULT"), any())).thenReturn(mockBucket2);

            // When
            rateLimitService.isAllowed(user1);
            rateLimitService.isAllowed(user2);

            // Then
            verify(rateLimitCache).get(eq(user1 + ":DEFAULT"), any());
            verify(rateLimitCache).get(eq(user2 + ":DEFAULT"), any());
        }

        @Test
        @DisplayName("Should generate correct cache key for same user with different rate limit types")
        void shouldGenerateCorrectCacheKeyForDifferentRateLimitTypes() {
            // Given
            String user = "user123";
            Bucket mockBucketDefault = createMockBucket(true);
            Bucket mockBucketStrict = createMockBucket(true);
            Bucket mockBucketRelaxed = createMockBucket(true);
            
            when(rateLimitCache.get(eq(user + ":DEFAULT"), any())).thenReturn(mockBucketDefault);
            when(rateLimitCache.get(eq(user + ":STRICT"), any())).thenReturn(mockBucketStrict);
            when(rateLimitCache.get(eq(user + ":RELAXED"), any())).thenReturn(mockBucketRelaxed);

            // When
            rateLimitService.isAllowed(user, RateLimitService.RateLimitType.DEFAULT);
            rateLimitService.isAllowed(user, RateLimitService.RateLimitType.STRICT);
            rateLimitService.isAllowed(user, RateLimitService.RateLimitType.RELAXED);

            // Then
            verify(rateLimitCache).get(eq(user + ":DEFAULT"), any());
            verify(rateLimitCache).get(eq(user + ":STRICT"), any());
            verify(rateLimitCache).get(eq(user + ":RELAXED"), any());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Should handle null key gracefully")
        void shouldHandleNullKeyGracefully() {
            // Given
            String key = null;
            Bucket mockBucket = createMockBucket(true);
            when(rateLimitCache.get(eq("null:DEFAULT"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key);

            // Then
            assertThat(result).isTrue();
            verify(rateLimitCache).get(eq("null:DEFAULT"), any());
        }

        @Test
        @DisplayName("Should handle empty key")
        void shouldHandleEmptyKey() {
            // Given
            String key = "";
            Bucket mockBucket = createMockBucket(true);
            when(rateLimitCache.get(eq(":DEFAULT"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key);

            // Then
            assertThat(result).isTrue();
            verify(rateLimitCache).get(eq(":DEFAULT"), any());
        }

        @Test
        @DisplayName("Should handle special characters in key")
        void shouldHandleSpecialCharactersInKey() {
            // Given
            String key = "user@domain.com:123!";
            Bucket mockBucket = createMockBucket(true);
            when(rateLimitCache.get(eq(key + ":DEFAULT"), any())).thenReturn(mockBucket);

            // When
            boolean result = rateLimitService.isAllowed(key);

            // Then
            assertThat(result).isTrue();
            verify(rateLimitCache).get(eq(key + ":DEFAULT"), any());
        }
    }

    @Nested
    @DisplayName("RateLimitType Enum Tests")
    class RateLimitTypeEnumTests {

        @Test
        @DisplayName("Should have all expected rate limit types")
        void shouldHaveAllExpectedRateLimitTypes() {
            // When & Then
            RateLimitService.RateLimitType[] types = RateLimitService.RateLimitType.values();
            
            assertThat(types).hasSize(3);
            assertThat(types).containsExactlyInAnyOrder(
                RateLimitService.RateLimitType.DEFAULT,
                RateLimitService.RateLimitType.STRICT,
                RateLimitService.RateLimitType.RELAXED
            );
        }

        @Test
        @DisplayName("Should convert from string correctly")
        void shouldConvertFromStringCorrectly() {
            // When & Then
            assertThat(RateLimitService.RateLimitType.valueOf("DEFAULT"))
                .isEqualTo(RateLimitService.RateLimitType.DEFAULT);
            assertThat(RateLimitService.RateLimitType.valueOf("STRICT"))
                .isEqualTo(RateLimitService.RateLimitType.STRICT);
            assertThat(RateLimitService.RateLimitType.valueOf("RELAXED"))
                .isEqualTo(RateLimitService.RateLimitType.RELAXED);
        }
    }

    /**
     * Helper method to create a mock bucket that simulates token consumption.
     *
     * @param shouldConsume whether the bucket should successfully consume a token
     * @return mocked Bucket instance
     */
    private Bucket createMockBucket(boolean shouldConsume) {
        Bucket mockBucket = mock(Bucket.class);
        when(mockBucket.tryConsume(1)).thenReturn(shouldConsume);
        return mockBucket;
    }
}
