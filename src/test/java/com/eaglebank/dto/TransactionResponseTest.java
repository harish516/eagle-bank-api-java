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
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionResponse Tests")
class TransactionResponseTest {

    private Validator validator;
    private LocalDateTime testCreatedTimestamp;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        testCreatedTimestamp = LocalDateTime.of(2025, 8, 3, 10, 30, 0);
    }

    @Nested
    @DisplayName("Valid Object Creation Tests")
    class ValidObjectCreationTests {

        @Test
        @DisplayName("Should create valid response with all required fields")
        void shouldCreateValidResponseWithAllRequiredFields() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getId()).isEqualTo("tan-123abc");
            assertThat(response.getAmount()).isEqualTo(new BigDecimal("100.00"));
            assertThat(response.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(response.getType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(response.getCreatedTimestamp()).isEqualTo(testCreatedTimestamp);
        }

        @Test
        @DisplayName("Should create valid response with optional fields")
        void shouldCreateValidResponseWithOptionalFields() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-456def")
                    .amount(new BigDecimal("250.50"))
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .reference("ATM withdrawal")
                    .userId("usr-abc123")
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getReference()).isEqualTo("ATM withdrawal");
            assertThat(response.getUserId()).isEqualTo("usr-abc123");
        }

        @ParameterizedTest
        @EnumSource(TransactionType.class)
        @DisplayName("Should create valid response with all transaction types")
        void shouldCreateValidResponseWithAllTransactionTypes(TransactionType transactionType) {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-test123")
                    .amount(new BigDecimal("500.00"))
                    .currency(Currency.GBP)
                    .type(transactionType)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getType()).isEqualTo(transactionType);
        }

        @Test
        @DisplayName("Should create valid response with minimum amount")
        void shouldCreateValidResponseWithMinimumAmount() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-min001")
                    .amount(new BigDecimal("0.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should create valid response with maximum amount")
        void shouldCreateValidResponseWithMaximumAmount() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-max001")
                    .amount(new BigDecimal("10000.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Transaction ID Validation Tests")
    class TransactionIdValidationTests {

        @Test
        @DisplayName("Should reject response when ID is null")
        void shouldRejectResponseWhenIdIsNull() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id(null)
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("id");
            assertThat(violation.getMessage()).isEqualTo("Transaction ID is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"tan-123", "tan-abc", "tan-123abc", "tan-ABC123", "tan-a1b2c3"})
        @DisplayName("Should accept valid transaction ID patterns")
        void shouldAcceptValidTransactionIdPatterns(String validId) {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id(validId)
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"123abc", "tan-", "tan", "TAN-123abc", "tan-123-abc", "tan-123@abc", "tan-123 abc"})
        @DisplayName("Should reject invalid transaction ID patterns")
        void shouldRejectInvalidTransactionIdPatterns(String invalidId) {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id(invalidId)
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("id");
            assertThat(violation.getMessage()).isEqualTo("Transaction ID must match pattern: tan-<alphanumeric>");
        }
    }

    @Nested
    @DisplayName("Amount Validation Tests")
    class AmountValidationTests {

        @Test
        @DisplayName("Should reject response when amount is null")
        void shouldRejectResponseWhenAmountIsNull() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(null)
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("amount");
            assertThat(violation.getMessage()).isEqualTo("Amount is required");
        }

        @Test
        @DisplayName("Should reject response when amount is below minimum")
        void shouldRejectResponseWhenAmountIsBelowMinimum() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("-0.01"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("amount");
            assertThat(violation.getMessage()).isEqualTo("Amount must be at least 0.00");
        }

        @Test
        @DisplayName("Should reject response when amount exceeds maximum")
        void shouldRejectResponseWhenAmountExceedsMaximum() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("10000.01"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("amount");
            assertThat(violation.getMessage()).isEqualTo("Amount must not exceed 10000.00");
        }

        @Test
        @DisplayName("Should handle precise decimal amounts")
        void shouldHandlePreciseDecimalAmounts() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("123.45"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getAmount()).isEqualTo(new BigDecimal("123.45"));
        }
    }

    @Nested
    @DisplayName("Currency Validation Tests")
    class CurrencyValidationTests {

        @Test
        @DisplayName("Should reject response when currency is null")
        void shouldRejectResponseWhenCurrencyIsNull() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(null)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("currency");
            assertThat(violation.getMessage()).isEqualTo("Currency is required");
        }

        @Test
        @DisplayName("Should accept GBP currency")
        void shouldAcceptGbpCurrency() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getCurrency()).isEqualTo(Currency.GBP);
        }
    }

    @Nested
    @DisplayName("Transaction Type Validation Tests")
    class TransactionTypeValidationTests {

        @Test
        @DisplayName("Should reject response when type is null")
        void shouldRejectResponseWhenTypeIsNull() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(null)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("type");
            assertThat(violation.getMessage()).isEqualTo("Transaction type is required");
        }

        @Test
        @DisplayName("Should accept DEPOSIT transaction type")
        void shouldAcceptDepositTransactionType() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getType()).isEqualTo(TransactionType.DEPOSIT);
        }

        @Test
        @DisplayName("Should accept WITHDRAWAL transaction type")
        void shouldAcceptWithdrawalTransactionType() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        }
    }

    @Nested
    @DisplayName("User ID Validation Tests")
    class UserIdValidationTests {

        @Test
        @DisplayName("Should accept null user ID")
        void shouldAcceptNullUserId() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .userId(null)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getUserId()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"usr-123", "usr-abc", "usr-123abc", "usr-ABC123", "usr-a1b2c3"})
        @DisplayName("Should accept valid user ID patterns")
        void shouldAcceptValidUserIdPatterns(String validUserId) {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .userId(validUserId)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"123abc", "usr-", "usr", "USR-123abc", "usr-123-abc", "usr-123@abc", "usr-123 abc"})
        @DisplayName("Should reject invalid user ID patterns")
        void shouldRejectInvalidUserIdPatterns(String invalidUserId) {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .userId(invalidUserId)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("userId");
            assertThat(violation.getMessage()).isEqualTo("User ID must match pattern: usr-<alphanumeric>");
        }
    }

    @Nested
    @DisplayName("Timestamp Field Tests")
    class TimestampFieldTests {

        @Test
        @DisplayName("Should reject response when created timestamp is null")
        void shouldRejectResponseWhenCreatedTimestampIsNull() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(null)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TransactionResponse> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("createdTimestamp");
            assertThat(violation.getMessage()).isEqualTo("Created timestamp is required");
        }

        @Test
        @DisplayName("Should accept valid timestamp")
        void shouldAcceptValidTimestamp() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(now)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getCreatedTimestamp()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should handle past and future timestamps")
        void shouldHandlePastAndFutureTimestamps() {
            LocalDateTime pastTimestamp = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            LocalDateTime futureTimestamp = LocalDateTime.of(2030, 12, 31, 23, 59, 59);

            // Test past timestamp
            TransactionResponse pastResponse = TransactionResponse.builder()
                    .id("tan-past123")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(pastTimestamp)
                    .build();

            // Test future timestamp
            TransactionResponse futureResponse = TransactionResponse.builder()
                    .id("tan-future123")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(futureTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> pastViolations = validator.validate(pastResponse);
            Set<ConstraintViolation<TransactionResponse>> futureViolations = validator.validate(futureResponse);

            // Then
            assertThat(pastViolations).isEmpty();
            assertThat(futureViolations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Reference Field Tests")
    class ReferenceFieldTests {

        @Test
        @DisplayName("Should accept null reference")
        void shouldAcceptNullReference() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference(null)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getReference()).isNull();
        }

        @Test
        @DisplayName("Should accept empty reference")
        void shouldAcceptEmptyReference() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("")
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getReference()).isEqualTo("");
        }

        @Test
        @DisplayName("Should accept long reference text")
        void shouldAcceptLongReferenceText() {
            // Given
            String longReference = "This is a very long transaction reference that describes the purpose of the transaction in great detail";
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference(longReference)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getReference()).isEqualTo(longReference);
        }
    }

    @Nested
    @DisplayName("Object Equality and Builder Tests")
    class ObjectEqualityAndBuilderTests {

        @Test
        @DisplayName("Should create equal objects with same values")
        void shouldCreateEqualObjectsWithSameValues() {
            // Given
            TransactionResponse response1 = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("Test reference")
                    .userId("usr-abc123")
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            TransactionResponse response2 = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("Test reference")
                    .userId("usr-abc123")
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When & Then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should create different objects with different values")
        void shouldCreateDifferentObjectsWithDifferentValues() {
            // Given
            TransactionResponse response1 = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            TransactionResponse response2 = TransactionResponse.builder()
                    .id("tan-456def")
                    .amount(new BigDecimal("200.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When & Then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should support toBuilder functionality")
        void shouldSupportToBuilderFunctionality() {
            // Given
            TransactionResponse original = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("Original reference")
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            TransactionResponse modified = original.toBuilder()
                    .amount(new BigDecimal("200.00"))
                    .reference("Modified reference")
                    .build();

            // Then
            assertThat(modified.getAmount()).isEqualTo(new BigDecimal("200.00"));
            assertThat(modified.getReference()).isEqualTo("Modified reference");
            assertThat(modified.getId()).isEqualTo("tan-123abc");
            assertThat(modified.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(modified.getType()).isEqualTo(TransactionType.DEPOSIT);
        }

        @Test
        @DisplayName("Should have proper toString implementation")
        void shouldHaveProperToStringImplementation() {
            // Given
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference("Test reference")
                    .userId("usr-abc123")
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            String toString = response.toString();

            // Then
            assertThat(toString).contains("TransactionResponse");
            assertThat(toString).contains("id=tan-123abc");
            assertThat(toString).contains("amount=100.00");
            assertThat(toString).contains("currency=GBP");
            assertThat(toString).contains("type=DEPOSIT");
            assertThat(toString).contains("reference=Test reference");
            assertThat(toString).contains("userId=usr-abc123");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Should validate complete empty response")
        void shouldValidateCompleteEmptyResponse() {
            // Given
            TransactionResponse response = TransactionResponse.builder().build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSize(5); // id, amount, currency, type, and createdTimestamp are required
            
            boolean hasIdViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("id"));
            boolean hasAmountViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            boolean hasCurrencyViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
            boolean hasTypeViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("type"));
            boolean hasTimestampViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("createdTimestamp"));
            
            assertThat(hasIdViolation).isTrue();
            assertThat(hasAmountViolation).isTrue();
            assertThat(hasCurrencyViolation).isTrue();
            assertThat(hasTypeViolation).isTrue();
            assertThat(hasTimestampViolation).isTrue();
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
                TransactionResponse response = TransactionResponse.builder()
                        .id("tan-boundary" + amount.toString().replace(".", ""))
                        .amount(amount)
                        .currency(Currency.GBP)
                        .type(TransactionType.DEPOSIT)
                        .createdTimestamp(testCreatedTimestamp)
                        .build();

                // When
                Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

                // Then
                assertThat(violations).isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle concurrent object creation")
        void shouldHandleConcurrentObjectCreation() throws InterruptedException {
            // Given
            int numberOfThreads = 10;
            TransactionResponse[] responses = new TransactionResponse[numberOfThreads];
            Thread[] threads = new Thread[numberOfThreads];

            // When
            for (int i = 0; i < numberOfThreads; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    responses[index] = TransactionResponse.builder()
                            .id("tan-concurrent" + index)
                            .amount(new BigDecimal("100.00").add(new BigDecimal(index)))
                            .currency(Currency.GBP)
                            .type(index % 2 == 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL)
                            .reference("Concurrent response " + index)
                            .createdTimestamp(testCreatedTimestamp.plusMinutes(index))
                            .build();
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            for (int i = 0; i < numberOfThreads; i++) {
                assertThat(responses[i]).isNotNull();
                assertThat(responses[i].getId()).isEqualTo("tan-concurrent" + i);
                assertThat(responses[i].getAmount()).isEqualTo(new BigDecimal("100.00").add(new BigDecimal(i)));
                assertThat(responses[i].getReference()).isEqualTo("Concurrent response " + i);
            }
        }

        @Test
        @DisplayName("Should handle balance precision edge cases")
        void shouldHandleBalancePrecisionEdgeCases() {
            // Given
            BigDecimal[] edgeCaseAmounts = {
                    new BigDecimal("0.001"),   // More than 2 decimal places
                    new BigDecimal("0.009"),   // Rounding edge case
                    new BigDecimal("9999.999"), // Near maximum with extra precision
                    new BigDecimal("1234.567") // Multiple decimal places
            };

            for (BigDecimal amount : edgeCaseAmounts) {
                // When
                TransactionResponse response = TransactionResponse.builder()
                        .id("tan-precision" + amount.toString().replace(".", ""))
                        .amount(amount)
                        .currency(Currency.GBP)
                        .type(TransactionType.DEPOSIT)
                        .createdTimestamp(testCreatedTimestamp)
                        .build();

                // Then
                assertThat(response.getAmount()).isEqualTo(amount);
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
            TransactionResponse response = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference(specialCharReference)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).isEmpty();
            assertThat(response.getReference()).isEqualTo(specialCharReference);
        }

        @Test
        @DisplayName("Should maintain immutability through builder")
        void shouldMaintainImmutabilityThroughBuilder() {
            // Given
            TransactionResponse original = TransactionResponse.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .createdTimestamp(testCreatedTimestamp)
                    .build();

            // When
            TransactionResponse modified = original.toBuilder()
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
            TransactionResponse response = new TransactionResponse();

            // When
            Set<ConstraintViolation<TransactionResponse>> violations = validator.validate(response);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
            
            // Check that all required field violations are present
            boolean hasIdViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("id"));
            boolean hasAmountViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            boolean hasCurrencyViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
            boolean hasTypeViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("type"));
            boolean hasTimestampViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("createdTimestamp"));
            
            assertThat(hasIdViolation).isTrue();
            assertThat(hasAmountViolation).isTrue();
            assertThat(hasCurrencyViolation).isTrue();
            assertThat(hasTypeViolation).isTrue();
            assertThat(hasTimestampViolation).isTrue();
        }
    }
}
