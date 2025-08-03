package com.eaglebank.config;

import com.eaglebank.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAccessDeniedHandler Tests")
class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler customAccessDeniedHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AccessDeniedException accessDeniedException;

    @BeforeEach
    void setUp() {
        customAccessDeniedHandler = new CustomAccessDeniedHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        accessDeniedException = new AccessDeniedException("Access is denied");
    }

    @Test
    @DisplayName("Should return HTTP 403 Forbidden status")
    void shouldReturnForbiddenStatus() throws IOException {
        // Given
        request.setRequestURI("/api/v1/restricted");

        // When
        customAccessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("Should set content type to application/json")
    void shouldSetJsonContentType() throws IOException {
        // Given
        request.setRequestURI("/api/v1/restricted");

        // When
        customAccessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Should return proper error response structure")
    void shouldReturnProperErrorResponseStructure() throws IOException {
        // Given
        request.setRequestURI("/api/v1/restricted");
        LocalDateTime beforeCall = LocalDateTime.now();

        // When
        customAccessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        String responseContent = response.getContentAsString();
        assertThat(responseContent).isNotEmpty();

        // Parse the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);
        
        assertThat(errorResponse.getMessage()).isEqualTo("Access Denied");
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isAfterOrEqualTo(beforeCall);
        assertThat(errorResponse.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should serialize timestamp in ISO format, not as numeric timestamp")
    void shouldSerializeTimestampInIsoFormat() throws IOException {
        // Given
        request.setRequestURI("/api/v1/restricted");

        // When
        customAccessDeniedHandler.handle(request, response, accessDeniedException);

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
    @DisplayName("Should handle different request URIs correctly")
    void shouldHandleDifferentRequestUris() throws IOException {
        // Given
        String[] testUris = {
            "/api/v1/users",
            "/api/v1/accounts/123",
            "/admin/dashboard",
            "/api/v1/transactions"
        };

        for (String uri : testUris) {
            // Given
            MockHttpServletRequest testRequest = new MockHttpServletRequest();
            MockHttpServletResponse testResponse = new MockHttpServletResponse();
            testRequest.setRequestURI(uri);

            // When
            customAccessDeniedHandler.handle(testRequest, testResponse, accessDeniedException);

            // Then
            assertThat(testResponse.getStatus()).isEqualTo(403);
            assertThat(testResponse.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
            
            String responseContent = testResponse.getContentAsString();
            assertThat(responseContent).contains("Access Denied");
        }
    }

    @Test
    @DisplayName("Should create valid JSON response")
    void shouldCreateValidJsonResponse() throws IOException {
        // Given
        request.setRequestURI("/api/v1/restricted");

        // When
        customAccessDeniedHandler.handle(request, response, accessDeniedException);

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
        customAccessDeniedHandler.handle(request, response, accessDeniedException);
        
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Should handle empty request URI gracefully")
    void shouldHandleEmptyRequestUriGracefully() throws IOException {
        // Given
        request.setRequestURI("");

        // When & Then - Should not throw exception
        customAccessDeniedHandler.handle(request, response, accessDeniedException);
        
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }
}
