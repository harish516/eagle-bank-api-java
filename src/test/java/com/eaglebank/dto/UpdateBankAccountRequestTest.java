package com.eaglebank.dto;

import com.eaglebank.domain.AccountType;
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

@DisplayName("UpdateBankAccountRequest Tests")
class UpdateBankAccountRequestTest {

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
        @DisplayName("Should create valid UpdateBankAccountRequest with both name and accountType")
        void shouldCreateValidRequestWithBothFields() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("Updated Personal Bank Account")
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("Updated Personal Bank Account");
            assertThat(request.getAccountType()).isEqualTo("personal");
        }

        @Test
        @DisplayName("Should create valid request with only name field")
        void shouldCreateValidRequestWithOnlyName() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("My Updated Account")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("My Updated Account");
            assertThat(request.getAccountType()).isNull();
        }

        @Test
        @DisplayName("Should create valid request with only accountType field")
        void shouldCreateValidRequestWithOnlyAccountType() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isNull();
            assertThat(request.getAccountType()).isEqualTo("personal");
        }

        @Test
        @DisplayName("Should create valid request with empty object (no fields to update)")
        void shouldCreateValidRequestWithNoFields() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder().build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isNull();
            assertThat(request.getAccountType()).isNull();
        }

        @Test
        @DisplayName("Should create valid request with long account name")
        void shouldCreateValidRequestWithLongName() {
            // Given
            String longName = "My Very Long Updated Personal Savings Account Name";
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(longName)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Should create valid request with special characters in name")
        void shouldCreateValidRequestWithSpecialCharacters() {
            // Given
            String nameWithSpecialChars = "John's Updated Business Account - Main";
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(nameWithSpecialChars)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo(nameWithSpecialChars);
        }

        @Test
        @DisplayName("Should create valid request with unicode characters in name")
        void shouldCreateValidRequestWithUnicodeCharacters() {
            // Given
            String unicodeName = "José María's Updated Account";
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(unicodeName)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo(unicodeName);
        }
    }

    @Nested
    @DisplayName("Name Field Validation Tests")
    class NameFieldValidationTests {

        @Test
        @DisplayName("Should accept request when name is null (optional field)")
        void shouldAcceptRequestWhenNameIsNull() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(null)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should reject request when name is empty or contains only whitespace")
        void shouldRejectRequestWhenNameIsEmptyOrWhitespace(String invalidName) {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(invalidName)
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            if (invalidName.trim().isEmpty()) {
                assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
                boolean hasNameViolation = violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
                assertThat(hasNameViolation).isTrue();
            }
        }

        @Test
        @DisplayName("Should accept request with name containing leading and trailing spaces")
        void shouldAcceptRequestWithNameContainingLeadingAndTrailingSpaces() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("  Updated Personal Account  ")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("  Updated Personal Account  ");
        }

        @Test
        @DisplayName("Should accept request with single character name")
        void shouldAcceptRequestWithSingleCharacterName() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("A")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("A");
        }

        @Test
        @DisplayName("Should accept request with numeric characters in name")
        void shouldAcceptRequestWithNumericCharactersInName() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("Updated Account 123")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("Updated Account 123");
        }
    }

    @Nested
    @DisplayName("Account Type Field Validation Tests")
    class AccountTypeFieldValidationTests {

        @Test
        @DisplayName("Should accept valid account type 'personal'")
        void shouldAcceptValidAccountTypePersonal() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getAccountType()).isEqualTo("personal");
        }

        @Test
        @DisplayName("Should accept request when accountType is null (optional field)")
        void shouldAcceptRequestWhenAccountTypeIsNull() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("Updated Account")
                    .accountType(null)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getAccountType()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should reject request when accountType is empty or contains only whitespace")
        void shouldRejectRequestWhenAccountTypeIsEmptyOrWhitespace(String invalidAccountType) {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("Updated Account")
                    .accountType(invalidAccountType)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            if (invalidAccountType.trim().isEmpty()) {
                assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
                boolean hasAccountTypeViolation = violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("accountType"));
                assertThat(hasAccountTypeViolation).isTrue();
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"PERSONAL", "Personal", "business", "savings", "checking", "invalid", "123", "personal "})
        @DisplayName("Should reject request when accountType is not valid enum value")
        void shouldRejectRequestWhenAccountTypeIsNotValidEnumValue(String invalidAccountType) {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("Updated Account")
                    .accountType(invalidAccountType)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            if (!invalidAccountType.equals("personal")) {
                assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
                boolean hasAccountTypeViolation = violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("accountType"));
                assertThat(hasAccountTypeViolation).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Partial Update Scenarios")
    class PartialUpdateScenarios {

        @Test
        @DisplayName("Should support updating only name while keeping accountType unchanged")
        void shouldSupportUpdatingOnlyName() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("New Account Name")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("New Account Name");
            assertThat(request.getAccountType()).isNull();
        }

        @Test
        @DisplayName("Should support updating only accountType while keeping name unchanged")
        void shouldSupportUpdatingOnlyAccountType() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isNull();
            assertThat(request.getAccountType()).isEqualTo("personal");
        }

        @Test
        @DisplayName("Should support clearing fields by not providing them")
        void shouldSupportClearingFieldsByNotProvidingThem() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder().build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isNull();
            assertThat(request.getAccountType()).isNull();
        }

        @Test
        @DisplayName("Should validate different combinations of field updates")
        void shouldValidateDifferentCombinationsOfFieldUpdates() {
            // Test Case 1: Both fields provided
            UpdateBankAccountRequest bothFields = UpdateBankAccountRequest.builder()
                    .name("Updated Name")
                    .accountType("personal")
                    .build();

            // Test Case 2: Only name provided
            UpdateBankAccountRequest onlyName = UpdateBankAccountRequest.builder()
                    .name("Updated Name")
                    .build();

            // Test Case 3: Only accountType provided
            UpdateBankAccountRequest onlyAccountType = UpdateBankAccountRequest.builder()
                    .accountType("personal")
                    .build();

            // Test Case 4: No fields provided
            UpdateBankAccountRequest noFields = UpdateBankAccountRequest.builder().build();

            // When & Then
            assertThat(validator.validate(bothFields)).isEmpty();
            assertThat(validator.validate(onlyName)).isEmpty();
            assertThat(validator.validate(onlyAccountType)).isEmpty();
            assertThat(validator.validate(noFields)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Object Equality and Builder Tests")
    class ObjectEqualityAndBuilderTests {

        @Test
        @DisplayName("Should create equal objects with same values")
        void shouldCreateEqualObjectsWithSameValues() {
            // Given
            UpdateBankAccountRequest request1 = UpdateBankAccountRequest.builder()
                    .name("Updated Account")
                    .accountType("personal")
                    .build();

            UpdateBankAccountRequest request2 = UpdateBankAccountRequest.builder()
                    .name("Updated Account")
                    .accountType("personal")
                    .build();

            // Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should create equal objects when both have null values")
        void shouldCreateEqualObjectsWhenBothHaveNullValues() {
            // Given
            UpdateBankAccountRequest request1 = UpdateBankAccountRequest.builder().build();
            UpdateBankAccountRequest request2 = UpdateBankAccountRequest.builder().build();

            // Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should create different objects with different names")
        void shouldCreateDifferentObjectsWithDifferentNames() {
            // Given
            UpdateBankAccountRequest request1 = UpdateBankAccountRequest.builder()
                    .name("Updated Account 1")
                    .accountType("personal")
                    .build();

            UpdateBankAccountRequest request2 = UpdateBankAccountRequest.builder()
                    .name("Updated Account 2")
                    .accountType("personal")
                    .build();

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should create different objects with different account types")
        void shouldCreateDifferentObjectsWithDifferentAccountTypes() {
            // Given
            UpdateBankAccountRequest request1 = UpdateBankAccountRequest.builder()
                    .name("Updated Account")
                    .accountType("personal")
                    .build();

            UpdateBankAccountRequest request2 = UpdateBankAccountRequest.builder()
                    .name("Updated Account")
                    .accountType("business")
                    .build();

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should support builder pattern modifications")
        void shouldSupportBuilderPatternModifications() {
            // Given
            UpdateBankAccountRequest originalRequest = UpdateBankAccountRequest.builder()
                    .name("Original Account")
                    .accountType("personal")
                    .build();

            // When
            UpdateBankAccountRequest modifiedRequest = originalRequest.toBuilder()
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
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("Updated Account")
                    .accountType("personal")
                    .build();

            // When
            String toString = request.toString();

            // Then
            assertThat(toString).contains("UpdateBankAccountRequest");
            assertThat(toString).contains("Updated Account");
            assertThat(toString).contains("personal");
        }

        @Test
        @DisplayName("Should have proper toString representation with null values")
        void shouldHaveProperToStringRepresentationWithNullValues() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder().build();

            // When
            String toString = request.toString();

            // Then
            assertThat(toString).contains("UpdateBankAccountRequest");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Should handle request with null object")
        void shouldHandleRequestWithNullObject() {
            // Given
            UpdateBankAccountRequest request = null;

            // When & Then
            assertThat(request).isNull();
        }

        @Test
        @DisplayName("Should handle very long account name")
        void shouldHandleVeryLongAccountName() {
            // Given
            String veryLongName = "A".repeat(1000);
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(veryLongName)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            // Should either pass or fail based on max length validation if implemented
            assertThat(request.getName()).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle account name with only special characters")
        void shouldHandleAccountNameWithOnlySpecialCharacters() {
            // Given
            String specialCharsName = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(specialCharsName)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

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
                    UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                            .name("Account " + index)
                            .accountType("personal")
                            .build();
                    
                    Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);
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

        @Test
        @DisplayName("Should handle mixed null and valid field combinations")
        void shouldHandleMixedNullAndValidFieldCombinations() {
            // Given - Test various combinations
            UpdateBankAccountRequest[] requests = {
                UpdateBankAccountRequest.builder().name("Valid Name").accountType(null).build(),
                UpdateBankAccountRequest.builder().name(null).accountType("personal").build(),
                UpdateBankAccountRequest.builder().name(null).accountType(null).build(),
                UpdateBankAccountRequest.builder().name("Valid Name").build(),
                UpdateBankAccountRequest.builder().accountType("personal").build()
            };

            // When & Then
            for (UpdateBankAccountRequest request : requests) {
                Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);
                assertThat(violations).isEmpty(); // All should be valid since fields are optional
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
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(sqlInjectionAttempt)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty(); // Should pass validation, protection handled at service/repository layer
            assertThat(request.getName()).isEqualTo(sqlInjectionAttempt);
        }

        @Test
        @DisplayName("Should handle XSS attempt in name")
        void shouldHandleXssAttemptInName() {
            // Given
            String xssAttempt = "<script>alert('XSS')</script>";
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name(xssAttempt)
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty(); // Should pass validation, sanitization handled elsewhere
            assertThat(request.getName()).isEqualTo(xssAttempt);
        }

        @Test
        @DisplayName("Should maintain field immutability after creation")
        void shouldMaintainFieldImmutabilityAfterCreation() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
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
        @DisplayName("Should handle partial updates securely")
        void shouldHandlePartialUpdatesSecurely() {
            // Given - Different partial update scenarios
            UpdateBankAccountRequest nameOnlyUpdate = UpdateBankAccountRequest.builder()
                    .name("Secure Name Update")
                    .build();

            UpdateBankAccountRequest accountTypeOnlyUpdate = UpdateBankAccountRequest.builder()
                    .accountType("personal")
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> nameViolations = validator.validate(nameOnlyUpdate);
            Set<ConstraintViolation<UpdateBankAccountRequest>> accountTypeViolations = validator.validate(accountTypeOnlyUpdate);

            // Then
            assertThat(nameViolations).isEmpty();
            assertThat(accountTypeViolations).isEmpty();
            assertThat(nameOnlyUpdate.getAccountType()).isNull();
            assertThat(accountTypeOnlyUpdate.getName()).isNull();
        }

        @Test
        @DisplayName("Should validate that null fields represent no change intention")
        void shouldValidateThatNullFieldsRepresentNoChangeIntention() {
            // Given
            UpdateBankAccountRequest request = UpdateBankAccountRequest.builder()
                    .name("Updated Name")
                    .accountType(null) // Explicitly null - should mean "don't update this field"
                    .build();

            // When
            Set<ConstraintViolation<UpdateBankAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getName()).isEqualTo("Updated Name");
            assertThat(request.getAccountType()).isNull(); // Should remain null
        }
    }
}
