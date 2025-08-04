package com.eaglebank.service;

import com.eaglebank.service.interfaces.UserServiceInterface;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.User;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.exception.UserNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.util.LoggingUtils;
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
public class UserService implements UserServiceInterface {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user - email: {}, name: {}", 
                LoggingUtils.safeEmailId(request.getEmail()), request.getName());
        long startTime = System.currentTimeMillis();
        
        try {
            // Check if user with this email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("User creation failed - email already exists: {}", 
                        LoggingUtils.maskEmail(request.getEmail()));
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
            long duration = System.currentTimeMillis() - startTime;
            log.info("User created successfully - id: {}, email: {}, duration: {}ms", 
                    LoggingUtils.maskUserId(savedUser.getId()), 
                    LoggingUtils.maskEmail(savedUser.getEmail()), duration);
            return mapToUserResponse(savedUser);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("User creation failed - email: {}, duration: {}ms, error: {}", 
                    LoggingUtils.maskEmail(request.getEmail()), duration, e.getMessage(), e);
            throw e;
        }
    }

    public UserResponse getUserById(String userId) {
        log.info("Getting user by ID: {}", LoggingUtils.maskUserId(userId));
        
        // Validate user ID
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be null or empty");
        }
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            return mapToUserResponse(user);
        } catch (UserNotFoundException e) {
            throw e; // Re-throw business exceptions as-is
        } catch (Exception e) {
            // Convert infrastructure exceptions to business exceptions
            throw new IllegalStateException("Failed to retrieve user with ID: " + userId, e);
        }
    }

    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", LoggingUtils.maskUserId(userId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

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
        log.info("Deleting user with ID: {}", LoggingUtils.maskUserId(userId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Check if user has associated accounts
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

    public UserResponse getUserByEmail(String email) {
        log.info("Getting user by email: {}", LoggingUtils.maskEmail(email));
        
        // Validate email input
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        
        try {
            User user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
            return mapToUserResponse(user);
        } catch (UserNotFoundException e) {
            throw e; // Re-throw business exceptions as-is
        } catch (Exception e) {
            // Convert infrastructure exceptions to business exceptions
            throw new IllegalStateException("Failed to retrieve user with email: " + email, e);
        }
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