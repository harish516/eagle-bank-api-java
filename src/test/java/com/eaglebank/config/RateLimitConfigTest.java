package com.eaglebank.config;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for RateLimitConfig configuration class.
 * Tests all configuration beans, cache behavior, and bandwidth configurations.
 */
@DisplayName("RateLimitConfig Tests")
class RateLimitConfigTest {

    private RateLimitConfig rateLimitConfig;

    @BeforeEach
    void setUp() {
        rateLimitConfig = new RateLimitConfig();
    }

    @Nested
    @DisplayName("Configuration and Annotation Tests")
    class ConfigurationAndAnnotationTests {

        @Test
        @DisplayName("Should be annotated with @Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            assertThat(RateLimitConfig.class.isAnnotationPresent(Configuration.class))
                    .isTrue();
        }

        @Test
        @DisplayName("Should have proper class structure")
        void shouldHaveProperClassStructure() {
            assertThat(RateLimitConfig.class.getSimpleName())
                    .isEqualTo("RateLimitConfig");
            assertThat(RateLimitConfig.class.getPackage().getName())
                    .isEqualTo("com.eaglebank.config");
            
            // Should have logging annotation (check if class has annotations)
            assertThat(RateLimitConfig.class.getAnnotations()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Rate Limit Cache Tests")
    class RateLimitCacheTests {

        @Test
        @DisplayName("Should create rate limit cache with correct configuration")
        void shouldCreateRateLimitCacheWithCorrectConfiguration() {
            Cache<String, Bucket> cache = rateLimitConfig.rateLimitCache();

            assertThat(cache).isNotNull();
            assertThat(cache.asMap()).isEmpty();
        }

        @Test
        @DisplayName("Should create cache that can store and retrieve buckets")
        void shouldCreateCacheThatCanStoreAndRetrieveBuckets() {
            Cache<String, Bucket> cache = rateLimitConfig.rateLimitCache();
            
            // Create a test bucket using default bandwidth
            Bandwidth bandwidth = rateLimitConfig.defaultBandwidth();
            Bucket testBucket = Bucket.builder()
                    .addLimit(bandwidth)
                    .build();

            // Store and retrieve bucket
            String key = "test-key";
            cache.put(key, testBucket);
            
            Bucket retrievedBucket = cache.getIfPresent(key);
            assertThat(retrievedBucket).isNotNull();
            assertThat(retrievedBucket.getAvailableTokens()).isEqualTo(20); // Initial tokens
        }

        @Test
        @DisplayName("Should create cache with statistics enabled")
        void shouldCreateCacheWithStatisticsEnabled() {
            Cache<String, Bucket> cache = rateLimitConfig.rateLimitCache();
            
            // Cache stats should be available
            assertThat(cache.stats()).isNotNull();
            assertThat(cache.stats().requestCount()).isZero();
        }

        @Test
        @DisplayName("Should create cache that handles null values correctly")
        void shouldCreateCacheThatHandlesNullValuesCorrectly() {
            Cache<String, Bucket> cache = rateLimitConfig.rateLimitCache();
            
            Bucket result = cache.getIfPresent("non-existent-key");
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Default Bandwidth Tests")
    class DefaultBandwidthTests {

        @Test
        @DisplayName("Should create default bandwidth with correct capacity")
        void shouldCreateDefaultBandwidthWithCorrectCapacity() {
            Bandwidth bandwidth = rateLimitConfig.defaultBandwidth();

            assertThat(bandwidth).isNotNull();
            
            // Create bucket to test bandwidth configuration
            Bucket bucket = Bucket.builder()
                    .addLimit(bandwidth)
                    .build();
            
            // Should have 20 initial tokens (burst capacity)
            assertThat(bucket.getAvailableTokens()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should allow consumption within limits")
        void shouldAllowConsumptionWithinLimits() {
            Bandwidth bandwidth = rateLimitConfig.defaultBandwidth();
            Bucket bucket = Bucket.builder()
                    .addLimit(bandwidth)
                    .build();

            // Should be able to consume initial tokens
            assertThat(bucket.tryConsume(10)).isTrue();
            assertThat(bucket.getAvailableTokens()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Strict Bandwidth Tests")
    class StrictBandwidthTests {

        @Test
        @DisplayName("Should create strict bandwidth with correct capacity")
        void shouldCreateStrictBandwidthWithCorrectCapacity() {
            Bandwidth bandwidth = rateLimitConfig.strictBandwidth();

            assertThat(bandwidth).isNotNull();
            
            Bucket bucket = Bucket.builder()
                    .addLimit(bandwidth)
                    .build();
            
            // Should have 5 initial tokens (burst capacity)
            assertThat(bucket.getAvailableTokens()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should be more restrictive than default bandwidth")
        void shouldBeMoreRestrictiveThanDefaultBandwidth() {
            Bandwidth defaultBandwidth = rateLimitConfig.defaultBandwidth();
            Bandwidth strictBandwidth = rateLimitConfig.strictBandwidth();

            Bucket defaultBucket = Bucket.builder()
                    .addLimit(defaultBandwidth)
                    .build();
            
            Bucket strictBucket = Bucket.builder()
                    .addLimit(strictBandwidth)
                    .build();

            // Strict should have fewer initial tokens than default
            assertThat(strictBucket.getAvailableTokens())
                    .isLessThan(defaultBucket.getAvailableTokens());
        }
    }

    @Nested
    @DisplayName("Relaxed Bandwidth Tests")
    class RelaxedBandwidthTests {

        @Test
        @DisplayName("Should create relaxed bandwidth with correct capacity")
        void shouldCreateRelaxedBandwidthWithCorrectCapacity() {
            Bandwidth bandwidth = rateLimitConfig.relaxedBandwidth();

            assertThat(bandwidth).isNotNull();
            
            Bucket bucket = Bucket.builder()
                    .addLimit(bandwidth)
                    .build();
            
            // Should have 50 initial tokens (burst capacity)
            assertThat(bucket.getAvailableTokens()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should be more permissive than default bandwidth")
        void shouldBeMorePermissiveThanDefaultBandwidth() {
            Bandwidth defaultBandwidth = rateLimitConfig.defaultBandwidth();
            Bandwidth relaxedBandwidth = rateLimitConfig.relaxedBandwidth();

            Bucket defaultBucket = Bucket.builder()
                    .addLimit(defaultBandwidth)
                    .build();
            
            Bucket relaxedBucket = Bucket.builder()
                    .addLimit(relaxedBandwidth)
                    .build();

            // Relaxed should have more initial tokens than default
            assertThat(relaxedBucket.getAvailableTokens())
                    .isGreaterThan(defaultBucket.getAvailableTokens());
        }

        @Test
        @DisplayName("Should allow larger burst consumption")
        void shouldAllowLargerBurstConsumption() {
            Bandwidth bandwidth = rateLimitConfig.relaxedBandwidth();
            Bucket bucket = Bucket.builder()
                    .addLimit(bandwidth)
                    .build();

            // Should be able to consume a large number of tokens
            assertThat(bucket.tryConsume(30)).isTrue();
            assertThat(bucket.getAvailableTokens()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Bandwidth Comparison Tests")
    class BandwidthComparisonTests {

        @Test
        @DisplayName("Should have different initial token capacities")
        void shouldHaveDifferentInitialTokenCapacities() {
            Bandwidth defaultBandwidth = rateLimitConfig.defaultBandwidth();
            Bandwidth strictBandwidth = rateLimitConfig.strictBandwidth();
            Bandwidth relaxedBandwidth = rateLimitConfig.relaxedBandwidth();

            Bucket defaultBucket = Bucket.builder().addLimit(defaultBandwidth).build();
            Bucket strictBucket = Bucket.builder().addLimit(strictBandwidth).build();
            Bucket relaxedBucket = Bucket.builder().addLimit(relaxedBandwidth).build();

            long defaultTokens = defaultBucket.getAvailableTokens();
            long strictTokens = strictBucket.getAvailableTokens();
            long relaxedTokens = relaxedBucket.getAvailableTokens();

            // Verify the hierarchy: strict < default < relaxed
            assertThat(strictTokens).isLessThan(defaultTokens);
            assertThat(defaultTokens).isLessThan(relaxedTokens);
        }

        @Test
        @DisplayName("Should create distinct bandwidth instances")
        void shouldCreateDistinctBandwidthInstances() {
            Bandwidth defaultBandwidth1 = rateLimitConfig.defaultBandwidth();
            Bandwidth defaultBandwidth2 = rateLimitConfig.defaultBandwidth();

            // Should create new instances each time (prototype scope)
            assertThat(defaultBandwidth1).isNotSameAs(defaultBandwidth2);
        }
    }
}
