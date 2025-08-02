package com.eaglebank.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class BankAccountTest {

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
    void shouldValidateAccountNumberFormat() {
        assertThatThrownBy(() -> BankAccount.builder()
                .accountNumber("invalid-account")
                .sortCode("10-10-10")
                .name("Personal Bank Account")
                .accountType(AccountType.PERSONAL)
                .balance(new BigDecimal("1000.00"))
                .currency("GBP")
                .user(user)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number must match pattern");
    }

    @Test
    void shouldValidateBalanceRange() {
        assertThatThrownBy(() -> BankAccount.builder()
                .accountNumber("01234567")
                .sortCode("10-10-10")
                .name("Personal Bank Account")
                .accountType(AccountType.PERSONAL)
                .balance(new BigDecimal("-100.00"))
                .currency("GBP")
                .user(user)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Balance must be between 0 and 10000");

        assertThatThrownBy(() -> BankAccount.builder()
                .accountNumber("01234567")
                .sortCode("10-10-10")
                .name("Personal Bank Account")
                .accountType(AccountType.PERSONAL)
                .balance(new BigDecimal("15000.00"))
                .currency("GBP")
                .user(user)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Balance must be between 0 and 10000");
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
} 