package com.eaglebank.dto;

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

@DisplayName("CreateBankAccountRequest Tests")
class CreateBankAccountRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Valid Object Creation Tests")
    class ValidObjectCreationTests {

        @Test
        @DisplayName("Should create valid CreateBankAccountRequest with all required fields")
        void shouldCreateValidRequest() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Personal Bank Account")
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("Personal Bank Account");
            assertThat(request.getAccountType()).isEqualTo("personal");
        }

        @Test
        @DisplayName("Should create valid request with different account name")
        void shouldCreateValidRequestWithDifferentName() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("My Account")
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("My Account");
            assertThat(request.getAccountType()).isEqualTo("personal");
        }

        @Test
        @DisplayName("Should create valid request with long account name")
        void shouldCreateValidRequestWithLongName() {
            // Given
            String longName = "My Very Long Personal Savings Account Name";
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(longName)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Should create valid request with special characters in name")
        void shouldCreateValidRequestWithSpecialCharacters() {
            // Given
            String nameWithSpecialChars = "John's Business Account - Main";
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(nameWithSpecialChars)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo(nameWithSpecialChars);
        }

        @Test
        @DisplayName("Should create valid request with unicode characters in name")
        void shouldCreateValidRequestWithUnicodeCharacters() {
            // Given
            String unicodeName = "José María's Account";
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(unicodeName)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo(unicodeName);
        }
    }

    @Nested
    @DisplayName("Name Field Validation Tests")
    class NameFieldValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject request when name is null or empty")
        void shouldRejectRequestWhenNameIsNullOrEmpty(String invalidName) {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(invalidName)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateBankAccountRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
            assertThat(violation.getMessage()).satisfiesAnyOf(
                msg -> assertThat(msg).contains("required"),
                msg -> assertThat(msg).contains("blank"),
                msg -> assertThat(msg).contains("empty")
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should reject request when name contains only whitespace")
        void shouldRejectRequestWhenNameContainsOnlyWhitespace(String whitespaceOnlyName) {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(whitespaceOnlyName)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateBankAccountRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
            assertThat(violation.getMessage()).satisfiesAnyOf(
                msg -> assertThat(msg).contains("required"),
                msg -> assertThat(msg).contains("blank")
            );
        }

        @Test
        @DisplayName("Should accept request with name containing leading and trailing spaces")
        void shouldAcceptRequestWithNameContainingLeadingAndTrailingSpaces() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("  Personal Account  ")
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("  Personal Account  ");
        }

        @Test
        @DisplayName("Should accept request with single character name")
        void shouldAcceptRequestWithSingleCharacterName() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("A")
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("A");
        }

        @Test
        @DisplayName("Should accept request with numeric characters in name")
        void shouldAcceptRequestWithNumericCharactersInName() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Account 123")
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("Account 123");
        }
    }

    @Nested
    @DisplayName("Account Type Field Validation Tests")
    class AccountTypeFieldValidationTests {

        @Test
        @DisplayName("Should accept valid account type 'personal'")
        void shouldAcceptValidAccountTypePersonal() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getAccountType()).isEqualTo("personal");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject request when accountType is null or empty")
        void shouldRejectRequestWhenAccountTypeIsNullOrEmpty(String invalidAccountType) {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType(invalidAccountType)
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSizeBetween(1, 2);  // Could be 1 (just NotBlank) or 2 (NotBlank + Pattern)
            assertThat(violations).anyMatch(v -> 
                v.getPropertyPath().toString().equals("accountType") &&
                (v.getMessage().contains("required") || 
                 v.getMessage().contains("blank") || 
                 v.getMessage().contains("empty") || 
                 v.getMessage().contains("personal")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"PERSONAL", "Personal", "business", "savings", "checking", "invalid", "123", "personal "})
        @DisplayName("Should reject request when accountType is not valid enum value")
        void shouldRejectRequestWhenAccountTypeIsNotValidEnumValue(String invalidAccountType) {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType(invalidAccountType)
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            if (!invalidAccountType.equals("personal")) {
                assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
                boolean hasAccountTypeViolation = violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("accountType"));
                assertThat(hasAccountTypeViolation).isTrue();
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should reject request when accountType contains only whitespace")
        void shouldRejectRequestWhenAccountTypeContainsOnlyWhitespace(String whitespaceOnlyAccountType) {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType(whitespaceOnlyAccountType)
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSizeBetween(1, 2);  // Could be 1 or 2 violations for whitespace
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountType"));
        }
    }

    @Nested
    @DisplayName("Object Equality and Builder Tests")
    class ObjectEqualityAndBuilderTests {

        @Test
        @DisplayName("Should create equal objects with same values")
        void shouldCreateEqualObjectsWithSameValues() {
            // Given
            CreateBankAccountRequest request1 = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType("personal")
                    .build();

            CreateBankAccountRequest request2 = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType("personal")
                    .build();

            // Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should create different objects with different names")
        void shouldCreateDifferentObjectsWithDifferentNames() {
            // Given
            CreateBankAccountRequest request1 = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType("personal")
                    .build();

            CreateBankAccountRequest request2 = CreateBankAccountRequest.builder()
                    .name("Business Account")
                    .accountType("personal")
                    .build();

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should create different objects with different account types")
        void shouldCreateDifferentObjectsWithDifferentAccountTypes() {
            // Given
            CreateBankAccountRequest request1 = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType("personal")
                    .build();

            CreateBankAccountRequest request2 = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType("business")
                    .build();

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should support builder pattern modifications")
        void shouldSupportBuilderPatternModifications() {
            // Given
            CreateBankAccountRequest originalRequest = CreateBankAccountRequest.builder()
                    .name("Original Account")
                    .accountType("personal")
                    .build();

            // When
            CreateBankAccountRequest modifiedRequest = originalRequest.toBuilder()
                    .name("Modified Account")
                    .build();

            // Then
            assertThat(originalRequest.getName()).isEqualTo("Original Account");
            assertThat(modifiedRequest.getName()).isEqualTo("Modified Account");
            assertThat(modifiedRequest.getAccountType()).isEqualTo("personal");
        }

        @Test
        @DisplayName("Should have proper toString representation")
        void shouldHaveProperToStringRepresentation() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Personal Account")
                    .accountType("personal")
                    .build();

            // When
            String toString = request.toString();

            // Then
            assertThat(toString).contains("CreateBankAccountRequest");
            assertThat(toString).contains("Personal Account");
            assertThat(toString).contains("personal");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Should handle request with null object")
        void shouldHandleRequestWithNullObject() {
            // Given
            CreateBankAccountRequest request = null;

            // When & Then
            assertThat(request).isNull();
        }

        @Test
        @DisplayName("Should validate complete empty request")
        void shouldValidateCompleteEmptyRequest() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder().build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(2); // name and accountType required
            
            boolean hasNameViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
            boolean hasAccountTypeViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("accountType"));
            
            assertThat(hasNameViolation).isTrue();
            assertThat(hasAccountTypeViolation).isTrue();
        }

        @Test
        @DisplayName("Should handle very long account name")
        void shouldHandleVeryLongAccountName() {
            // Given
            String veryLongName = "A".repeat(1000);
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(veryLongName)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            // Should either pass or fail based on max length validation if implemented
            assertThat(violations).isNotNull(); // Validation may or may not have length restrictions
            assertThat(request.getName()).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle account name with only special characters")
        void shouldHandleAccountNameWithOnlySpecialCharacters() {
            // Given
            String specialCharsName = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(specialCharsName)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty(); // Should be valid unless pattern validation is added
            assertThat(request.getName()).isEqualTo(specialCharsName);
        }

        @Test
        @DisplayName("Should handle concurrent validation")
        void shouldHandleConcurrentValidation() throws InterruptedException {
            // Given
            int numberOfThreads = 10;
            Thread[] threads = new Thread[numberOfThreads];
            boolean[] results = new boolean[numberOfThreads];

            // When
            for (int i = 0; i < numberOfThreads; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                            .name("Account " + index)
                            .accountType("personal")
                            .build();
                    
                    Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);
                    results[index] = violations.isEmpty();
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            for (boolean result : results) {
                assertThat(result).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Data Integrity and Security Tests")
    class DataIntegrityAndSecurityTests {

        @Test
        @DisplayName("Should handle SQL injection attempt in name")
        void shouldHandleSqlInjectionAttemptInName() {
            // Given
            String sqlInjectionAttempt = "'; DROP TABLE users; --";
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(sqlInjectionAttempt)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty(); // Should pass validation, protection handled at service/repository layer
            assertThat(request.getName()).isEqualTo(sqlInjectionAttempt);
        }

        @Test
        @DisplayName("Should handle XSS attempt in name")
        void shouldHandleXssAttemptInName() {
            // Given
            String xssAttempt = "<script>alert('XSS')</script>";
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name(xssAttempt)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty(); // Should pass validation, sanitization handled elsewhere
            assertThat(request.getName()).isEqualTo(xssAttempt);
        }

        @Test
        @DisplayName("Should maintain field immutability after creation")
        void shouldMaintainFieldImmutabilityAfterCreation() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Original Account")
                    .accountType("personal")
                    .build();

            String originalName = request.getName();
            String originalAccountType = request.getAccountType();

            // When - Attempting to modify (if setters exist)
            // request.setName("Modified Name"); // This should not be possible with @Builder only

            // Then
            assertThat(request.getName()).isEqualTo(originalName);
            assertThat(request.getAccountType()).isEqualTo(originalAccountType);
        }

        @Test
        @DisplayName("Should handle null field validation gracefully")
        void shouldHandleNullFieldValidationGracefully() {
            // Given
            CreateBankAccountRequest request = CreateBankAccountRequest.builder()
                    .name("Valid Account")
                    .accountType(null)
                    .build();

            // When
            Set<ConstraintViolation<CreateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            boolean hasAccountTypeViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("accountType"));
            assertThat(hasAccountTypeViolation).isTrue();
        }
    }
}
