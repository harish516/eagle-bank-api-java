package com.eaglebank.service;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.User;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.exception.UserNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

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

    /**
     * Input validation tests for UserService methods.
     * These tests ensure that the service methods handle invalid input correctly.
     */

    @Test
    void shouldThrowExceptionWhenCreatingUserWithInvalidData() {
        // Test validation of CreateUserRequest fields
        createUserRequest.setEmail("invalid-email");
        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");

        createUserRequest.setEmail("valid@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingUserWithInvalidData() {
        // Test validation of UpdateUserRequest fields
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));

        updateUserRequest.setEmail("invalid-email");
        assertThatThrownBy(() -> userService.updateUser("usr-abc123", updateUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must be in valid format");

        updateUserRequest.setEmail("valid@example.com");
    }

    @Test
    void shouldHandleNullOrEmptyUserIds() {
        // Test null/empty user ID parameters
        assertThatThrownBy(() -> userService.getUserById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null or empty");

        assertThatThrownBy(() -> userService.getUserById(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null or empty");
    }

    /**
     * Tests for UserService.createUser() method.
     * This method creates a new user based on the CreateUserRequest DTO and saves it to the repository.
     * It also checks if a user with the same email already exists before proceeding with creation.
     */

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

    /**
     * Tests for UserService.getUserById() method.
     * This method retrieves a user by their ID and maps it to a UserResponse DTO.
     */

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
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById("usr-nonexistent");
    }

    /**
     * Tests for UserService.updateUser() method.
     * This method updates an existing user with new details provided in the UpdateUserRequest DTO.
     */

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
    void shouldUpdateUserPartiallyWithOnlyName() {
        UpdateUserRequest partialUpdate = UpdateUserRequest.builder()
                .name("Partially Updated User")
                .build();

        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("usr-abc123", partialUpdate);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Partially Updated User");
        assertThat(result.getPhoneNumber()).isEqualTo("+44123456789");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById("usr-abc123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserPartiallyWithOnlyEmail() {
        UpdateUserRequest partialUpdate = UpdateUserRequest.builder()
                .email("updated@example.com")
                .build();

        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("usr-abc123", partialUpdate);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getPhoneNumber()).isEqualTo("+44123456789");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");

        verify(userRepository).findById("usr-abc123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserPartiallyWithOnlyPhoneNumber() {
        UpdateUserRequest partialUpdate = UpdateUserRequest.builder()
                .phoneNumber("+44987654321")
                .build();

        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("usr-abc123", partialUpdate);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getPhoneNumber()).isEqualTo("+44987654321");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldUpdateUserPartiallyWithOnlyAddress() {
        Address newAddress = Address.builder()
                .line1("456 Another Street")
                .town("Manchester")
                .county("Greater Manchester")
                .postcode("M1 1AA")
                .build();

        UpdateUserRequest partialUpdate = UpdateUserRequest.builder()
                .address(newAddress)
                .build();

        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("usr-abc123", partialUpdate);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getPhoneNumber()).isEqualTo("+44123456789");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getAddress()).isEqualTo(newAddress);

        verify(userRepository).findById("usr-abc123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {
        UpdateUserRequest partialUpdate = UpdateUserRequest.builder()
                .email("existing@example.com")
                .build();

        // Create a different user with the email we're trying to update to
        Address differentAddress = Address.builder()
                .line1("456 Different Street")
                .town("Manchester")
                .county("Greater Manchester")
                .postcode("M1 1AA")
                .build();
                
        User differentUser = User.builder()
                .id("usr-different")
                .name("Different User")
                .address(differentAddress)
                .phoneNumber("+44987654321")
                .email("existing@example.com")
                .build();

        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(differentUser));

        assertThatThrownBy(() -> userService.updateUser("usr-abc123", partialUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository).findById("usr-abc123");
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForUpdate() {
        when(userRepository.findById("usr-nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("usr-nonexistent", updateUserRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with ID");

        verify(userRepository).findById("usr-nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Tests for UserService.deleteUser() method.
     * This method deletes a user by ID, ensuring that the user exists and has no associated bank accounts before deletion.
     */

    @Test
    void shouldDeleteUserSuccessfully() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByUserId("usr-abc123")).thenReturn(Collections.emptyList());
        doNothing().when(userRepository).delete(testUser);

        userService.deleteUser("usr-abc123");

        verify(userRepository).findById("usr-abc123");
        verify(bankAccountRepository).findByUserId("usr-abc123");
        verify(userRepository).delete(testUser);
    }

    @Test
    void shouldThrowExceptionWhenDeletingUserWithAccounts() {
        // Mock that user exists
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        
        // Mock that user has associated bank accounts
        BankAccount mockAccount = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("123456")
                .name("Test Account")
                .user(testUser)
                .build();
        when(bankAccountRepository.findByUserId("usr-abc123")).thenReturn(List.of(mockAccount));

        assertThatThrownBy(() -> userService.deleteUser("usr-abc123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete user with associated bank accounts");

        verify(userRepository).findById("usr-abc123");
        verify(bankAccountRepository).findByUserId("usr-abc123");
        verify(userRepository, never()).delete(any(User.class));
    }

    /**
     * Tests for UserService.getAllUsers() method.
     * This method retrieves all users from the repository and maps them to UserResponse DTOs.
     */

    @Test
    void shouldGetAllUsersSuccessfully() {
        List<User> users = List.of(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("usr-abc123");
        assertThat(result.get(0).getName()).isEqualTo("Test User");

        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userRepository).findAll();
    }

    /**
     * Edge cases and exception handling tests for UserService methods.
     * These tests ensure that the service methods handle unexpected scenarios gracefully.
     */

    @Test
    void shouldHandleRepositoryExceptions() {
        // Test database/repository exceptions
        when(userRepository.findById("usr-abc123")).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> userService.getUserById("usr-abc123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to retrieve user");

        verify(userRepository).findById("usr-abc123");
    }

    @Test
    void shouldPreserveUserTimestampsCorrectly() {
        // Test createdTimestamp preservation during updates
        LocalDateTime originalCreatedTime = LocalDateTime.now().minusDays(1);
        LocalDateTime originalUpdatedTime = LocalDateTime.now().minusHours(1);
        
        testUser.setCreatedTimestamp(originalCreatedTime);
        testUser.setUpdatedTimestamp(originalUpdatedTime);
        
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("usr-abc123", updateUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getCreatedTimestamp()).isEqualTo(originalCreatedTime);
        assertThat(result.getUpdatedTimestamp()).isAfter(originalUpdatedTime);

        verify(userRepository).findById("usr-abc123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateTimestampOnUserUpdate() {
        // Test updatedTimestamp is updated correctly
        LocalDateTime originalUpdatedTime = LocalDateTime.now().minusHours(1);
        testUser.setUpdatedTimestamp(originalUpdatedTime);
        
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("usr-abc123", updateUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUpdatedTimestamp()).isAfter(originalUpdatedTime);

        verify(userRepository).findById("usr-abc123");
        verify(userRepository).save(any(User.class));
    }

    // Tests for getUserByEmail method
    @Test
    void shouldReturnUserWhenFoundByEmail() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("usr-abc123");
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+44123456789");

        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(bankAccountRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: nonexistent@example.com");

        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        // Given
        String email = null;

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be null or empty");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(bankAccountRepository);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        // Given
        String email = "";

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be null or empty");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(bankAccountRepository);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        // Given
        String email = "   ";

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be null or empty");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(bankAccountRepository);
    }

    @Test
    void shouldTrimEmailWhenSearching() {
        // Given
        String emailWithSpaces = "  test@example.com  ";
        String trimmedEmail = "test@example.com";
        when(userRepository.findByEmail(trimmedEmail)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserByEmail(emailWithSpaces);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findByEmail(trimmedEmail);
    }

    @Test
    void shouldHandleRepositoryExceptionInGetUserByEmail() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to retrieve user with email: test@example.com")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(userRepository).findByEmail(email);
    }

} 