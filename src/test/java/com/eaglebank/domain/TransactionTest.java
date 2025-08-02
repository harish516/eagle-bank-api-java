package com.eaglebank.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class TransactionTest {

    private Transaction transaction;
    private BankAccount bankAccount;
    private User user;

    @BeforeEach
    void setUp() {
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
    void shouldValidateTransactionIdFormat() {
        assertThatThrownBy(() -> Transaction.builder()
                .id("invalid-id")
                .amount(new BigDecimal("100.00"))
                .currency("GBP")
                .type(TransactionType.DEPOSIT)
                .bankAccount(bankAccount)
                .userId(user.getId())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction ID must match pattern");
    }

    @Test
    void shouldValidateAmountRange() {
        assertThatThrownBy(() -> Transaction.builder()
                .id("tan-123abc")
                .amount(new BigDecimal("-100.00"))
                .currency("GBP")
                .type(TransactionType.DEPOSIT)
                .bankAccount(bankAccount)
                .userId(user.getId())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be between 0 and 10000");

        assertThatThrownBy(() -> Transaction.builder()
                .id("tan-123abc")
                .amount(new BigDecimal("15000.00"))
                .currency("GBP")
                .type(TransactionType.DEPOSIT)
                .bankAccount(bankAccount)
                .userId(user.getId())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be between 0 and 10000");
    }

    @Test
    void shouldValidateCurrency() {
        assertThatThrownBy(() -> Transaction.builder()
                .id("tan-123abc")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .type(TransactionType.DEPOSIT)
                .bankAccount(bankAccount)
                .userId(user.getId())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency must be GBP");
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
} 