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

@DisplayName("BankAccount Bean Validation Tests")
class BankAccountBeanValidationTest {

    private Validator validator;
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
    }

    @Nested
    @DisplayName("Account Number Validation")
    class AccountNumberValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when account number is null, empty or blank")
        void shouldFailValidationWhenAccountNumberIsNullEmptyOrBlank(String invalidAccountNumber) {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber(invalidAccountNumber)
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Account number is required"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345678", "0123456", "021234567", "abc12345", "01-23456"})
        @DisplayName("Should fail validation when account number doesn't match pattern")
        void shouldFailValidationWhenAccountNumberDoesNotMatchPattern(String invalidAccountNumber) {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber(invalidAccountNumber)
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Account number must match pattern"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"01234567", "01000000", "01999999"})
        @DisplayName("Should pass validation with valid account number")
        void shouldPassValidationWithValidAccountNumber(String validAccountNumber) {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber(validAccountNumber)
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Balance Validation")
    class BalanceValidationTests {

        @Test
        @DisplayName("Should fail validation when balance is null")
        void shouldFailValidationWhenBalanceIsNull() {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(null)
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("balance"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Balance is required"));
        }

        @Test
        @DisplayName("Should fail validation when balance is below minimum")
        void shouldFailValidationWhenBalanceIsBelowMinimum() {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("-0.01"))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("balance"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Balance must be at least 0"));
        }

        @Test
        @DisplayName("Should fail validation when balance exceeds maximum")
        void shouldFailValidationWhenBalanceExceedsMaximum() {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("10000.01"))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("balance"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Balance must not exceed 10000"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "100.50", "10000.00"})
        @DisplayName("Should pass validation with valid balance")
        void shouldPassValidationWithValidBalance(String validBalance) {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal(validBalance))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Required Fields Validation")
    class RequiredFieldsValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when sort code is null, empty or blank")
        void shouldFailValidationWhenSortCodeIsNullEmptyOrBlank(String invalidSortCode) {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode(invalidSortCode)
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("sortCode"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Sort code is required"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when account name is null, empty or blank")
        void shouldFailValidationWhenAccountNameIsNullEmptyOrBlank(String invalidName) {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name(invalidName)
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Account name is required"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when currency is null, empty or blank")
        void shouldFailValidationWhenCurrencyIsNullEmptyOrBlank(String invalidCurrency) {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency(invalidCurrency)
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Currency is required"));
        }

        @Test
        @DisplayName("Should fail validation when account type is null")
        void shouldFailValidationWhenAccountTypeIsNull() {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(null)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(validUser)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountType"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("Account type is required"));
        }

        @Test
        @DisplayName("Should fail validation when user is null")
        void shouldFailValidationWhenUserIsNull() {
            // Given
            BankAccount account = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(null)
                    .build();

            // When
            Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("user"));
            assertThat(violations).anyMatch(v -> v.getMessage().contains("User is required"));
        }
    }

    @Test
    @DisplayName("Should pass validation with all valid fields")
    void shouldPassValidationWithAllValidFields() {
        // Given
        BankAccount account = BankAccount.builder()
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

        // When
        Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);

        // Then
        assertThat(violations).isEmpty();
    }
}
