package com.eaglebank.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for User domain entity focusing on non-validation functionality.
 * Validation tests are covered in UserBeanValidationTest.
 */
class UserTestNew {

    private User user;
    private Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        user = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateUserWithValidData() {
        assertThat(user.getId()).isEqualTo("usr-abc123");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getAddress()).isEqualTo(address);
        assertThat(user.getPhoneNumber()).isEqualTo("+44123456789");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldUpdateUserDetails() {
        Address newAddress = Address.builder()
                .line1("456 New Street")
                .town("Manchester")
                .county("Greater Manchester")
                .postcode("M1 1AA")
                .build();

        user.setName("Updated User");
        user.setAddress(newAddress);
        user.setPhoneNumber("+44987654321");
        user.setEmail("updated@example.com");

        assertThat(user.getName()).isEqualTo("Updated User");
        assertThat(user.getAddress()).isEqualTo(newAddress);
        assertThat(user.getPhoneNumber()).isEqualTo("+44987654321");
        assertThat(user.getEmail()).isEqualTo("updated@example.com");
    }

    /**
     * Tests the implementation of equals method in User class.
     */
    @Test
    void shouldImplementEqualsCorrectly() {
        User user1 = User.builder()
                .id("usr-equals")
                .name("Equals User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("equals@example.com")
                .build();

        User user2 = User.builder()
                .id("usr-equals")
                .name("Equals User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("equals@example.com")
                .build();

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void shouldImplementHashCodeCorrectly() {
        User user1 = User.builder()
                .id("usr-hash")
                .name("Hash User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("hash@example.com")
                .build();

        User user2 = User.builder()
                .id("usr-hash")
                .name("Hash User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("hash@example.com")
                .build();

        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentIds() {
        User user1 = User.builder()
                .id("usr-different1")
                .name("Same User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("same@example.com")
                .build();

        User user2 = User.builder()
                .id("usr-different2")
                .name("Same User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("same@example.com")
                .build();

        assertThat(user1).isNotEqualTo(user2);
        assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    void shouldNotBeEqualToNull() {
        assertThat(user).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToDifferentClass() {
        assertThat(user).isNotEqualTo("not a user");
    }

    @Test
    void shouldBeEqualToItself() {
        assertThat(user).isEqualTo(user);
    }
    
    /**
     * Tests the serialization and deserialization of User objects.
     * It checks if a User object can be serialized to a byte stream and then deserialized
     * @throws Exception
     */
    @Test
    void shouldSerializeAndDeserialize() throws Exception {
        // Test that User objects can be serialized (if Serializable is implemented)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(user);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        User deserializedUser = (User) ois.readObject();
        ois.close();

        assertThat(deserializedUser).isEqualTo(user);
    }

    /**
     * Tests the toBuilder pattern in User class.
     * It checks if a User object can be copied and modified using the toBuilder method.
     */
    @Test
    void shouldSupportToBuilderPattern() {
        User originalUser = User.builder()
                .id("usr-original")
                .name("Original User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("original@example.com")
                .build();

        User copiedUser = originalUser.toBuilder()
                .name("Copied User")
                .email("copied@example.com")
                .build();

        assertThat(copiedUser.getId()).isEqualTo("usr-original"); // Same ID
        assertThat(copiedUser.getName()).isEqualTo("Copied User"); // Changed name
        assertThat(copiedUser.getEmail()).isEqualTo("copied@example.com"); // Changed email
        assertThat(copiedUser.getPhoneNumber()).isEqualTo("+44123456789"); // Same phone
    }

    /**
     * Tests the concurrent creation of User objects.
     * It checks if multiple threads can create User objects without any issues.
     * @throws InterruptedException
     */
    @Test
    void shouldHandleConcurrentCreation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    User user = User.builder()
                            .id("usr-thread" + index)
                            .name("Thread User " + index)
                            .address(address)
                            .phoneNumber("+44123456789")
                            .email("thread" + index + "@example.com")
                            .build();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(0);
    }

    /**
     * Tests the creation of a User with minimal required fields.
     * It checks if the user is created successfully without optional fields.
     */
    @Test
    void shouldCreateUserWithMinimalRequiredFields() {
        User minimalUser = User.builder()
                .id("usr-min")
                .name("Min User")
                .address(address)
                .phoneNumber("+12")
                .email("min@example.com")
                .build();

        assertThat(minimalUser.getId()).isEqualTo("usr-min");
        assertThat(minimalUser.getName()).isEqualTo("Min User");
        assertThat(minimalUser.getCreatedTimestamp()).isNull();
        assertThat(minimalUser.getUpdatedTimestamp()).isNull();
    }

    /**
     * Tests the handling of created and updated timestamps in User class.
     * It checks if the timestamps are set correctly during user creation.
     */
    @Test
    void shouldHandleTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        User userWithTimestamps = User.builder()
                .id("usr-timestamp")
                .name("Timestamp User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("timestamp@example.com")
                .createdTimestamp(now)
                .updatedTimestamp(now)
                .build();

        assertThat(userWithTimestamps.getCreatedTimestamp()).isEqualTo(now);
        assertThat(userWithTimestamps.getUpdatedTimestamp()).isEqualTo(now);
    }

    @Test
    void shouldAllowDifferentTimestamps() {
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now();

        User userWithDifferentTimestamps = User.builder()
                .id("usr-difftimestamps")
                .name("Different Timestamps User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("diff@example.com")
                .createdTimestamp(created)
                .updatedTimestamp(updated)
                .build();

        assertThat(userWithDifferentTimestamps.getCreatedTimestamp()).isEqualTo(created);
        assertThat(userWithDifferentTimestamps.getUpdatedTimestamp()).isEqualTo(updated);
        assertThat(userWithDifferentTimestamps.getUpdatedTimestamp())
                .isAfter(userWithDifferentTimestamps.getCreatedTimestamp());
    }

    /**
     * Tests valid input scenarios that should succeed (without expecting exceptions).
     * These tests verify that the User entity accepts valid data formats.
     */
    @Test
    void shouldAcceptValidUserIdFormats() {
        // Test with numbers
        User userWithNumbers = User.builder()
                .id("usr-123456")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();
        assertThat(userWithNumbers.getId()).isEqualTo("usr-123456");

        // Test with mixed case
        User userWithMixedCase = User.builder()
                .id("usr-AbC123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();
        assertThat(userWithMixedCase.getId()).isEqualTo("usr-AbC123");
    }

    @Test
    void shouldAcceptValidPhoneNumberFormats() {
        // Test minimum phone number
        User userMinPhone = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+12")
                .email("test@example.com")
                .build();
        assertThat(userMinPhone.getPhoneNumber()).isEqualTo("+12");

        // Test maximum phone number
        String maxPhone = "+1" + "2".repeat(14); // 16 chars total
        User userMaxPhone = User.builder()
                .id("usr-abc124")
                .name("Test User")
                .address(address)
                .phoneNumber(maxPhone)
                .email("test@example.com")
                .build();
        assertThat(userMaxPhone.getPhoneNumber()).isEqualTo(maxPhone);

        // Test with country code 999
        User userCountryCode999 = User.builder()
                .id("usr-abc125")
                .name("Test User")
                .address(address)
                .phoneNumber("+999123456789")
                .email("test@example.com")
                .build();
        assertThat(userCountryCode999.getPhoneNumber()).isEqualTo("+999123456789");
    }

    @Test
    void shouldAcceptValidEmailFormats() {
        // Test email with plus
        User userEmailPlus = User.builder()
                .id("usr-email1")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test+tag@example.com")
                .build();
        assertThat(userEmailPlus.getEmail()).isEqualTo("test+tag@example.com");

        // Test email with underscore
        User userEmailUnderscore = User.builder()
                .id("usr-email2")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test_user@example.com")
                .build();
        assertThat(userEmailUnderscore.getEmail()).isEqualTo("test_user@example.com");

        // Test email with dot
        User userEmailDot = User.builder()
                .id("usr-email3")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test.user@example.com")
                .build();
        assertThat(userEmailDot.getEmail()).isEqualTo("test.user@example.com");

        // Test email with numbers
        User userEmailNumbers = User.builder()
                .id("usr-email4")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("user123@example123.com")
                .build();
        assertThat(userEmailNumbers.getEmail()).isEqualTo("user123@example123.com");

        // Test email with long TLD
        User userEmailLongTLD = User.builder()
                .id("usr-email5")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.museum")
                .build();
        assertThat(userEmailLongTLD.getEmail()).isEqualTo("test@example.museum");
    }

    @Test
    void shouldAcceptMaxAllowedLengths() {
        String maxUserId = "usr-" + "a".repeat(250); // Just under 255 limit
        String maxName = "a".repeat(255);
        String maxEmail = "a".repeat(243) + "@example.com"; // Just under 255 limit
        String maxPhoneNumber = "+1" + "2".repeat(14); // 16 chars total, at limit

        User validUser = User.builder()
                .id(maxUserId)
                .name(maxName)
                .address(address)
                .phoneNumber(maxPhoneNumber)
                .email(maxEmail)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        assertThat(validUser).isNotNull();
        assertThat(validUser.getId()).isEqualTo(maxUserId);
        assertThat(validUser.getName()).isEqualTo(maxName);
        assertThat(validUser.getEmail()).isEqualTo(maxEmail);
        assertThat(validUser.getPhoneNumber()).isEqualTo(maxPhoneNumber);
    }
}
