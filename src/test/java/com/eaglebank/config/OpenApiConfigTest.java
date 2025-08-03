package com.eaglebank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenApiConfig Tests")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Test
    @DisplayName("Should create OpenAPI configuration with correct basic info")
    void shouldCreateOpenApiWithCorrectBasicInfo() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertThat(openAPI).isNotNull();
        
        Info info = openAPI.getInfo();
        assertThat(info.getTitle()).isEqualTo("Eagle Bank API");
        assertThat(info.getDescription()).isEqualTo("REST API for Eagle Bank with Keycloak authentication");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Should create OpenAPI with correct contact information")
    void shouldCreateOpenApiWithCorrectContactInfo() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("Eagle Bank Development Team");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("dev@eaglebank.com");
    }

    @Test
    @DisplayName("Should create OpenAPI with correct license information")
    void shouldCreateOpenApiWithCorrectLicenseInfo() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("MIT License");
        assertThat(openAPI.getInfo().getLicense().getUrl()).isEqualTo("https://opensource.org/licenses/MIT");
    }

    @Test
    @DisplayName("Should create servers with default port and root context path")
    void shouldCreateServersWithDefaultConfiguration() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        List<Server> servers = openAPI.getServers();
        assertThat(servers).hasSize(2);
        
        Server localServer = servers.get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:8080");
        assertThat(localServer.getDescription()).isEqualTo("Local development server");
        
        Server prodServer = servers.get(1);
        assertThat(prodServer.getUrl()).isEqualTo("https://api.eaglebank.com");
        assertThat(prodServer.getDescription()).isEqualTo("Production server");
    }

    @Test
    @DisplayName("Should create servers with custom port")
    void shouldCreateServersWithCustomPort() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "9090");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        List<Server> servers = openAPI.getServers();
        Server localServer = servers.get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:9090");
    }

    @Test
    @DisplayName("Should create servers with custom context path")
    void shouldCreateServersWithCustomContextPath() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/api");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        List<Server> servers = openAPI.getServers();
        
        Server localServer = servers.get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:8080/api");
        
        Server prodServer = servers.get(1);
        assertThat(prodServer.getUrl()).isEqualTo("https://api.eaglebank.com/api");
    }

    @Test
    @DisplayName("Should handle context path with leading slash correctly")
    void shouldHandleContextPathWithLeadingSlash() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/eagle-bank");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        List<Server> servers = openAPI.getServers();
        Server localServer = servers.get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:8080/eagle-bank");
    }

    @Test
    @DisplayName("Should create security schemes correctly")
    void shouldCreateSecuritySchemesCorrectly() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).hasSize(2);
        
        // Test Bearer Auth scheme
        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(bearerAuth).isNotNull();
        assertThat(bearerAuth.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(bearerAuth.getScheme()).isEqualTo("bearer");
        assertThat(bearerAuth.getBearerFormat()).isEqualTo("JWT");
        assertThat(bearerAuth.getDescription()).isEqualTo("JWT token obtained from Keycloak");
        
        // Test OAuth2 scheme
        SecurityScheme oauth2 = openAPI.getComponents().getSecuritySchemes().get("oauth2");
        assertThat(oauth2).isNotNull();
        assertThat(oauth2.getType()).isEqualTo(SecurityScheme.Type.OAUTH2);
        assertThat(oauth2.getDescription()).isEqualTo("OAuth2 authentication via Keycloak");
    }

    @Test
    @DisplayName("Should create OAuth2 flows correctly")
    void shouldCreateOAuth2FlowsCorrectly() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        SecurityScheme oauth2 = openAPI.getComponents().getSecuritySchemes().get("oauth2");
        assertThat(oauth2.getFlows()).isNotNull();
        assertThat(oauth2.getFlows().getPassword()).isNotNull();
        assertThat(oauth2.getFlows().getPassword().getTokenUrl())
            .isEqualTo("http://eagle-bank-keycloak:8180/realms/eagle-bank/protocol/openid-connect/token");
        
        // Test scopes
        assertThat(oauth2.getFlows().getPassword().getScopes()).isNotNull();
        assertThat(oauth2.getFlows().getPassword().getScopes()).containsKey("openid");
        assertThat(oauth2.getFlows().getPassword().getScopes()).containsKey("profile");
        assertThat(oauth2.getFlows().getPassword().getScopes()).containsKey("email");
    }

    @Test
    @DisplayName("Should create security requirements correctly")
    void shouldCreateSecurityRequirementsCorrectly() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "/");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        List<SecurityRequirement> securityRequirements = openAPI.getSecurity();
        assertThat(securityRequirements).hasSize(2);
        
        // Test bearerAuth requirement
        SecurityRequirement bearerAuthReq = securityRequirements.get(0);
        assertThat(bearerAuthReq.get("bearerAuth")).isNotNull();
        
        // Test oauth2 requirement
        SecurityRequirement oauth2Req = securityRequirements.get(1);
        assertThat(oauth2Req.get("oauth2")).containsExactly("openid", "profile", "email");
    }

    @Test
    @DisplayName("Should handle empty context path correctly")
    void shouldHandleEmptyContextPath() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
        ReflectionTestUtils.setField(openApiConfig, "contextPath", "");

        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        List<Server> servers = openAPI.getServers();
        Server localServer = servers.get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:8080");
    }
}
