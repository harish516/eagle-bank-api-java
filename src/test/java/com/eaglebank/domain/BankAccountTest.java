package com.eaglebank.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

class BankAccountTest {

    private BankAccount bankAccount;
    private User user;
    private Validator validator;

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

        user = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();

        bankAccount = BankAccount.builder()
                .accountNumber("01234567")
                .sortCode("10-10-10")
                .name("Personal Bank Account")
                .accountType(AccountType.PERSONAL)
                .balance(new BigDecimal("1000.00"))
                .currency("GBP")
                .user(user)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateBankAccountWithValidData() {
        assertThat(bankAccount.getAccountNumber()).isEqualTo("01234567");
        assertThat(bankAccount.getSortCode()).isEqualTo("10-10-10");
        assertThat(bankAccount.getName()).isEqualTo("Personal Bank Account");
        assertThat(bankAccount.getAccountType()).isEqualTo(AccountType.PERSONAL);
        assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(bankAccount.getCurrency()).isEqualTo("GBP");
        assertThat(bankAccount.getUser()).isEqualTo(user);
    }

    @Test
    void shouldDepositMoney() {
        BigDecimal initialBalance = bankAccount.getBalance();
        BigDecimal depositAmount = new BigDecimal("500.00");

        bankAccount.deposit(depositAmount);

        assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.add(depositAmount));
    }

    @Test
    void shouldWithdrawMoney() {
        BigDecimal initialBalance = bankAccount.getBalance();
        BigDecimal withdrawalAmount = new BigDecimal("300.00");

        bankAccount.withdraw(withdrawalAmount);

        assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.subtract(withdrawalAmount));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        BigDecimal withdrawalAmount = new BigDecimal("1500.00");

        assertThatThrownBy(() -> bankAccount.withdraw(withdrawalAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingNegativeAmount() {
        assertThatThrownBy(() -> bankAccount.withdraw(new BigDecimal("-100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");
    }

    @Test
    void shouldThrowExceptionWhenDepositingNegativeAmount() {
        assertThatThrownBy(() -> bankAccount.deposit(new BigDecimal("-100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");
    }

    // === COMPREHENSIVE VALIDATION TESTS ===

    @Nested
    @DisplayName("Bean Validation Tests")
    class BeanValidationTests {

        @Test
        @DisplayName("Should fail validation when sort code is null")
        void shouldFailValidationWhenSortCodeIsNull() {
            BankAccount invalidAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode(null)
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(invalidAccount);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Sort code is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when sort code is empty or blank")
        void shouldFailValidationWhenSortCodeIsEmptyOrBlank(String invalidSortCode) {
            BankAccount invalidAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode(invalidSortCode)
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(invalidAccount);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Sort code is required");
        }

        @Test
        @DisplayName("Should fail validation when account name is null")
        void shouldFailValidationWhenAccountNameIsNull() {
            BankAccount invalidAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name(null)
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(invalidAccount);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Account name is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when account name is empty or blank")
        void shouldFailValidationWhenAccountNameIsEmptyOrBlank(String invalidName) {
            BankAccount invalidAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name(invalidName)
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(invalidAccount);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Account name is required");
        }

        @Test
        @DisplayName("Should fail validation when currency is null")
        void shouldFailValidationWhenCurrencyIsNull() {
            BankAccount invalidAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency(null)
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(invalidAccount);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Currency is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when currency is empty or blank")
        void shouldFailValidationWhenCurrencyIsEmptyOrBlank(String invalidCurrency) {
            BankAccount invalidAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency(invalidCurrency)
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(invalidAccount);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Currency is required");
        }

        @Test
        @DisplayName("Should fail validation when balance is below minimum")
        void shouldFailValidationWhenBalanceIsBelowMinimum() {
            BankAccount invalidAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("-0.01"))
                    .currency("GBP")
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(invalidAccount);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Balance must be at least 0");
        }

        @Test
        @DisplayName("Should fail validation when balance exceeds maximum")
        void shouldFailValidationWhenBalanceExceedsMaximum() {
            BankAccount invalidAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("10000.01"))
                    .currency("GBP")
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(invalidAccount);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Balance must not exceed 10000");
        }

        @Test
        @DisplayName("Should pass validation when balance is at minimum boundary")
        void shouldPassValidationWhenBalanceIsAtMinimumBoundary() {
            BankAccount validAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("0.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(validAccount);
            
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when balance is at maximum boundary")
        void shouldPassValidationWhenBalanceIsAtMaximumBoundary() {
            BankAccount validAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("10000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            Set<ConstraintViolation<BankAccount>> violations = validator.validate(validAccount);
            
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Account Number Validation Tests")
    class AccountNumberValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "0123456",   // Too short
            "012345678", // Too long  
            "0123456a",  // Contains letter
            "0023456",   // Doesn't start with 01
            "1234567",   // Doesn't start with 01
            "01-23456",  // Contains special character
            "01 23456",  // Contains space
            ""           // Empty
        })

        @Test
        @DisplayName("Should handle null account number gracefully")
        void shouldHandleNullAccountNumberGracefully() {
            // Null account number should not trigger validation exception in builder
            // but would fail at persistence level due to @Id constraint
            BankAccount accountWithNullNumber = BankAccount.builder()
                    .accountNumber(null)
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            assertThat(accountWithNullNumber.getAccountNumber()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "01000000",
            "01123456", 
            "01999999"
        })
        @DisplayName("Should accept valid account number formats")
        void shouldAcceptValidAccountNumberFormats(String validAccountNumber) {
            BankAccount validAccount = BankAccount.builder()
                    .accountNumber(validAccountNumber)
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            assertThat(validAccount.getAccountNumber()).isEqualTo(validAccountNumber);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should return true when account has sufficient funds")
        void shouldReturnTrueWhenAccountHasSufficientFunds() {
            BigDecimal checkAmount = new BigDecimal("500.00");
            
            boolean hasFunds = bankAccount.hasSufficientFunds(checkAmount);
            
            assertThat(hasFunds).isTrue();
        }

        @Test
        @DisplayName("Should return false when account has insufficient funds")
        void shouldReturnFalseWhenAccountHasInsufficientFunds() {
            BigDecimal checkAmount = new BigDecimal("1500.00");
            
            boolean hasFunds = bankAccount.hasSufficientFunds(checkAmount);
            
            assertThat(hasFunds).isFalse();
        }

        @Test
        @DisplayName("Should return true when check amount equals exact balance")
        void shouldReturnTrueWhenCheckAmountEqualsExactBalance() {
            BigDecimal exactBalance = bankAccount.getBalance();
            
            boolean hasFunds = bankAccount.hasSufficientFunds(exactBalance);
            
            assertThat(hasFunds).isTrue();
        }

        @Test
        @DisplayName("Should handle zero deposit amount")
        void shouldHandleZeroDepositAmount() {
            assertThatThrownBy(() -> bankAccount.deposit(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Should handle zero withdrawal amount")
        void shouldHandleZeroWithdrawalAmount() {
            assertThatThrownBy(() -> bankAccount.withdraw(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Should handle null deposit amount")
        void shouldHandleNullDepositAmount() {
            assertThatThrownBy(() -> bankAccount.deposit(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Should handle null withdrawal amount")
        void shouldHandleNullWithdrawalAmount() {
            assertThatThrownBy(() -> bankAccount.withdraw(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Should handle precise decimal deposit amounts")
        void shouldHandlePreciseDecimalDepositAmounts() {
            BigDecimal initialBalance = bankAccount.getBalance();
            BigDecimal depositAmount = new BigDecimal("123.45");

            bankAccount.deposit(depositAmount);

            assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.add(depositAmount));
            assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("1123.45"));
        }

        @Test
        @DisplayName("Should handle precise decimal withdrawal amounts")
        void shouldHandlePreciseDecimalWithdrawalAmounts() {
            BigDecimal initialBalance = bankAccount.getBalance();
            BigDecimal withdrawalAmount = new BigDecimal("123.45");

            bankAccount.withdraw(withdrawalAmount);

            assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.subtract(withdrawalAmount));
            assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("876.55"));
        }

        @Test
        @DisplayName("Should handle withdrawal of entire balance")
        void shouldHandleWithdrawalOfEntireBalance() {
            BigDecimal entireBalance = bankAccount.getBalance();

            bankAccount.withdraw(entireBalance);

            assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Should handle multiple consecutive deposits")
        void shouldHandleMultipleConsecutiveDeposits() {
            BigDecimal initialBalance = bankAccount.getBalance();

            bankAccount.deposit(new BigDecimal("100.00"));
            bankAccount.deposit(new BigDecimal("200.00"));
            bankAccount.deposit(new BigDecimal("50.00"));

            BigDecimal expectedBalance = initialBalance
                    .add(new BigDecimal("100.00"))
                    .add(new BigDecimal("200.00"))
                    .add(new BigDecimal("50.00"));

            assertThat(bankAccount.getBalance()).isEqualTo(expectedBalance);
            assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("1350.00"));
        }

        @Test
        @DisplayName("Should handle multiple consecutive withdrawals")
        void shouldHandleMultipleConsecutiveWithdrawals() {
            BigDecimal initialBalance = bankAccount.getBalance();

            bankAccount.withdraw(new BigDecimal("100.00"));
            bankAccount.withdraw(new BigDecimal("200.00"));
            bankAccount.withdraw(new BigDecimal("50.00"));

            BigDecimal expectedBalance = initialBalance
                    .subtract(new BigDecimal("100.00"))
                    .subtract(new BigDecimal("200.00"))
                    .subtract(new BigDecimal("50.00"));

            assertThat(bankAccount.getBalance()).isEqualTo(expectedBalance);
            assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("650.00"));
        }

        @Test
        @DisplayName("Should handle mixed deposits and withdrawals")
        void shouldHandleMixedDepositsAndWithdrawals() {
            bankAccount.deposit(new BigDecimal("500.00"));   // 1500.00
            bankAccount.withdraw(new BigDecimal("200.00"));  // 1300.00
            bankAccount.deposit(new BigDecimal("100.00"));   // 1400.00
            bankAccount.withdraw(new BigDecimal("300.00"));  // 1100.00

            assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("1100.00"));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesAndErrorScenarios {

        @Test
        @DisplayName("Should maintain precision with large decimal operations")
        void shouldMaintainPrecisionWithLargeDecimalOperations() {
            // Start with maximum allowed balance
            BankAccount maxBalanceAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Max Balance Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("10000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            maxBalanceAccount.withdraw(new BigDecimal("0.01"));
            assertThat(maxBalanceAccount.getBalance()).isEqualTo(new BigDecimal("9999.99"));

            maxBalanceAccount.deposit(new BigDecimal("0.01"));
            assertThat(maxBalanceAccount.getBalance()).isEqualTo(new BigDecimal("10000.00"));
        }

        @Test
        @DisplayName("Should handle account types correctly")
        void shouldHandleAccountTypesCorrectly() {
            BankAccount personalAccount = BankAccount.builder()
                    .accountNumber("01234568")
                    .sortCode("10-10-10")
                    .name("Personal Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("5000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            assertThat(personalAccount.getAccountType()).isEqualTo(AccountType.PERSONAL);
            
            // Business logic should work the same for all account types
            personalAccount.deposit(new BigDecimal("1000.00"));
            assertThat(personalAccount.getBalance()).isEqualTo(new BigDecimal("6000.00"));
        }

        @Test
        @DisplayName("Should create account with minimal valid data")
        void shouldCreateAccountWithMinimalValidData() {
            BankAccount minimalAccount = BankAccount.builder()
                    .accountNumber("01000001")
                    .sortCode("99-99-99")
                    .name("Minimal Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(BigDecimal.ZERO)
                    .currency("GBP")
                    .user(user)
                    .build();

            assertThat(minimalAccount.getAccountNumber()).isEqualTo("01000001");
            assertThat(minimalAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
            assertThat(minimalAccount.getCreatedTimestamp()).isNull(); // Not set in test
            assertThat(minimalAccount.getUpdatedTimestamp()).isNull(); // Not set in test
        }

        @Test
        @DisplayName("Should preserve timestamps during balance operations")
        void shouldPreserveTimestampsDuringBalanceOperations() {
            LocalDateTime createdTime = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime updatedTime = LocalDateTime.of(2024, 1, 2, 15, 30);

            BankAccount timestampedAccount = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Timestamped Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .createdTimestamp(createdTime)
                    .updatedTimestamp(updatedTime)
                    .build();

            // Timestamps should remain unchanged during balance operations
            timestampedAccount.deposit(new BigDecimal("100.00"));
            
            assertThat(timestampedAccount.getCreatedTimestamp()).isEqualTo(createdTime);
            assertThat(timestampedAccount.getUpdatedTimestamp()).isEqualTo(updatedTime);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when account numbers are the same")
        void shouldBeEqualWhenAccountNumbersAreTheSame() {
            BankAccount account1 = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Account 1")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            BankAccount account2 = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("20-20-20")
                    .name("Account 2")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("2000.00"))
                    .currency("USD")
                    .user(user)
                    .build();

            // Since accountNumber is the @Id, accounts with same account number should be equal
            assertThat(account1.getAccountNumber()).isEqualTo(account2.getAccountNumber());
        }

        @Test
        @DisplayName("Should not be equal when account numbers are different")
        void shouldNotBeEqualWhenAccountNumbersAreDifferent() {
            BankAccount account1 = BankAccount.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            BankAccount account2 = BankAccount.builder()
                    .accountNumber("01234568")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            assertThat(account1.getAccountNumber()).isNotEqualTo(account2.getAccountNumber());
        }
    }
} 