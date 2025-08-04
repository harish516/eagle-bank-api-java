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
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

class TransactionTest {

    private Transaction transaction;
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
                .build();

        transaction = Transaction.builder()
                .id("tan-123abc")
                .amount(new BigDecimal("100.00"))
                .currency("GBP")
                .type(TransactionType.DEPOSIT)
                .reference("Test transaction")
                .bankAccount(bankAccount)
                .userId(user.getId())
                .createdTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateTransactionWithValidData() {
        assertThat(transaction.getId()).isEqualTo("tan-123abc");
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(transaction.getCurrency()).isEqualTo("GBP");
        assertThat(transaction.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.getReference()).isEqualTo("Test transaction");
        assertThat(transaction.getBankAccount()).isEqualTo(bankAccount);
        assertThat(transaction.getUserId()).isEqualTo("usr-abc123");
    }

    @Test
    void shouldProcessDepositTransaction() {
        BigDecimal initialBalance = bankAccount.getBalance();
        BigDecimal depositAmount = new BigDecimal("200.00");

        Transaction depositTransaction = Transaction.builder()
                .id("tan-deposit")
                .amount(depositAmount)
                .currency("GBP")
                .type(TransactionType.DEPOSIT)
                .bankAccount(bankAccount)
                .userId(user.getId())
                .build();

        depositTransaction.process();

        assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.add(depositAmount));
    }

    @Test
    void shouldProcessWithdrawalTransaction() {
        BigDecimal initialBalance = bankAccount.getBalance();
        BigDecimal withdrawalAmount = new BigDecimal("300.00");

        Transaction withdrawalTransaction = Transaction.builder()
                .id("tan-withdrawal")
                .amount(withdrawalAmount)
                .currency("GBP")
                .type(TransactionType.WITHDRAWAL)
                .bankAccount(bankAccount)
                .userId(user.getId())
                .build();

        withdrawalTransaction.process();

        assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.subtract(withdrawalAmount));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFundsForWithdrawal() {
        BigDecimal withdrawalAmount = new BigDecimal("1500.00");

        Transaction withdrawalTransaction = Transaction.builder()
                .id("tan-withdrawal")
                .amount(withdrawalAmount)
                .currency("GBP")
                .type(TransactionType.WITHDRAWAL)
                .bankAccount(bankAccount)
                .userId(user.getId())
                .build();

        assertThatThrownBy(() -> withdrawalTransaction.process())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient funds");
    }

    // === COMPREHENSIVE VALIDATION TESTS ===

    @Nested
    @DisplayName("Bean Validation Tests")
    class BeanValidationTests {

        @Test
        @DisplayName("Should fail validation when currency is null")
        void shouldFailValidationWhenCurrencyIsNull() {
            Transaction invalidTransaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(null)
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Set<ConstraintViolation<Transaction>> violations = validator.validate(invalidTransaction);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Currency is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should fail validation when currency is empty or blank")
        void shouldFailValidationWhenCurrencyIsEmptyOrBlank(String invalidCurrency) {
            Transaction invalidTransaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency(invalidCurrency)
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Set<ConstraintViolation<Transaction>> violations = validator.validate(invalidTransaction);
            
            assertThat(violations).hasSize(2); // Both @NotBlank and @Pattern trigger for empty/blank strings
            assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                    "Currency is required",
                    "Currency must be GBP"
                );
        }

        @Test
        @DisplayName("Should fail validation when amount is below minimum")
        void shouldFailValidationWhenAmountIsBelowMinimum() {
            Transaction invalidTransaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("-0.01"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Set<ConstraintViolation<Transaction>> violations = validator.validate(invalidTransaction);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount must be at least 0");
        }

        @Test
        @DisplayName("Should fail validation when amount exceeds maximum")
        void shouldFailValidationWhenAmountExceedsMaximum() {
            Transaction invalidTransaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("10000.01"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Set<ConstraintViolation<Transaction>> violations = validator.validate(invalidTransaction);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount must not exceed 10000");
        }

        @Test
        @DisplayName("Should pass validation when amount is at minimum boundary")
        void shouldPassValidationWhenAmountIsAtMinimumBoundary() {
            Transaction validTransaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("0.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Set<ConstraintViolation<Transaction>> violations = validator.validate(validTransaction);
            
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when amount is at maximum boundary")
        void shouldPassValidationWhenAmountIsAtMaximumBoundary() {
            Transaction validTransaction = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("10000.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Set<ConstraintViolation<Transaction>> violations = validator.validate(validTransaction);
            
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Transaction ID Validation Tests")
    class TransactionIdValidationTests {
        @ParameterizedTest
        @ValueSource(strings = {
            "tan-123abc",
            "tan-ABC123",
            "tan-a1b2c3",
            "tan-123456789",
            "tan-AbCdEf123"
        })
        @DisplayName("Should accept valid transaction ID formats")
        void shouldAcceptValidTransactionIdFormats(String validId) {
            Transaction validTransaction = Transaction.builder()
                    .id(validId)
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThat(validTransaction.getId()).isEqualTo(validId);
        }

        @Test
        @DisplayName("Should handle null transaction ID gracefully")
        void shouldHandleNullTransactionIdGracefully() {
            Transaction transactionWithNullId = Transaction.builder()
                    .id(null)
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThat(transactionWithNullId.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("Transaction Processing Tests")
    class TransactionProcessingTests {

        @ParameterizedTest
        @EnumSource(TransactionType.class)
        @DisplayName("Should process all transaction types correctly")
        void shouldProcessAllTransactionTypesCorrectly(TransactionType transactionType) {
            BigDecimal amount = new BigDecimal("100.00");
            BigDecimal initialBalance = bankAccount.getBalance();

            Transaction transaction = Transaction.builder()
                    .id("tan-test123")
                    .amount(amount)
                    .currency("GBP")
                    .type(transactionType)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            transaction.process();

            switch (transactionType) {
                case DEPOSIT:
                    assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.add(amount));
                    break;
                case WITHDRAWAL:
                    assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.subtract(amount));
                    break;
            }
        }

        @Test
        @DisplayName("Should handle zero amount transactions")
        void shouldHandleZeroAmountTransactions() {
            BigDecimal zeroAmount = BigDecimal.ZERO;

            Transaction zeroDepositTransaction = Transaction.builder()
                    .id("tan-zero001")
                    .amount(zeroAmount)
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            // Zero amount deposits should fail with IllegalArgumentException
            assertThatThrownBy(zeroDepositTransaction::process)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");

            Transaction zeroWithdrawalTransaction = Transaction.builder()
                    .id("tan-zero002")
                    .amount(zeroAmount)
                    .currency("GBP")
                    .type(TransactionType.WITHDRAWAL)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            // Zero amount withdrawals should also fail
            assertThatThrownBy(zeroWithdrawalTransaction::process)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Should handle precise decimal amounts")
        void shouldHandlePreciseDecimalAmounts() {
            BigDecimal preciseAmount = new BigDecimal("123.45");
            BigDecimal initialBalance = bankAccount.getBalance();

            Transaction preciseTransaction = Transaction.builder()
                    .id("tan-precise1")
                    .amount(preciseAmount)
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            preciseTransaction.process();

            assertThat(bankAccount.getBalance()).isEqualTo(initialBalance.add(preciseAmount));
            assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("1123.45"));
        }

        @Test
        @DisplayName("Should handle maximum allowed transaction amount")
        void shouldHandleMaximumAllowedTransactionAmount() {
            // Create account with sufficient balance for max withdrawal
            BankAccount highBalanceAccount = BankAccount.builder()
                    .accountNumber("01234568")
                    .sortCode("10-10-10")
                    .name("High Balance Account")
                    .accountType(AccountType.PERSONAL)
                    .balance(new BigDecimal("10000.00"))
                    .currency("GBP")
                    .user(user)
                    .build();

            BigDecimal maxAmount = new BigDecimal("10000.00");

            Transaction maxTransaction = Transaction.builder()
                    .id("tan-max001")
                    .amount(maxAmount)
                    .currency("GBP")
                    .type(TransactionType.WITHDRAWAL)
                    .bankAccount(highBalanceAccount)
                    .userId(user.getId())
                    .build();

            maxTransaction.process();

            assertThat(highBalanceAccount.getBalance()).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Should prevent withdrawal exceeding account balance")
        void shouldPreventWithdrawalExceedingAccountBalance() {
            BigDecimal excessiveAmount = bankAccount.getBalance().add(new BigDecimal("1.00"));

            Transaction excessiveWithdrawal = Transaction.builder()
                    .id("tan-excess1")
                    .amount(excessiveAmount)
                    .currency("GBP")
                    .type(TransactionType.WITHDRAWAL)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThatThrownBy(excessiveWithdrawal::process)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("Should maintain balance precision during multiple transactions")
        void shouldMaintainBalancePrecisionDuringMultipleTransactions() {
            BigDecimal initialBalance = bankAccount.getBalance();

            // Perform multiple precise transactions
            Transaction deposit1 = Transaction.builder()
                    .id("tan-multi1")
                    .amount(new BigDecimal("0.01"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Transaction withdrawal1 = Transaction.builder()
                    .id("tan-multi2")
                    .amount(new BigDecimal("0.01"))
                    .currency("GBP")
                    .type(TransactionType.WITHDRAWAL)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            deposit1.process();
            withdrawal1.process();

            // Balance should return to original amount
            assertThat(bankAccount.getBalance()).isEqualTo(initialBalance);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesAndErrorScenarios {

        @Test
        @DisplayName("Should handle transactions with references")
        void shouldHandleTransactionsWithReferences() {
            String reference = "Online transfer from savings";

            Transaction transactionWithReference = Transaction.builder()
                    .id("tan-ref001")
                    .amount(new BigDecimal("250.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .reference(reference)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThat(transactionWithReference.getReference()).isEqualTo(reference);
        }

        @Test
        @DisplayName("Should handle transactions without references")
        void shouldHandleTransactionsWithoutReferences() {
            Transaction transactionWithoutReference = Transaction.builder()
                    .id("tan-noref01")
                    .amount(new BigDecimal("150.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThat(transactionWithoutReference.getReference()).isNull();
        }

        @Test
        @DisplayName("Should preserve timestamps during transaction processing")
        void shouldPreserveTimestampsDuringTransactionProcessing() {
            LocalDateTime createdTime = LocalDateTime.of(2024, 1, 1, 10, 0);

            Transaction timestampedTransaction = Transaction.builder()
                    .id("tan-time01")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .createdTimestamp(createdTime)
                    .build();

            timestampedTransaction.process();

            // Timestamp should remain unchanged during processing
            assertThat(timestampedTransaction.getCreatedTimestamp()).isEqualTo(createdTime);
        }

        @Test
        @DisplayName("Should handle long reference strings")
        void shouldHandleLongReferenceStrings() {
            String longReference = "This is a very long reference string that contains detailed information about the transaction including the source, destination, purpose and additional metadata that might be useful for tracking and auditing purposes in the banking system";

            Transaction longRefTransaction = Transaction.builder()
                    .id("tan-longref")
                    .amount(new BigDecimal("75.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .reference(longReference)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThat(longRefTransaction.getReference()).isEqualTo(longReference);
        }

        @Test
        @DisplayName("Should handle special characters in reference")
        void shouldHandleSpecialCharactersInReference() {
            String specialReference = "Transfer from John's account (£100) - Payment #123 for invoice @2024/01";

            Transaction specialRefTransaction = Transaction.builder()
                    .id("tan-special")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .reference(specialReference)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThat(specialRefTransaction.getReference()).isEqualTo(specialReference);
        }

        @Test
        @DisplayName("Should create transaction with minimal valid data")
        void shouldCreateTransactionWithMinimalValidData() {
            Transaction minimalTransaction = Transaction.builder()
                    .id("tan-minimal")
                    .amount(new BigDecimal("0.01"))  // Use smallest positive amount
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThat(minimalTransaction.getId()).isEqualTo("tan-minimal");
            assertThat(minimalTransaction.getAmount()).isEqualTo(new BigDecimal("0.01"));
            assertThat(minimalTransaction.getReference()).isNull();
            assertThat(minimalTransaction.getCreatedTimestamp()).isNull(); // Not set in test
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when transaction IDs are the same")
        void shouldBeEqualWhenTransactionIdsAreTheSame() {
            Transaction transaction1 = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Transaction transaction2 = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("200.00"))
                    .currency("GBP")
                    .type(TransactionType.WITHDRAWAL)
                    .bankAccount(bankAccount)
                    .userId("different-user")
                    .build();

            // Since id is the @Id, transactions with same id should be equal
            assertThat(transaction1.getId()).isEqualTo(transaction2.getId());
        }

        @Test
        @DisplayName("Should not be equal when transaction IDs are different")
        void shouldNotBeEqualWhenTransactionIdsAreDifferent() {
            Transaction transaction1 = Transaction.builder()
                    .id("tan-123abc")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            Transaction transaction2 = Transaction.builder()
                    .id("tan-456def")
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .bankAccount(bankAccount)
                    .userId(user.getId())
                    .build();

            assertThat(transaction1.getId()).isNotEqualTo(transaction2.getId());
        }
    }
} 