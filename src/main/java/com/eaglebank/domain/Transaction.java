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
    @Column(name = "id", nullable = false)
    private String id;

    @DecimalMin(value = "0.00", message = "Amount must be at least 0")
    @DecimalMax(value = "10000.00", message = "Amount must not exceed 10000")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Column(name = "currency", nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "reference")
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @CreatedDate
    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @PrePersist
    @PreUpdate
    public void validate() {
        validateId();
        validateAmount();
        validateCurrency();
    }

    private void validateId() {
        if (id != null && !id.matches("^tan-[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("Transaction ID must match pattern ^tan-[A-Za-z0-9]+$");
        }
    }

    private void validateAmount() {
        if (amount != null && (amount.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(new BigDecimal("10000.00")) > 0)) {
            throw new IllegalArgumentException("Amount must be between 0 and 10000");
        }
    }

    private void validateCurrency() {
        if (currency != null && !"GBP".equals(currency)) {
            throw new IllegalArgumentException("Currency must be GBP");
        }
    }

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