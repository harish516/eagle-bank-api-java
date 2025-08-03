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
    
    @Pattern(regexp = ".*\\S.*", message = "Name cannot be empty or contain only whitespace")
    @Schema(description = "Account name (optional)", example = "Updated Personal Bank Account")
    private String name;
    
    @Pattern(regexp = "personal", message = "Account type must be 'personal'")
    @Schema(description = "Account type (optional)", example = "personal")
    private String accountType;
    
    /**
     * Validates that at least one field is provided for update.
     * Throws IllegalArgumentException if both name and accountType are null or empty.
     */
    public void validate() {
        boolean nameIsEmpty = name == null || name.trim().isEmpty();
        boolean accountTypeIsEmpty = accountType == null || accountType.trim().isEmpty();
        
        if (nameIsEmpty && accountTypeIsEmpty) {
            throw new IllegalArgumentException("At least one field (name or accountType) must be provided for update");
        }
    }
}
