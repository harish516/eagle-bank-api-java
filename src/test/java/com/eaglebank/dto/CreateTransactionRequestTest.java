package com.eaglebank.dto;

import com.eaglebank.domain.Currency;
import com.eaglebank.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateTransactionRequest Tests")
class CreateTransactionRequestTest {

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
        @DisplayName("Should create valid request with all required fields")
        void shouldCreateValidRequestWithAllRequiredFields() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getAmount()).isEqualTo(new BigDecimal("100.00"));
            assertThat(request.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(request.getType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(request.getReference()).isNull();
        }

        @Test
        @DisplayName("Should create valid request with optional reference field")
        void shouldCreateValidRequestWithOptionalReferenceField() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("250.50"))
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .reference("Monthly subscription payment")
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getReference()).isEqualTo("Monthly subscription payment");
        }

        @ParameterizedTest
        @EnumSource(TransactionType.class)
        @DisplayName("Should create valid request with all transaction types")
        void shouldCreateValidRequestWithAllTransactionTypes(TransactionType transactionType) {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("500.00"))
                    .currency(Currency.GBP)
                    .type(transactionType)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getType()).isEqualTo(transactionType);
        }

        @Test
        @DisplayName("Should create valid request with minimum amount")
        void shouldCreateValidRequestWithMinimumAmount() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("0.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should create valid request with maximum amount")
        void shouldCreateValidRequestWithMaximumAmount() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("10000.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Amount Field Validation Tests")
    class AmountFieldValidationTests {

        @Test
        @DisplayName("Should reject request when amount is null")
        void shouldRejectRequestWhenAmountIsNull() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(null)
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateTransactionRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("amount");
            assertThat(violation.getMessage()).isEqualTo("Amount is required");
        }

        @Test
        @DisplayName("Should reject request when amount is below minimum")
        void shouldRejectRequestWhenAmountIsBelowMinimum() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("-0.01"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateTransactionRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("amount");
            assertThat(violation.getMessage()).isEqualTo("Amount must be at least 0.00");
        }

        @Test
        @DisplayName("Should reject request when amount exceeds maximum")
        void shouldRejectRequestWhenAmountExceedsMaximum() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("10000.01"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateTransactionRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("amount");
            assertThat(violation.getMessage()).isEqualTo("Amount must not exceed 10000.00");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.01", "99.99", "1000.00", "5000.50", "9999.99"})
        @DisplayName("Should accept valid amount values")
        void shouldAcceptValidAmountValues(String amountStr) {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal(amountStr))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle precise decimal amounts")
        void shouldHandlePreciseDecimalAmounts() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("123.45"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getAmount()).isEqualTo(new BigDecimal("123.45"));
        }
    }

    @Nested
    @DisplayName("Currency Field Validation Tests")
    class CurrencyFieldValidationTests {

        @Test
        @DisplayName("Should reject request when currency is null")
        void shouldRejectRequestWhenCurrencyIsNull() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(null)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateTransactionRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("currency");
            assertThat(violation.getMessage()).isEqualTo("Currency is required");
        }

        @Test
        @DisplayName("Should accept GBP currency")
        void shouldAcceptGbpCurrency() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getCurrency()).isEqualTo(Currency.GBP);
        }
    }

    @Nested
    @DisplayName("Transaction Type Field Validation Tests")
    class TransactionTypeFieldValidationTests {

        @Test
        @DisplayName("Should reject request when type is null")
        void shouldRejectRequestWhenTypeIsNull() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(null)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateTransactionRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("type");
            assertThat(violation.getMessage()).isEqualTo("Transaction type is required");
        }

        @Test
        @DisplayName("Should accept DEPOSIT transaction type")
        void shouldAcceptDepositTransactionType() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getType()).isEqualTo(TransactionType.DEPOSIT);
        }

        @Test
        @DisplayName("Should accept WITHDRAWAL transaction type")
        void shouldAcceptWithdrawalTransactionType() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        }
    }

    @Nested
    @DisplayName("Reference Field Tests")
    class ReferenceFieldTests {

        @Test
        @DisplayName("Should accept null reference")
        void shouldAcceptNullReference() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference(null)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getReference()).isNull();
        }

        @Test
        @DisplayName("Should accept empty reference")
        void shouldAcceptEmptyReference() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("")
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getReference()).isEqualTo("");
        }

        @Test
        @DisplayName("Should accept long reference text")
        void shouldAcceptLongReferenceText() {
            // Given
            String longReference = "This is a very long transaction reference that describes the purpose of the transaction in great detail";
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference(longReference)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getReference()).isEqualTo(longReference);
        }
    }

    @Nested
    @DisplayName("Object Equality and Builder Tests")
    class ObjectEqualityAndBuilderTests {

        @Test
        @DisplayName("Should create equal objects with same values")
        void shouldCreateEqualObjectsWithSameValues() {
            // Given
            CreateTransactionRequest request1 = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("Test reference")
                    .build();

            CreateTransactionRequest request2 = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("Test reference")
                    .build();

            // When & Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should create different objects with different values")
        void shouldCreateDifferentObjectsWithDifferentValues() {
            // Given
            CreateTransactionRequest request1 = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            CreateTransactionRequest request2 = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("200.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .build();

            // When & Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should support toBuilder functionality")
        void shouldSupportToBuilderFunctionality() {
            // Given
            CreateTransactionRequest original = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("Original reference")
                    .build();

            // When
            CreateTransactionRequest modified = original.toBuilder()
                    .amount(new BigDecimal("200.00"))
                    .reference("Modified reference")
                    .build();

            // Then
            assertThat(modified.getAmount()).isEqualTo(new BigDecimal("200.00"));
            assertThat(modified.getReference()).isEqualTo("Modified reference");
            assertThat(modified.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(modified.getType()).isEqualTo(TransactionType.DEPOSIT);
        }

        @Test
        @DisplayName("Should have proper toString implementation")
        void shouldHaveProperToStringImplementation() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("Test reference")
                    .build();

            // When
            String toString = request.toString();

            // Then
            assertThat(toString).contains("CreateTransactionRequest");
            assertThat(toString).contains("amount=100.00");
            assertThat(toString).contains("currency=GBP");
            assertThat(toString).contains("type=DEPOSIT");
            assertThat(toString).contains("reference=Test reference");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Should validate complete empty request")
        void shouldValidateCompleteEmptyRequest() {
            // Given
            CreateTransactionRequest request = CreateTransactionRequest.builder().build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(3); // amount, currency, and type are required
            
            boolean hasAmountViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            boolean hasCurrencyViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
            boolean hasTypeViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("type"));
            
            assertThat(hasAmountViolation).isTrue();
            assertThat(hasCurrencyViolation).isTrue();
            assertThat(hasTypeViolation).isTrue();
        }

        @Test
        @DisplayName("Should handle boundary amount values")
        void shouldHandleBoundaryAmountValues() {
            // Test boundary values
            BigDecimal[] boundaryAmounts = {
                new BigDecimal("0.00"),    // minimum
                new BigDecimal("0.01"),    // just above minimum
                new BigDecimal("9999.99"), // just below maximum
                new BigDecimal("10000.00") // maximum
            };

            for (BigDecimal amount : boundaryAmounts) {
                // Given
                CreateTransactionRequest request = CreateTransactionRequest.builder()
                        .amount(amount)
                        .currency(Currency.GBP)
                        .type(TransactionType.DEPOSIT)
                        .build();

                // When
                Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

                // Then
                assertThat(violations).isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle concurrent object creation")
        void shouldHandleConcurrentObjectCreation() throws InterruptedException {
            // Given
            int numberOfThreads = 10;
            CreateTransactionRequest[] requests = new CreateTransactionRequest[numberOfThreads];
            Thread[] threads = new Thread[numberOfThreads];

            // When
            for (int i = 0; i < numberOfThreads; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    requests[index] = CreateTransactionRequest.builder()
                            .amount(new BigDecimal("100.00").add(new BigDecimal(index)))
                            .currency(Currency.GBP)
                            .type(index % 2 == 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL)
                            .reference("Concurrent request " + index)
                            .build();
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            for (int i = 0; i < numberOfThreads; i++) {
                assertThat(requests[i]).isNotNull();
                assertThat(requests[i].getAmount()).isEqualTo(new BigDecimal("100.00").add(new BigDecimal(i)));
                assertThat(requests[i].getReference()).isEqualTo("Concurrent request " + i);
            }
        }
    }

    @Nested
    @DisplayName("Data Integrity and Security Tests")
    class DataIntegrityAndSecurityTests {

        @Test
        @DisplayName("Should handle special characters in reference field")
        void shouldHandleSpecialCharactersInReferenceField() {
            // Given
            String specialCharReference = "Payment for @item #123 & service (50% discount) - £100.00";
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference(specialCharReference)
                    .build();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
            assertThat(request.getReference()).isEqualTo(specialCharReference);
        }

        @Test
        @DisplayName("Should maintain immutability through builder")
        void shouldMaintainImmutabilityThroughBuilder() {
            // Given
            CreateTransactionRequest original = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .build();

            // When
            CreateTransactionRequest modified = original.toBuilder()
                    .amount(new BigDecimal("200.00"))
                    .build();

            // Then
            assertThat(original.getAmount()).isEqualTo(new BigDecimal("100.00"));
            assertThat(modified.getAmount()).isEqualTo(new BigDecimal("200.00"));
            assertThat(original).isNotEqualTo(modified);
        }

        @Test
        @DisplayName("Should handle null field validation gracefully")
        void shouldHandleNullFieldValidationGracefully() {
            // Given
            CreateTransactionRequest request = new CreateTransactionRequest();

            // When
            Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
            
            // Check that all required field violations are present
            boolean hasAmountViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            boolean hasCurrencyViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
            boolean hasTypeViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("type"));
            
            assertThat(hasAmountViolation).isTrue();
            assertThat(hasCurrencyViolation).isTrue();
            assertThat(hasTypeViolation).isTrue();
        }
    }
}
