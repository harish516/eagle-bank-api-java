package com.eaglebank.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BankAccount {

    @Id
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^01\\d{6}$", message = "Account number must match pattern ^01\\d{6}$")
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @NotBlank(message = "Sort code is required")
    @Column(name = "sort_code", nullable = false)
    private String sortCode;

    @NotBlank(message = "Account name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance must be at least 0")
    @DecimalMax(value = "10000.00", message = "Balance must not exceed 10000")
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @NotBlank(message = "Currency is required")
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @LastModifiedDate
    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    // Business logic methods - domain behavior
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }

    public boolean hasSufficientFunds(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
} 