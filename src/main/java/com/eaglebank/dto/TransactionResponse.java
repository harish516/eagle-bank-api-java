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
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing transaction details")
public class TransactionResponse {
    
    @Schema(description = "Transaction ID", example = "tan-123abc", required = true)
    @NotNull(message = "Transaction ID is required")
    @Pattern(regexp = "^tan-[A-Za-z0-9]+$", message = "Transaction ID must match pattern: tan-<alphanumeric>")
    private String id;
    
    @Schema(description = "Transaction amount", example = "1000.00", required = true)
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
    
    @Schema(description = "User ID", example = "usr-abc123")
    @Pattern(regexp = "^usr-[A-Za-z0-9]+$", message = "User ID must match pattern: usr-<alphanumeric>")
    private String userId;
    
    @Schema(description = "Transaction creation timestamp", required = true)
    @NotNull(message = "Created timestamp is required")
    private LocalDateTime createdTimestamp;
}
