package com.eaglebank.controller;

import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        log.info("Getting user by ID: {}", userId);
        try {
            UserResponse response = userService.getUserById(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("User not found: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String userId, 
                                                 @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user with ID: {}", userId);
        try {
            UserResponse response = userService.updateUser(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("User not found for update: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        log.info("Deleting user with ID: {}", userId);
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("User not found for deletion: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Cannot delete user with associated accounts: {}", userId);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Getting all users");
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }
} 