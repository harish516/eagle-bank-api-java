package com.eaglebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Composite response containing created user and initial bank account details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after creating a user and an initial bank account")
public class CreateUserAndAccountResponse {

    // User
    @Schema(description = "User ID", example = "usr-abc123")
    private String userId;

    @Schema(description = "User name", example = "Jane Doe")
    private String name;

    @Schema(description = "User email", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "User creation timestamp")
    private LocalDateTime userCreatedTimestamp;

    // Bank Account
    @Schema(description = "Account number", example = "01123456")
    private String accountNumber;

    @Schema(description = "Sort code", example = "00-00-00")
    private String sortCode;

    @Schema(description = "Account name", example = "Jane Personal Account")
    private String accountName;

    @Schema(description = "Account type", example = "personal")
    private String accountType;

    @Schema(description = "Account balance", example = "0.00")
    private BigDecimal balance;

    @Schema(description = "Currency", example = "GBP")
    private String currency;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime accountCreatedTimestamp;
}
