package com.eaglebank.controller;

import com.eaglebank.domain.Address;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.exception.UserNotFoundException;
import com.eaglebank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.eaglebank.config.JpaAuditingConfig;
import com.eaglebank.config.SecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse testUserResponse;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        testUserResponse = UserResponse.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        createUserRequest = CreateUserRequest.builder()
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .build();

        updateUserRequest = UpdateUserRequest.builder()
                .name("Updated User")
                .phoneNumber("+44987654321")
                .email("updated@example.com")
                .build();
    }

    @Test
    @WithMockUser
    void shouldCreateUserSuccessfully() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUserResponse);

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .jwt(builder -> builder.claim("email", "test@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("usr-abc123"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {
        when(userService.getUserById("usr-abc123")).thenReturn(testUserResponse);
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);

        mockMvc.perform(get("/api/v1/users/usr-abc123")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-abc123"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(userService).getUserById("usr-abc123");
    }

    @Test
    @WithMockUser
    void shouldUpdateUserSuccessfully() throws Exception {
        UserResponse updatedResponse = UserResponse.builder()
                .id("usr-abc123")
                .name("Updated User")
                .phoneNumber("+44987654321")
                .email("updated@example.com")
                .build();

        when(userService.updateUser(eq("usr-abc123"), any(UpdateUserRequest.class)))
                .thenReturn(updatedResponse);
        when(userService.getUserById("usr-abc123")).thenReturn(updatedResponse);


        mockMvc.perform(patch("/api/v1/users/usr-abc123")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .jwt(builder -> builder.claim("email", "updated@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.phoneNumber").value("+44987654321"));

        verify(userService).updateUser(eq("usr-abc123"), any(UpdateUserRequest.class));
    }

    @Test
    @WithMockUser
    void shouldDeleteUserSuccessfully() throws Exception {
        doNothing().when(userService).deleteUser("usr-abc123");
        when(userService.getUserById("usr-abc123")).thenReturn(testUserResponse);

        mockMvc.perform(delete("/api/v1/users/usr-abc123")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("usr-abc123");
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidUserData() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .name("")
                .phoneNumber("invalid-phone")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .jwt(builder -> builder.claim("email", "test@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        when(userService.getUserById("usr-nonexistent"))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/usr-nonexistent")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isNotFound());

        verify(userService).getUserById("usr-nonexistent");
    }

    @Test
    @WithMockUser
    void shouldReturnConflictWhenDeletingUserWithAccounts() throws Exception {
        doThrow(new IllegalStateException("Cannot delete user with associated bank accounts"))
                .when(userService).deleteUser("usr-abc123");
         when(userService.getUserById("usr-abc123")).thenReturn(testUserResponse);

        mockMvc.perform(delete("/api/v1/users/usr-abc123")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isConflict());

        verify(userService).deleteUser("usr-abc123");
    }
} 