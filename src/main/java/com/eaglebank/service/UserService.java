package com.eaglebank.service;

import com.eaglebank.domain.User;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return mapToUserResponse(user);
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
        // This would need to be implemented when BankAccount entity is created
        // For now, we'll just delete the user
        
        userRepository.delete(user);
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