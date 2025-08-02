package com.eaglebank.service;

import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.User;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
        // Check if user with this email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email already exists: " + request.getEmail());
        }
        
        User user = User.builder()
                .id("usr-" + UUID.randomUUID().toString().replace("-", ""))
                .name(request.getName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public UserResponse getUserById(String userId) {
        log.info("Getting user by ID: {}", userId);
        
        // Validate user ID
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be null or empty");
        }
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            return mapToUserResponse(user);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e; // Re-throw validation exceptions
            }
            throw new IllegalStateException("Failed to retrieve user with ID: " + userId, e);
        }
    }

    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            // Check if email is already in use by another user
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use by another user");
            }
            user.setEmail(request.getEmail());
        }

        user.setUpdatedTimestamp(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public void deleteUser(String userId) {
        log.info("Deleting user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Check if user has associated accounts
        // For now, we'll simulate this check - in a real implementation this would
        // check the BankAccount repository for accounts associated with this user
        // Since the test expects this to throw an exception, we'll implement basic logic
        if (hasAssociatedBankAccounts(userId)) {
            throw new IllegalStateException("Cannot delete user with associated bank accounts");
        }
        
        userRepository.delete(user);
    }
    
    private boolean hasAssociatedBankAccounts(String userId) {
        // Check if the user has any associated bank accounts
        List<BankAccount> userAccounts = bankAccountRepository.findByUserId(userId);
        return !userAccounts.isEmpty();
    }

    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .createdTimestamp(user.getCreatedTimestamp())
                .updatedTimestamp(user.getUpdatedTimestamp())
                .build();
    }
} 