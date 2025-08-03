package com.eaglebank.dto;

import com.eaglebank.domain.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information response")
public class UserResponse {
    
    @Schema(description = "Unique user identifier", example = "usr-123456")
    private String id;
    
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;
    
    @Schema(description = "User's address information")
    private Address address;
    
    @Schema(description = "Phone number in international format", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Timestamp when user was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdTimestamp;
    
    @Schema(description = "Timestamp when user was last updated", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedTimestamp;
} 