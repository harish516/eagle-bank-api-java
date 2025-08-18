package com.eaglebank.dto;

import com.eaglebank.domain.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite request to create a User and an initial Bank Account atomically.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new user along with an initial bank account")
public class CreateUserAndAccountRequest {

    // User fields
    @NotBlank(message = "Name is required")
    @Schema(description = "Full name of the user", example = "Jane Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Valid
    @Schema(description = "User address information")
    private Address address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in international format")
    @Schema(description = "Phone number in international format", example = "+447911123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in valid format")
    @Schema(description = "Email address", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    // Bank account fields
    @NotBlank(message = "Account name is required")
    @Schema(description = "Initial bank account name", example = "Jane Personal Account", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountName;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "personal", message = "Account type must be 'personal'")
    @Schema(description = "Account type (currently only 'personal')", example = "personal", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountType;
}
