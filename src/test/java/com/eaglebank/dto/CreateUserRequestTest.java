package com.eaglebank.dto;

import com.eaglebank.domain.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateUserRequest Tests")
class CreateUserRequestTest {

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
    @DisplayName("Valid Object Creation Tests")
    class ValidObjectCreationTests {

        @Test
        @DisplayName("Should create valid CreateUserRequest with all required fields")
        void shouldCreateValidRequest() {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("john.doe@example.com")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("John Doe");
            assertThat(request.getAddress()).isEqualTo(validAddress);
            assertThat(request.getPhoneNumber()).isEqualTo("+1234567890");
            assertThat(request.getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should create valid request with minimal address")
        void shouldCreateValidRequestWithMinimalAddress() {
            // Given
            Address minimalAddress = Address.builder()
                    .line1("456 Oak Street")
                    .town("Manchester")
                    .county("Greater Manchester")
                    .postcode("M1 1AA")
                    .build();

            CreateUserRequest request = CreateUserRequest.builder()
                    .name("Jane Smith")
                    .address(minimalAddress)
                    .phoneNumber("+447123456789")
                    .email("jane.smith@test.co.uk")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should create request without address (address is optional)")
        void shouldCreateRequestWithoutAddress() {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("Bob Wilson")
                    .phoneNumber("+33123456789")
                    .email("bob.wilson@example.fr")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getAddress()).isNull();
        }
    }

    @Nested
    @DisplayName("Name Validation Tests")
    class NameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should reject null, empty or blank name")
        void shouldRejectInvalidName(String invalidName) {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name(invalidName)
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");
        }

        @Test
        @DisplayName("Should accept valid name")
        void shouldAcceptValidName() {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Phone Number Validation Tests")
    class PhoneNumberValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should reject null, empty or blank phone number")
        void shouldRejectBlankPhoneNumber(String invalidPhone) {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber(invalidPhone)
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then - Both @NotBlank and @Pattern violations are triggered for blank fields
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            boolean hasRequiredViolation = violations.stream()
                    .anyMatch(v -> v.getMessage().equals("Phone number is required"));
            assertThat(hasRequiredViolation).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1234567890",           // Missing +
                "+",                    // Just +
                "+0123456789",          // Starts with 0
                "123-456-7890",         // Contains dashes
                "(123) 456-7890",       // Contains parentheses
                "+123 456 7890",        // Contains spaces
                "+123456789012345678",  // Too long (>15 digits)
                "abc",                  // Non-numeric
                "+1a23456789"           // Contains letters
        })
        @DisplayName("Should reject invalid phone number formats")
        void shouldRejectInvalidPhoneFormats(String invalidPhone) {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber(invalidPhone)
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Phone number must be in international format");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "+1234567890",          // US format
                "+447123456789",        // UK format
                "+33123456789",         // France format
                "+86123456789",         // China format
                "+911234567890",        // India format
                "+12",                  // Minimal valid (2 digits)
                "+123456789012345"      // Maximum valid (14 digits after +)
        })
        @DisplayName("Should accept valid phone number formats")
        void shouldAcceptValidPhoneFormats(String validPhone) {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber(validPhone)
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should reject null, empty or blank email")
        void shouldRejectBlankEmail(String invalidEmail) {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email(invalidEmail)
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then - Both @NotBlank and @Email violations are triggered for blank fields
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            boolean hasRequiredViolation = violations.stream()
                    .anyMatch(v -> v.getMessage().equals("Email is required"));
            assertThat(hasRequiredViolation).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid-email",
                "@example.com",
                "user@",
                "user..name@example.com",
                "user name@example.com",
                "user@ex ample.com"
        })
        @DisplayName("Should reject invalid email formats")
        void shouldRejectInvalidEmailFormats(String invalidEmail) {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email(invalidEmail)
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be in valid format");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "test@example.com",
                "user.name@example.co.uk",
                "firstname+lastname@domain.org",
                "email@subdomain.example.com",
                "firstname-lastname@example.com",
                "user123@example123.com"
        })
        @DisplayName("Should accept valid email formats")
        void shouldAcceptValidEmailFormats(String validEmail) {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email(validEmail)
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Address Validation Tests")
    class AddressValidationTests {

        @Test
        @DisplayName("Should validate nested address object when address is provided")
        void shouldValidateNestedAddress() {
            // Given - Invalid address (missing required fields)
            Address invalidAddress = Address.builder()
                    .line1("")  // Empty line1 should fail validation
                    .build();

            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(invalidAddress)
                    .phoneNumber("+1234567890")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            // Check that the violation is from the nested address validation
            boolean hasAddressViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().startsWith("address"));
            assertThat(hasAddressViolation).isTrue();
        }

        @Test
        @DisplayName("Should pass validation with valid nested address")
        void shouldPassValidationWithValidAddress() {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("test@example.com")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create object using builder pattern")
        void shouldCreateObjectUsingBuilder() {
            // Given & When
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("Test User")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("test@example.com")
                    .build();

            // Then
            assertThat(request).isNotNull();
            assertThat(request.getName()).isEqualTo("Test User");
            assertThat(request.getAddress()).isEqualTo(validAddress);
            assertThat(request.getPhoneNumber()).isEqualTo("+1234567890");
            assertThat(request.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should create object using no-args constructor")
        void shouldCreateObjectUsingNoArgsConstructor() {
            // Given & When
            CreateUserRequest request = new CreateUserRequest();

            // Then
            assertThat(request).isNotNull();
            assertThat(request.getName()).isNull();
            assertThat(request.getAddress()).isNull();
            assertThat(request.getPhoneNumber()).isNull();
            assertThat(request.getEmail()).isNull();
        }

        @Test
        @DisplayName("Should create object using all-args constructor")
        void shouldCreateObjectUsingAllArgsConstructor() {
            // Given & When
            CreateUserRequest request = new CreateUserRequest(
                    "Test User",
                    validAddress,
                    "+1234567890",
                    "test@example.com"
            );

            // Then
            assertThat(request).isNotNull();
            assertThat(request.getName()).isEqualTo("Test User");
            assertThat(request.getAddress()).isEqualTo(validAddress);
            assertThat(request.getPhoneNumber()).isEqualTo("+1234567890");
            assertThat(request.getEmail()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            // Given
            CreateUserRequest request1 = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("john@example.com")
                    .build();

            CreateUserRequest request2 = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("john@example.com")
                    .build();

            // Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            // Given
            CreateUserRequest request1 = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("john@example.com")
                    .build();

            CreateUserRequest request2 = CreateUserRequest.builder()
                    .name("Jane Doe")  // Different name
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("john@example.com")
                    .build();

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToString() {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("john@example.com")
                    .build();

            // When
            String toString = request.toString();

            // Then
            assertThat(toString).contains("John Doe");
            assertThat(toString).contains("+1234567890");
            assertThat(toString).contains("john@example.com");
            assertThat(toString).contains("CreateUserRequest");
        }
    }

    @Nested
    @DisplayName("Multiple Validation Errors Tests")
    class MultipleValidationErrorsTests {

        @Test
        @DisplayName("Should report multiple validation errors when multiple fields are invalid")
        void shouldReportMultipleValidationErrors() {
            // Given
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("")  // Invalid: blank
                    .address(validAddress)
                    .phoneNumber("invalid-phone")  // Invalid: wrong format
                    .email("invalid-email")  // Invalid: wrong format
                    .build();

            // When
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(3);
            
            Set<String> violationMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(java.util.stream.Collectors.toSet());
            
            assertThat(violationMessages).containsExactlyInAnyOrder(
                    "Name is required",
                    "Phone number must be in international format",
                    "Email must be in valid format"
            );
        }
    }
}
