package com.eaglebank.dto;

import com.eaglebank.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bank account information response")
public class BankAccountResponse {
    
    @Schema(description = "Account number with format ^01\\d{6}$", example = "01234567")
    @Pattern(regexp = "^01\\d{6}$", message = "Account number must follow format 01XXXXXX")
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    
    @Schema(description = "Sort code", example = "10-10-10")
    @NotBlank(message = "Sort code is required")
    private String sortCode;
    
    @Schema(description = "Account name", example = "Personal Savings Account")
    @NotBlank(message = "Account name is required")
    private String name;
    
    @Schema(description = "Account type", example = "personal")
    @NotBlank(message = "Account type is required")
    private String accountType;
    
    @Schema(description = "Account balance", example = "1500.75")
    @DecimalMin(value = "0.00", message = "Balance must be at least 0.00")
    @DecimalMax(value = "10000.00", message = "Balance must not exceed 10000.00")
    @NotNull(message = "Balance is required")
    private BigDecimal balance;
    
    @Schema(description = "Currency code", example = "GBP")
    @NotNull(message = "Currency is required")
    private Currency currency;
    
    @Schema(description = "Account creation timestamp")
    @NotNull(message = "Created timestamp is required")
    private LocalDateTime createdTimestamp;
    
    @Schema(description = "Last update timestamp")
    @NotNull(message = "Updated timestamp is required")
    private LocalDateTime updatedTimestamp;
}
