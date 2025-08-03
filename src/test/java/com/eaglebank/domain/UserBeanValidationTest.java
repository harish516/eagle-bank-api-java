package com.eaglebank.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Bean Validation Tests")
class UserBeanValidationTest {

    private Validator validator;
    private Address validAddress;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        validAddress = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();
    }

    @Nested
    @DisplayName("User ID Validation")
    class UserIdValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when user ID is null, empty or blank")
        void shouldFailValidationWhenUserIdIsNullEmptyOrBlank(String invalidId) {
            // Given
            User user = User.builder()
                    .id(invalidId)
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("id"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("User ID is required"));
        }

        @Test
        @DisplayName("Should fail validation when user ID exceeds maximum length")
        void shouldFailValidationWhenUserIdExceedsMaxLength() {
            // Given
            String longId = "usr-" + "a".repeat(260); // Exceeds 255 character limit
            User user = User.builder()
                    .id(longId)
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("id"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("User ID exceeds maximum length"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-id", "usr-", "usr-@invalid", "usr-with spaces", "abc-123"})
        @DisplayName("Should fail validation when user ID doesn't match pattern")
        void shouldFailValidationWhenUserIdDoesNotMatchPattern(String invalidId) {
            // Given
            User user = User.builder()
                    .id(invalidId)
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("id"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("User ID must match pattern"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"usr-abc123", "usr-123", "usr-ABC123", "usr-a1B2c3"})
        @DisplayName("Should pass validation with valid user ID")
        void shouldPassValidationWithValidUserId(String validId) {
            // Given
            User user = User.builder()
                    .id(validId)
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Name Validation")
    class NameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when name is null, empty or blank")
        void shouldFailValidationWhenNameIsNullEmptyOrBlank(String invalidName) {
            // Given
            User user = User.builder()
                    .id("usr-123")
                    .name(invalidName)
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Name is required"));
        }

        @Test
        @DisplayName("Should fail validation when name exceeds maximum length")
        void shouldFailValidationWhenNameExceedsMaxLength() {
            // Given
            String longName = "a".repeat(260); // Exceeds 255 character limit
            User user = User.builder()
                    .id("usr-123")
                    .name(longName)
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Name exceeds maximum length"));
        }
    }

    @Nested
    @DisplayName("Phone Number Validation")
    class PhoneNumberValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when phone number is null, empty or blank")
        void shouldFailValidationWhenPhoneNumberIsNullEmptyOrBlank(String invalidPhone) {
            // Given
            User user = User.builder()
                    .id("usr-123")
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber(invalidPhone)
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Phone number is required"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"1234567890", "+0123456789", "invalid-phone", "+1", "+12345678901234567"})
        @DisplayName("Should fail validation when phone number doesn't match international format")
        void shouldFailValidationWhenPhoneNumberDoesNotMatchFormat(String invalidPhone) {
            // Given
            User user = User.builder()
                    .id("usr-123")
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber(invalidPhone)
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber"));
        }

        @Test
        @DisplayName("Should fail validation when phone number exceeds maximum length")
        void shouldFailValidationWhenPhoneNumberExceedsMaxLength() {
            // Given
            String longPhone = "+1" + "2".repeat(20); // Exceeds 16 character limit
            User user = User.builder()
                    .id("usr-123")
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber(longPhone)
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Phone number exceeds maximum length"));
        }
    }

    @Nested
    @DisplayName("Email Validation")
    class EmailValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when email is null, empty or blank")
        void shouldFailValidationWhenEmailIsNullEmptyOrBlank(String invalidEmail) {
            // Given
            User user = User.builder()
                    .id("usr-123")
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email(invalidEmail)
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "@example.com", "user@", "user..name@example.com"})
        @DisplayName("Should fail validation when email format is invalid")
        void shouldFailValidationWhenEmailFormatIsInvalid(String invalidEmail) {
            // Given
            User user = User.builder()
                    .id("usr-123")
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email(invalidEmail)
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Email must be in valid format"));
        }

        @Test
        @DisplayName("Should fail validation when email exceeds maximum length")
        void shouldFailValidationWhenEmailExceedsMaxLength() {
            // Given
            String longEmail = "a".repeat(250) + "@example.com"; // Exceeds 255 character limit
            User user = User.builder()
                    .id("usr-123")
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber("+44123456789")
                    .email(longEmail)
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Email exceeds maximum length"));
        }
    }

    @Nested
    @DisplayName("Address Validation")
    class AddressValidationTests {

        @Test
        @DisplayName("Should fail validation when address is null")
        void shouldFailValidationWhenAddressIsNull() {
            // Given
            User user = User.builder()
                    .id("usr-123")
                    .name("Test User")
                    .address(null)
                    .phoneNumber("+44123456789")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("address"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Address is required"));
        }

        @Test
        @DisplayName("Should cascade validation to address fields")
        void shouldCascadeValidationToAddressFields() {
            // Given
            Address invalidAddress = Address.builder()
                    .line1("") // Invalid - blank
                    .town("London")
                    .county("Greater London")
                    .postcode("SW1A 1AA")
                    .build();

            User user = User.builder()
                    .id("usr-123")
                    .name("Test User")
                    .address(invalidAddress)
                    .phoneNumber("+44123456789")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("address.line1"));
        }
    }

    @Test
    @DisplayName("Should pass validation with all valid fields")
    void shouldPassValidationWithAllValidFields() {
        // Given
        User user = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(validAddress)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }
}
