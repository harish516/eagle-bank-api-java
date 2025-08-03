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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transaction Bean Validation Tests")
class TransactionBeanValidationTest {

    private Validator validator;
    private BankAccount validBankAccount;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();
                
        validUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
                
        validBankAccount = BankAccount.builder()
                .accountNumber("01234567")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(new BigDecimal("1000.00"))
                .currency("GBP")
                .user(validUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Transaction ID Validation")
    class TransactionIdValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"invalid-id", "tan-", "tan-@invalid", "tan-with spaces", "abc-123"})
        @DisplayName("Should fail validation when transaction ID doesn't match pattern")
        void shouldFailValidationWhenTransactionIdDoesNotMatchPattern(String invalidId) {
            // Given
            Transaction transaction = Transaction.builder()
                    .id(invalidId)
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("id"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Transaction ID must match pattern"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"tan-abc123", "tan-123", "tan-ABC123", "tan-a1B2c3"})
        @DisplayName("Should pass validation with valid transaction ID")
        void shouldPassValidationWithValidTransactionId(String validId) {
            // Given
            Transaction transaction = Transaction.builder()
                    .id(validId)
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when transaction ID is null (auto-generated)")
        void shouldPassValidationWhenTransactionIdIsNull() {
            // Given
            Transaction transaction = Transaction.builder()
                    .id(null)
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Amount Validation")
    class AmountValidationTests {

        @Test
        @DisplayName("Should fail validation when amount is null")
        void shouldFailValidationWhenAmountIsNull() {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(null)
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Amount is required"));
        }

        @Test
        @DisplayName("Should fail validation when amount is below minimum")
        void shouldFailValidationWhenAmountIsBelowMinimum() {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("-0.01"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Amount must be at least 0"));
        }

        @Test
        @DisplayName("Should fail validation when amount exceeds maximum")
        void shouldFailValidationWhenAmountExceedsMaximum() {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("10000.01"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Amount must not exceed 10000"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "100.50", "10000.00"})
        @DisplayName("Should pass validation with valid amount")
        void shouldPassValidationWithValidAmount(String validAmount) {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal(validAmount))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Currency Validation")
    class CurrencyValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when currency is null, empty or blank")
        void shouldFailValidationWhenCurrencyIsNullEmptyOrBlank(String invalidCurrency) {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(invalidCurrency)
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Currency is required"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"USD", "EUR", "JPY", "invalid"})
        @DisplayName("Should fail validation when currency is not GBP")
        void shouldFailValidationWhenCurrencyIsNotGBP(String invalidCurrency) {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(invalidCurrency)
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Currency must be GBP"));
        }

        @Test
        @DisplayName("Should pass validation with GBP currency")
        void shouldPassValidationWithGBPCurrency() {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Required Fields Validation")
    class RequiredFieldsValidationTests {

        @Test
        @DisplayName("Should fail validation when transaction type is null")
        void shouldFailValidationWhenTransactionTypeIsNull() {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(null)
                    .bankAccount(validBankAccount)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Transaction type is required"));
        }

        @Test
        @DisplayName("Should fail validation when bank account is null")
        void shouldFailValidationWhenBankAccountIsNull() {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(null)
                    .userId(validUser.getId())
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("bankAccount"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Bank account is required"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when user ID is null, empty or blank")
        void shouldFailValidationWhenUserIdIsNullEmptyOrBlank(String invalidUserId) {
            // Given
            Transaction transaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(validBankAccount)
                    .userId(invalidUserId)
                    .build();

            // When
            Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("User ID is required"));
        }
    }

    @Test
    @DisplayName("Should pass validation with all valid fields")
    void shouldPassValidationWithAllValidFields() {
        // Given
        Transaction transaction = Transaction.builder()
                .id("tan-123abc")
                .amount(new BigDecimal("100.00"))
                .currency("GBP")
                .type(TransactionType.DEPOSIT)
                .reference("Test transaction")
                .bankAccount(validBankAccount)
                .userId(validUser.getId())
                .createdTimestamp(LocalDateTime.now())
                .build();

        // When
        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

        // Then
        assertThat(violations).isEmpty();
    }
}
