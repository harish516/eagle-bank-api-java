package com.eaglebank.integration;

import com.eaglebank.domain.Address;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
@Testcontainers
class UserIntegrationTest {

    @Container
    static final GenericContainer<?> keycloak = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:latest"))
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withCommand("start-dev");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void shouldCreateUserSuccessfully() throws Exception {
        Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        CreateUserRequest request = CreateUserRequest.builder()
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+44123456789"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidUserData() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("")
                .phoneNumber("invalid-phone")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldGetUserByIdSuccessfully() throws Exception {
        // First create a user
        Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        CreateUserRequest createRequest = CreateUserRequest.builder()
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();

        String response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract user ID from response
        String userId = objectMapper.readTree(response).get("id").asText();

        // Get user by ID
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @WithMockUser
    void shouldUpdateUserSuccessfully() throws Exception {
        // First create a user
        Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        CreateUserRequest createRequest = CreateUserRequest.builder()
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();

        String response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        // Update user
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .name("Updated User")
                .phoneNumber("+44987654321")
                .email("updated@example.com")
                .build();

        mockMvc.perform(patch("/api/v1/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.phoneNumber").value("+44987654321"));
    }

    @Test
    @WithMockUser
    void shouldDeleteUserSuccessfully() throws Exception {
        // First create a user
        Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        CreateUserRequest createRequest = CreateUserRequest.builder()
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();

        String response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        // Delete user
        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isNotFound());
    }
} 