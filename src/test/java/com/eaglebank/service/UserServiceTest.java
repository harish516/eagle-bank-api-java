package com.eaglebank.service;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.User;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
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

        testUser = User.builder()
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
    void shouldCreateUserSuccessfully() {
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.createUser(createUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("usr-abc123");
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with email already exists");

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getUserById("usr-abc123");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("usr-abc123");
        assertThat(result.getName()).isEqualTo("Test User");

        verify(userRepository).findById("usr-abc123");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById("usr-nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("usr-nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById("usr-nonexistent");
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("usr-abc123", updateUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated User");
        assertThat(result.getPhoneNumber()).isEqualTo("+44987654321");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");

        verify(userRepository).findById("usr-abc123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        userService.deleteUser("usr-abc123");

        verify(userRepository).findById("usr-abc123");
        verify(userRepository).delete(testUser);
    }

    @Test
    void shouldThrowExceptionWhenDeletingUserWithAccounts() {
        // This test would require checking if user has associated bank accounts
        // Implementation would depend on business logic
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));

        // Mock the check for associated accounts
        // This is a placeholder for the actual implementation
        assertThatThrownBy(() -> userService.deleteUser("usr-abc123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete user with associated bank accounts");

        verify(userRepository).findById("usr-abc123");
        verify(userRepository, never()).delete(any(User.class));
    }
} 