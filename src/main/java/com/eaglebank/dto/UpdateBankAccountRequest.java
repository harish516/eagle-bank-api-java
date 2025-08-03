package com.eaglebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing bank account")
public class UpdateBankAccountRequest {
    
    @Schema(description = "Account name (optional)", example = "Updated Personal Bank Account")
    @Pattern(regexp = ".*\\S.*", message = "Name cannot be empty or contain only whitespace")
    private String name;
    
    @Schema(description = "Account type (optional)", example = "personal")
    @Pattern(regexp = "personal", message = "Account type must be 'personal'")
    private String accountType;
}
