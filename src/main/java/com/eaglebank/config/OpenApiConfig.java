package com.eaglebank.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        // Ensure context path doesn't have double slashes
        String basePath = contextPath.equals("/") ? "" : contextPath;
        
        return new OpenAPI()
                .info(new Info()
                        .title("Eagle Bank API")
                        .description("REST API for Eagle Bank with Keycloak authentication")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Eagle Bank Development Team")
                                .email("dev@eaglebank.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + basePath)
                                .description("Local development server"),
                        new Server()
                                .url("https://api.eaglebank.com" + basePath)
                                .description("Production server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from Keycloak"))
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("OAuth2 authentication via Keycloak")
                                .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                                        .password(new io.swagger.v3.oas.models.security.OAuthFlow()
                                                .tokenUrl("http://eagle-bank-keycloak:8180/realms/eagle-bank/protocol/openid-connect/token")
                                                .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                                        .addString("openid", "OpenID Connect scope")
                                                        .addString("profile", "Profile information")
                                                        .addString("email", "Email address"))))))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("oauth2", List.of("openid", "profile", "email")));
    }
}
