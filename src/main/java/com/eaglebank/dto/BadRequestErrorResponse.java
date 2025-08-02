package com.eaglebank.dto;

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
public class BadRequestErrorResponse {
    private String message;
    private Map<String, String> details;
    private LocalDateTime timestamp;
} 