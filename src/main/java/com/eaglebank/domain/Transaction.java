package com.eaglebank.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @Pattern(regexp = "^tan-[A-Za-z0-9]+$", message = "Transaction ID must match pattern ^tan-[A-Za-z0-9]+$")
    @Column(name = "id", nullable = false)
    private String id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount must be at least 0")
    @DecimalMax(value = "10000.00", message = "Amount must not exceed 10000")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "GBP", message = "Currency must be GBP")
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "reference")
    private String reference;

    @NotNull(message = "Bank account is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @NotBlank(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @CreatedDate
    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    // Business logic method - domain behavior
    public void process() {
        switch (type) {
            case DEPOSIT:
                bankAccount.deposit(amount);
                break;
            case WITHDRAWAL:
                bankAccount.withdraw(amount);
                break;
            default:
                throw new IllegalStateException("Unknown transaction type: " + type);
        }
    }
} 