package com.eaglebank.dto;

import com.eaglebank.domain.Currency;
import com.eaglebank.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new transaction")
public class CreateTransactionRequest {
    
    @Schema(description = "Currency amount with up to two decimal places", example = "1000.00", required = true)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount must be at least 0.00")
    @DecimalMax(value = "10000.00", message = "Amount must not exceed 10000.00")
    private BigDecimal amount;
    
    @Schema(description = "Transaction currency", example = "GBP", required = true)
    @NotNull(message = "Currency is required")
    private Currency currency;
    
    @Schema(description = "Transaction type", example = "DEPOSIT", required = true)
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @Schema(description = "Transaction reference", example = "Monthly salary deposit")
    private String reference;

    /**
     * Validates the transaction request fields and business rules.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        // Validate required fields
        if (amount == null) {
            throw new IllegalArgumentException("Transaction amount is required");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Transaction currency is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        // Validate amount constraints
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
        if (amount.compareTo(new BigDecimal("10000.00")) > 0) {
            throw new IllegalArgumentException("Transaction amount must not exceed 10000.00");
        }
    }
}
