package com.eaglebank.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class UserTest {

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
    void shouldValidateUserIdFormat() {
        assertThatThrownBy(() -> User.builder()
                .id("invalid-id")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must match pattern");
    }

    @Test
    void shouldValidatePhoneNumberFormat() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("invalid-phone")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number must be in international format");
    }

    @Test
    void shouldValidateEmailFormat() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("invalid-email")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");
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
     * Tests the validation of null fields in User attributes.
     * It checks if the builder throws NullPointerException when required fields are null.
     */
    @Test
    void shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> User.builder()
                .id(null)
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id");
    }

    @Test
    void shouldThrowWhenPhoneNumberIsNull() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber(null)
                .email("test@example.com")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("phoneNumber");
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name(null)
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");
    }

    @Test
    void shouldThrowWhenAddressIsNull() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(null)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("address");
    }

    @Test
    void shouldThrowWhenEmailIsNull() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("email");
    }

    /**
     * Tests the validation of empty and blank fields in User attributes.
     */

    @Test
    void shouldThrowWhenNameIsEmpty() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("   ")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void shouldThrowWhenEmailIsEmpty() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be empty");
    }

    @Test
    void shouldThrowWhenEmailIsBlank() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("   ")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be empty");
    }

    @Test
    void shouldThrowWhenPhoneNumberIsEmpty() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number cannot be empty");
    }

    @Test
    void shouldThrowWhenPhoneNumberIsBlank() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("   ")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number cannot be empty");
    }

    @Test
    void shouldThrowWhenUserIdIsEmpty() {
        assertThatThrownBy(() -> User.builder()
                .id("")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be empty");
    }

    @Test
    void shouldThrowWhenUserIdIsBlank() {
        assertThatThrownBy(() -> User.builder()
                .id("   ")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be empty");
    }

    /**
     * Tests the validation of maximum lengths for User attributes.
     */

    @Test
    void shouldThrowWhenUserIdExceedsMaxLength() {
        String longUserId = "usr-" + "a".repeat(252); // 256 chars total, exceeds 255 limit
        assertThatThrownBy(() -> User.builder()
                .id(longUserId)
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID exceeds maximum length");
    }

    @Test
    void shouldThrowWhenNameExceedsMaxLength() {
        String longName = "a".repeat(256); // Assuming 255 char limit
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name(longName)
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name exceeds maximum length");
    }

    @Test
    void shouldThrowWhenEmailExceedsMaxLength() {
        String longEmail = "a".repeat(250) + "@example.com"; // 262 chars total, exceeds 255 limit
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email(longEmail)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email exceeds maximum length");
    }

    @Test
    void shouldThrowWhenPhoneNumberExceedsMaxLength() {
        String longPhoneNumber = "+1" + "2".repeat(15); // 17 chars total, exceeds 16 char limit
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber(longPhoneNumber)
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number exceeds maximum length");
    }

    @Test
    void shouldAcceptMaxAllowedLengths() {
        String maxUserId = "usr-" + "a".repeat(250); // Just under limit
        String maxName = "a".repeat(255);
        String maxEmail = "a".repeat(243) + "@example.com"; // Just under limit
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

    /**
     * Edge Case Validation Tests.
     */
    @Test
    void shouldValidateUserIdWithNumbers() {
        User validUser = User.builder()
                .id("usr-123456")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        assertThat(validUser.getId()).isEqualTo("usr-123456");
    }

    @Test
    void shouldValidateUserIdWithMixedCase() {
        User validUser = User.builder()
                .id("usr-AbC123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        assertThat(validUser.getId()).isEqualTo("usr-AbC123");
    }

    @Test
    void shouldThrowWhenUserIdHasSpecialCharacters() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc@123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must match pattern");
    }

    @Test
    void shouldThrowWhenUserIdHasSpaces() {
        assertThatThrownBy(() -> User.builder()
                .id("usr abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must match pattern");
    }

    @Test
    void shouldAcceptUserIdAtExactMaxLength() {
        String maxLengthUserId = "usr-" + "a".repeat(251); // Exactly 255 chars
        User validUser = User.builder()
                .id(maxLengthUserId)
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();

        assertThat(validUser.getId()).isEqualTo(maxLengthUserId);
    }

    @Test
    void shouldAcceptNameAtExactMaxLength() {
        String maxLengthName = "a".repeat(255); // Exactly 255 chars
        User validUser = User.builder()
                .id("usr-abc123")
                .name(maxLengthName)
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();

        assertThat(validUser.getName()).isEqualTo(maxLengthName);
    }

    /**
     * Tests the validation of phone numbers.
     */
    @Test
    void shouldValidateMinimumPhoneNumber() {
        User validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+12")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
        
        assertThat(validUser.getPhoneNumber()).isEqualTo("+12");
    }

    @Test
    void shouldValidateMaximumPhoneNumber() {
        String maxPhone = "+1" + "2".repeat(14); // 15 digits total
        User validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber(maxPhone)
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
        
        assertThat(validUser.getPhoneNumber()).isEqualTo(maxPhone);
    }

    @Test
    void shouldThrowWhenPhoneNumberStartsWithZero() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+0123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number must be in international format");
    }

    @Test
    void shouldThrowWhenPhoneNumberTooLong() {
        String tooLongPhone = "+1234567890123456"; // 17 chars: + plus 16 digits (exceeds both 16 char limit and 15 digit regex limit)
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber(tooLongPhone)
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number exceeds maximum length");
    }

    @Test
    void shouldThrowWhenPhoneNumberMissingPlus() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("44123456789")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number must be in international format");
    }

    @Test
    void shouldThrowWhenPhoneNumberTooShort() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+1")
                .email("test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number must be in international format");
    }

    @Test
    void shouldValidatePhoneNumberWithCountryCode999() {
        User validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+999123456789")
                .email("test@example.com")
                .build();

        assertThat(validUser.getPhoneNumber()).isEqualTo("+999123456789");
    }

    /**
     * Tests the validation of email addresses with various formats.
     */
    @Test
    void shouldValidateEmailWithPlus() {
        User validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test+tag@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        assertThat(validUser.getEmail()).isEqualTo("test+tag@example.com");
    }

    @Test
    void shouldValidateEmailWithUnderscore() {
        User validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test_user@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        assertThat(validUser.getEmail()).isEqualTo("test_user@example.com");
    }

    @Test
    void shouldValidateEmailWithDot() {
        User validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test.user@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        assertThat(validUser.getEmail()).isEqualTo("test.user@example.com");
    }

    @Test
    void shouldThrowWhenEmailMissingAtSymbol() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("testexample.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");
    }

    @Test
    void shouldThrowWhenEmailMissingDomain() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");
    }

    @Test
    void shouldThrowWhenEmailHasInvalidTLD() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.x")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");
    }

    @Test
    void shouldValidateEmailWithNumbers() {
        User validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("user123@example123.com")
                .build();

        assertThat(validUser.getEmail()).isEqualTo("user123@example123.com");
    }

    @Test
    void shouldValidateEmailWithLongTLD() {
        User validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.museum")
                .build();

        assertThat(validUser.getEmail()).isEqualTo("test@example.museum");
    }

    @Test
    void shouldThrowWhenEmailStartsWithDot() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email(".test@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");
    }

    @Test
    void shouldThrowWhenEmailEndsWithDot() {
        assertThatThrownBy(() -> User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test.@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");
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
     * Tests the implementation of equals method in User class.
     * It checks if two User objects with the same attributes are considered equal.
     */
    @Test
    void shouldImplementEqualsCorrectly() {
        User user1 = User.builder()
                .id("usr-same")
                .name("Same User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("same@example.com")
                .build();

        User user2 = User.builder()
                .id("usr-same")
                .name("Same User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("same@example.com")
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
     * Tests the concurrent validation of User objects.
     * It checks if multiple threads can create User objects without any issues.
     * @throws InterruptedException
     */
    @Test
    void shouldHandleConcurrentValidation() throws InterruptedException {
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
     * Tests the setters for User attributes.
     * It checks if the setters correctly update the User attributes and validate them.
     */

    @Test
    void shouldThrowWhenSettingInvalidUserId() {
        assertThatThrownBy(() -> user.setId("invalid-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must match pattern");
    }

    @Test
    void shouldThrowWhenSettingNullName() {
        assertThatThrownBy(() -> user.setName(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");
    }

    @Test
    void shouldThrowWhenSettingInvalidEmail() {
        assertThatThrownBy(() -> user.setEmail("invalid-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");
    }

    @Test
    void shouldThrowWhenSettingInvalidPhoneNumber() {
        assertThatThrownBy(() -> user.setPhoneNumber("invalid-phone"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number must be in international format");
    }
} 