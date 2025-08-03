package com.eaglebank.dto;

import com.eaglebank.domain.Address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing user")
public class UpdateUserRequest {
 
    @Schema(description = "Full name of the user", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Valid
    @Schema(description = "User's address information")
    private Address address;
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", 
             message = "Phone number must be in international format")
    @Schema(description = "Phone number in international format", example = "+1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;
    
    @Email(message = "Email must be in valid format")
    @Schema(description = "Email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

} 