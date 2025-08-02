package com.eaglebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response for validation failures")
public class BadRequestErrorResponse {
    @Schema(description = "General error message", example = "Validation failed")
    private String message;
    
    @Schema(description = "Field-specific validation errors", 
            example = "{\"email\": \"must be a valid email address\", \"firstName\": \"must not be blank\"}")
    private Map<String, String> details;
    
    @Schema(description = "Timestamp when the error occurred", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
} 