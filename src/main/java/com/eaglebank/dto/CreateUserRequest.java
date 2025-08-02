package com.eaglebank.dto;

import com.eaglebank.domain.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Valid
    private Address address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", 
             message = "Phone number must be in international format")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in valid format")
    private String email;
} 