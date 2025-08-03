package com.eaglebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new bank account")
public class CreateBankAccountRequest {
    
    @Schema(description = "Account name", example = "Personal Bank Account")
    @NotBlank(message = "Account name is required")
    private String name;
    
    @Schema(description = "Account type", example = "personal")
    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "personal", message = "Account type must be 'personal'")
    private String accountType;
}
