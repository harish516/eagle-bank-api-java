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

@DisplayName("UpdateUserRequest Tests")
class UpdateUserRequestTest {

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
    @DisplayName("Object Creation Tests")
    class ObjectCreationTests {

        @Test
        @DisplayName("Should create UpdateUserRequest using builder with all fields")
        void shouldCreateWithBuilder() {
            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("john.doe@example.com")
                    .build();

            // Then
            assertThat(request).isNotNull();
            assertThat(request.getName()).isEqualTo("John Doe");
            assertThat(request.getAddress()).isEqualTo(validAddress);
            assertThat(request.getPhoneNumber()).isEqualTo("+1234567890");
            assertThat(request.getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should create UpdateUserRequest using all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // When
            UpdateUserRequest request = new UpdateUserRequest(
                    "Jane Smith",
                    validAddress,
                    "+44123456789",
                    "jane.smith@test.com"
            );

            // Then
            assertThat(request).isNotNull();
            assertThat(request.getName()).isEqualTo("Jane Smith");
            assertThat(request.getAddress()).isEqualTo(validAddress);
            assertThat(request.getPhoneNumber()).isEqualTo("+44123456789");
            assertThat(request.getEmail()).isEqualTo("jane.smith@test.com");
        }

        @Test
        @DisplayName("Should create UpdateUserRequest using no-args constructor")
        void shouldCreateWithNoArgsConstructor() {
            // When
            UpdateUserRequest request = new UpdateUserRequest();

            // Then
            assertThat(request).isNotNull();
            assertThat(request.getName()).isNull();
            assertThat(request.getAddress()).isNull();
            assertThat(request.getPhoneNumber()).isNull();
            assertThat(request.getEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("Single Field Update Tests")
    class SingleFieldUpdateTests {

        @Test
        @DisplayName("Should allow updating only name")
        void shouldAllowUpdatingOnlyName() {
            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("John Updated")
                    .build();

            // Then
            assertThat(request.getName()).isEqualTo("John Updated");
            assertThat(request.getAddress()).isNull();
            assertThat(request.getPhoneNumber()).isNull();
            assertThat(request.getEmail()).isNull();

            // Validation should pass
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should allow updating only email")
        void shouldAllowUpdatingOnlyEmail() {
            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email("newemail@example.com")
                    .build();

            // Then
            assertThat(request.getName()).isNull();
            assertThat(request.getAddress()).isNull();
            assertThat(request.getPhoneNumber()).isNull();
            assertThat(request.getEmail()).isEqualTo("newemail@example.com");

            // Validation should pass
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should allow updating only phone number")
        void shouldAllowUpdatingOnlyPhoneNumber() {
            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .phoneNumber("+9876543210")
                    .build();

            // Then
            assertThat(request.getName()).isNull();
            assertThat(request.getAddress()).isNull();
            assertThat(request.getPhoneNumber()).isEqualTo("+9876543210");
            assertThat(request.getEmail()).isNull();

            // Validation should pass
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should allow updating only address")
        void shouldAllowUpdatingOnlyAddress() {
            // Given
            Address newAddress = Address.builder()
                    .line1("456 New Street")
                    .town("Manchester")
                    .county("Greater Manchester")
                    .postcode("M1 1AA")
                    .build();

            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .address(newAddress)
                    .build();

            // Then
            assertThat(request.getName()).isNull();
            assertThat(request.getAddress()).isEqualTo(newAddress);
            assertThat(request.getPhoneNumber()).isNull();
            assertThat(request.getEmail()).isNull();

            // Validation should pass
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Field Update Tests")
    class MultipleFieldUpdateTests {

        @Test
        @DisplayName("Should allow updating name and email together")
        void shouldAllowUpdatingNameAndEmail() {
            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("John Updated")
                    .email("john.updated@example.com")
                    .build();

            // Then
            assertThat(request.getName()).isEqualTo("John Updated");
            assertThat(request.getEmail()).isEqualTo("john.updated@example.com");
            assertThat(request.getAddress()).isNull();
            assertThat(request.getPhoneNumber()).isNull();

            // Validation should pass
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should allow updating phone and address together")
        void shouldAllowUpdatingPhoneAndAddress() {
            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .phoneNumber("+441234567890")
                    .address(validAddress)
                    .build();

            // Then
            assertThat(request.getPhoneNumber()).isEqualTo("+441234567890");
            assertThat(request.getAddress()).isEqualTo(validAddress);
            assertThat(request.getName()).isNull();
            assertThat(request.getEmail()).isNull();

            // Validation should pass
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should allow updating three fields together")
        void shouldAllowUpdatingThreeFields() {
            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("Jane Updated")
                    .phoneNumber("+33123456789")
                    .email("jane.updated@company.com")
                    .build();

            // Then
            assertThat(request.getName()).isEqualTo("Jane Updated");
            assertThat(request.getPhoneNumber()).isEqualTo("+33123456789");
            assertThat(request.getEmail()).isEqualTo("jane.updated@company.com");
            assertThat(request.getAddress()).isNull();

            // Validation should pass
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Empty Update Request Tests")
    class EmptyUpdateRequestTests {

        @Test
        @DisplayName("Should allow empty update request (no fields to update)")
        void shouldAllowEmptyUpdateRequest() {
            // When
            UpdateUserRequest request = UpdateUserRequest.builder().build();

            // Then
            assertThat(request.getName()).isNull();
            assertThat(request.getAddress()).isNull();
            assertThat(request.getPhoneNumber()).isNull();
            assertThat(request.getEmail()).isNull();

            // Validation should pass - empty update is valid
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should allow update request with all null fields")
        void shouldAllowAllNullFields() {
            // When
            UpdateUserRequest request = new UpdateUserRequest(null, null, null, null);

            // Then
            assertThat(request.getName()).isNull();
            assertThat(request.getAddress()).isNull();
            assertThat(request.getPhoneNumber()).isNull();
            assertThat(request.getEmail()).isNull();

            // Validation should pass
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Phone Number Validation Tests")
    class PhoneNumberValidationTests {

        @ParameterizedTest
        @DisplayName("Should accept valid international phone numbers when updating phone")
        @ValueSource(strings = {
                "+1234567890",
                "+44123456789",
                "+33987654321",
                "+49123456789",
                "+861234567890123",
                "+12345678901234"
        })
        void shouldAcceptValidPhoneNumbers(String phoneNumber) {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .phoneNumber(phoneNumber)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @DisplayName("Should reject invalid phone number formats when updating phone")
        @ValueSource(strings = {
                "1234567890",        // Missing +
                "+0123456789",       // Starts with 0 after +
                "++44123456789",     // Double +
                "+44 123 456 789",   // Contains spaces
                "+44-123-456-789",   // Contains hyphens
                "+44(123)456789",    // Contains parentheses
                "+",                 // Only +
                "+abc123456789",     // Contains letters
                "+44123456789012345", // Too long (>15 digits)
                "+4"                 // Too short
        })
        void shouldRejectInvalidPhoneNumbers(String phoneNumber) {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .phoneNumber(phoneNumber)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Phone number must be in international format");
        }

        @ParameterizedTest
        @DisplayName("Should reject null, empty or blank phone number when provided")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        void shouldRejectBlankPhoneNumber(String phoneNumber) {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .phoneNumber(phoneNumber)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            if (phoneNumber == null) {
                // Null is allowed (field is optional)
                assertThat(violations).isEmpty();
            } else {
                // Empty or blank strings should be rejected
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage())
                        .isEqualTo("Phone number must be in international format");
            }
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @ParameterizedTest
        @DisplayName("Should accept valid email formats when updating email")
        @ValueSource(strings = {
                "test@example.com",
                "user.name@domain.co.uk",
                "firstname+lastname@company.org",
                "user123@test-domain.com",
                "a@b.co",
                "user@sub.domain.example.com",
                "test.email+tag@gmail.com"
        })
        void shouldAcceptValidEmails(String email) {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email(email)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @DisplayName("Should reject invalid email formats when updating email")
        @ValueSource(strings = {
                "notanemail",
                "@domain.com",
                "user@",
                "user name@domain.com",
                "user@domain .com",
                "user..name@domain.com",
                ".user@domain.com",
                "user.@domain.com",
                "@"
        })
        void shouldRejectInvalidEmails(String email) {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email(email)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Email must be in valid format");
        }

        @Test
        @DisplayName("Should allow null email (optional field for updates)")
        void shouldAllowNullEmail() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email(null)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Address Validation Tests")
    class AddressValidationTests {

        @Test
        @DisplayName("Should accept valid address when updating address")
        void shouldAcceptValidAddress() {
            // Given
            Address completeAddress = Address.builder()
                    .line1("123 Complete Street")
                    .line2("Apartment 4B")
                    .town("London")
                    .county("Greater London")
                    .postcode("SW1A 1AA")
                    .build();

            UpdateUserRequest request = UpdateUserRequest.builder()
                    .address(completeAddress)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getAddress()).isEqualTo(completeAddress);
        }

        @Test
        @DisplayName("Should validate nested address fields when updating address")
        void shouldValidateNestedAddressFields() {
            // Given - Invalid address (missing required fields)
            Address invalidAddress = Address.builder()
                    .line1("")  // Empty line1 should fail validation
                    .build();

            UpdateUserRequest request = UpdateUserRequest.builder()
                    .address(invalidAddress)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            // Should contain violations from the Address validation
        }

        @Test
        @DisplayName("Should allow null address when not updating address")
        void shouldAllowNullAddress() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .address(null)
                    .name("Update only name")
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Combined Validation Tests")
    class CombinedValidationTests {

        @Test
        @DisplayName("Should validate all provided fields when multiple fields are updated")
        void shouldValidateAllProvidedFields() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("Valid Name")
                    .email("valid@example.com")
                    .phoneNumber("+1234567890")
                    .address(validAddress)
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should collect all validation errors when multiple fields are invalid")
        void shouldCollectAllValidationErrors() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .phoneNumber("invalid-phone")
                    .email("invalid-email")
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Phone number must be in international format",
                            "Email must be in valid format"
                    );
        }

        @Test
        @DisplayName("Should validate only provided fields, ignoring null fields")
        void shouldValidateOnlyProvidedFields() {
            // Given - Only email is invalid, others are null
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name(null)  // Should be ignored
                    .phoneNumber(null)  // Should be ignored
                    .email("invalid-email")  // Should be validated and fail
                    .address(null)  // Should be ignored
                    .build();

            // When
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Email must be in valid format");
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should support fluent builder pattern")
        void shouldSupportFluentBuilderPattern() {
            // Given & When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .phoneNumber("+1234567890")
                    .address(validAddress)
                    .build();

            // Then
            assertThat(request).isNotNull();
            assertThat(request.getName()).isEqualTo("Test User");
            assertThat(request.getEmail()).isEqualTo("test@example.com");
            assertThat(request.getPhoneNumber()).isEqualTo("+1234567890");
            assertThat(request.getAddress()).isEqualTo(validAddress);
        }

        @Test
        @DisplayName("Should allow builder chaining in any order")
        void shouldAllowBuilderChainingInAnyOrder() {
            // When - Different order of field setting
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email("test@example.com")
                    .address(validAddress)
                    .name("Test User")
                    .phoneNumber("+1234567890")
                    .build();

            // Then
            assertThat(request.getName()).isEqualTo("Test User");
            assertThat(request.getEmail()).isEqualTo("test@example.com");
            assertThat(request.getPhoneNumber()).isEqualTo("+1234567890");
            assertThat(request.getAddress()).isEqualTo(validAddress);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            // Given
            UpdateUserRequest request1 = UpdateUserRequest.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .phoneNumber("+1234567890")
                    .address(validAddress)
                    .build();

            UpdateUserRequest request2 = UpdateUserRequest.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .phoneNumber("+1234567890")
                    .address(validAddress)
                    .build();

            // Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            // Given
            UpdateUserRequest request1 = UpdateUserRequest.builder()
                    .name("John Doe")
                    .build();

            UpdateUserRequest request2 = UpdateUserRequest.builder()
                    .name("Jane Doe")
                    .build();

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should handle null fields in equals comparison")
        void shouldHandleNullFieldsInEquals() {
            // Given
            UpdateUserRequest request1 = UpdateUserRequest.builder().build();  // All nulls
            UpdateUserRequest request2 = UpdateUserRequest.builder().build();  // All nulls
            UpdateUserRequest request3 = UpdateUserRequest.builder()
                    .name("John")
                    .build();

            // Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1).isNotEqualTo(request3);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString with all fields")
        void shouldGenerateMeaningfulToStringWithAllFields() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .phoneNumber("+1234567890")
                    .address(validAddress)
                    .build();

            // When
            String toString = request.toString();

            // Then
            assertThat(toString).contains("UpdateUserRequest");
            assertThat(toString).contains("John Doe");
            assertThat(toString).contains("john@example.com");
            assertThat(toString).contains("+1234567890");
        }

        @Test
        @DisplayName("Should handle null fields in toString")
        void shouldHandleNullFieldsInToString() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("John")
                    .build();  // Other fields are null

            // When
            String toString = request.toString();

            // Then
            assertThat(toString).contains("UpdateUserRequest");
            assertThat(toString).contains("John");
            assertThat(toString).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long name when updating")
        void shouldHandleVeryLongName() {
            // Given
            String longName = "A".repeat(1000);

            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name(longName)
                    .build();

            // Then
            assertThat(request.getName()).hasSize(1000);
            // Should pass validation (no length constraint on name)
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in name")
        void shouldHandleSpecialCharactersInName() {
            // Given
            String nameWithSpecialChars = "José María O'Connor-Smith 中文 🌟";

            // When
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name(nameWithSpecialChars)
                    .build();

            // Then
            assertThat(request.getName()).isEqualTo(nameWithSpecialChars);
            Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle international phone numbers")
        void shouldHandleInternationalPhoneNumbers() {
            // Given
            String[] internationalNumbers = {
                    "+1234567890",      // US
                    "+441234567890",    // UK
                    "+33123456789",     // France
                    "+49123456789",     // Germany
                    "+81123456789",     // Japan
                    "+861234567890"     // China
            };

            for (String phoneNumber : internationalNumbers) {
                // When
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .phoneNumber(phoneNumber)
                        .build();

                // Then
                Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
                assertThat(violations)
                        .as("Phone number %s should be valid", phoneNumber)
                        .isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle complex email addresses")
        void shouldHandleComplexEmailAddresses() {
            // Given
            String[] complexEmails = {
                    "test.email+tag@example.com",
                    "user123@sub.domain.example.org",
                    "firstname.lastname@company-name.co.uk",
                    "a@b.co"
            };

            for (String email : complexEmails) {
                // When
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .email(email)
                        .build();

                // Then
                Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
                assertThat(violations)
                        .as("Email %s should be valid", email)
                        .isEmpty();
            }
        }
    }
}
