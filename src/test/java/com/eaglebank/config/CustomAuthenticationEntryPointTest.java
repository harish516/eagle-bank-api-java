package com.eaglebank.config;

import com.eaglebank.dto.ErrorResponse;
import com.eaglebank.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAuthenticationEntryPoint Tests")
class CustomAuthenticationEntryPointTest {

    @Mock
    private AuditService auditService;
    
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AuthenticationException authenticationException;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper, auditService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authenticationException = new BadCredentialsException("Bad credentials");
    }

    @Test
    @DisplayName("Should return HTTP 401 Unauthorized status")
    void shouldReturnUnauthorizedStatus() throws IOException {
        // Given
        request.setRequestURI("/api/v1/secure");

        // When
        customAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Then
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Should set content type to application/json")
    void shouldSetJsonContentType() throws IOException {
        // Given
        request.setRequestURI("/api/v1/secure");

        // When
        customAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Then
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Should return proper error response structure")
    void shouldReturnProperErrorResponseStructure() throws IOException {
        // Given
        request.setRequestURI("/api/v1/secure");
        LocalDateTime beforeCall = LocalDateTime.now();

        // When
        customAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Then
        String responseContent = response.getContentAsString();
        assertThat(responseContent).isNotEmpty();

        // Parse the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);
        
        assertThat(errorResponse.getMessage()).isEqualTo("Unauthorized");
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isAfterOrEqualTo(beforeCall);
        assertThat(errorResponse.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should serialize timestamp in ISO format, not as numeric timestamp")
    void shouldSerializeTimestampInIsoFormat() throws IOException {
        // Given
        request.setRequestURI("/api/v1/secure");

        // When
        customAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Then
        String responseContent = response.getContentAsString();
        
        // Should contain ISO formatted timestamp (e.g., "2024-01-15T10:30:00")
        assertThat(responseContent).contains("T");
        assertThat(responseContent).contains(":");
        assertThat(responseContent).contains("-");
        
        // Should NOT contain numeric timestamp
        assertThat(responseContent).doesNotMatch(".*\"timestamp\":\\s*\\d+.*");
    }

    @Test
    @DisplayName("Should handle different authentication exceptions")
    void shouldHandleDifferentAuthenticationExceptions() throws IOException {
        // Test different types of AuthenticationException
        AuthenticationException[] exceptions = {
            new BadCredentialsException("Invalid credentials"),
            new org.springframework.security.authentication.InsufficientAuthenticationException("Insufficient authentication"),
            new org.springframework.security.authentication.DisabledException("Account disabled"),
            new org.springframework.security.authentication.AccountExpiredException("Account expired")
        };

        for (AuthenticationException exception : exceptions) {
            // Given
            MockHttpServletRequest testRequest = new MockHttpServletRequest();
            MockHttpServletResponse testResponse = new MockHttpServletResponse();
            testRequest.setRequestURI("/api/v1/test");

            // When
            customAuthenticationEntryPoint.commence(testRequest, testResponse, exception);

            // Then
            assertThat(testResponse.getStatus()).isEqualTo(401);
            assertThat(testResponse.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
            
            String responseContent = testResponse.getContentAsString();
            assertThat(responseContent).contains("Unauthorized");
        }
    }

    @Test
    @DisplayName("Should handle different request URIs correctly")
    void shouldHandleDifferentRequestUris() throws IOException {
        // Given
        String[] testUris = {
            "/api/v1/users",
            "/api/v1/accounts/123",
            "/admin/login",
            "/api/v1/protected"
        };

        for (String uri : testUris) {
            // Given
            MockHttpServletRequest testRequest = new MockHttpServletRequest();
            MockHttpServletResponse testResponse = new MockHttpServletResponse();
            testRequest.setRequestURI(uri);

            // When
            customAuthenticationEntryPoint.commence(testRequest, testResponse, authenticationException);

            // Then
            assertThat(testResponse.getStatus()).isEqualTo(401);
            assertThat(testResponse.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
            
            String responseContent = testResponse.getContentAsString();
            assertThat(responseContent).contains("Unauthorized");
        }
    }

    @Test
    @DisplayName("Should create valid JSON response")
    void shouldCreateValidJsonResponse() throws IOException {
        // Given
        request.setRequestURI("/api/v1/secure");

        // When
        customAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Then
        String responseContent = response.getContentAsString();
        
        // Verify it's valid JSON by parsing it
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        
        // This should not throw an exception if JSON is valid
        ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
    }

    @Test
    @DisplayName("Should handle null request URI gracefully")
    void shouldHandleNullRequestUriGracefully() throws IOException {
        // Given
        request.setRequestURI(null);

        // When & Then - Should not throw exception
        customAuthenticationEntryPoint.commence(request, response, authenticationException);
        
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Should handle empty request URI gracefully")
    void shouldHandleEmptyRequestUriGracefully() throws IOException {
        // Given
        request.setRequestURI("");

        // When & Then - Should not throw exception
        customAuthenticationEntryPoint.commence(request, response, authenticationException);
        
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Should handle null authentication exception gracefully")
    void shouldHandleNullAuthenticationExceptionGracefully() throws IOException {
        // Given
        request.setRequestURI("/api/v1/secure");

        // When & Then - Should not throw exception
        customAuthenticationEntryPoint.commence(request, response, null);
        
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        
        String responseContent = response.getContentAsString();
        assertThat(responseContent).contains("Unauthorized");
    }
}
