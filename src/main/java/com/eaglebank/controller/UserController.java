package com.eaglebank.controller;

import com.eaglebank.dto.BadRequestErrorResponse;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.ErrorResponse;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.exception.CustomAccessDeniedException;
import com.eaglebank.service.interfaces.UserServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users in the Eagle Bank system")
public class UserController extends BaseController {

    private final UserServiceInterface userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user account in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "User with email already exists",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"User with email already exists\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves user details by user ID. Users can only access their own data.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only access their own data",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Forbidden - User can only access their own data\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"User not found\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable String userId, 
            Authentication authentication) {
        log.info("Getting user by ID: {}", userId);
        
        // Extract email from JWT token (this should be the primary identifier)
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        UserResponse requestedUser = userService.getUserById(userId);
        
        // Check if the authenticated user is requesting their own data by comparing emails
        if (!authenticatedEmail.equals(requestedUser.getEmail())) {
            log.warn("User with email {} attempted to access data for user ID: {} with email: {}", 
                authenticatedEmail, userId, requestedUser.getEmail());
            throw new IllegalStateException("Forbidden - User can only access their own data");
        }
        
        return ResponseEntity.ok(requestedUser);
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Update user", description = "Updates user details. Users can only update their own data.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only update their own data",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Forbidden - User can only update their own data\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"User not found\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable String userId, 
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        log.info("Updating user with ID: {}", userId);
        
        // Extract email from JWT token
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // First check if the user exists and if the authenticated user can access it
        UserResponse existingUser = userService.getUserById(userId);
        if (!authenticatedEmail.equals(existingUser.getEmail())) {
            log.warn("User with email {} attempted to update data for user ID: {} with email: {}", 
                authenticatedEmail, userId, existingUser.getEmail());
            throw new IllegalStateException("Forbidden - User can only access their own data");
        }
        
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Deletes a user account. Users can only delete their own account.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only delete their own account",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Forbidden - User can only delete their own account\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"User not found\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete user with associated accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Cannot delete user with associated accounts\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable String userId, 
            Authentication authentication) {
        log.info("Deleting user with ID: {}", userId);
        
        // Extract email from JWT token
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // First check if the user exists and if the authenticated user can access it
        UserResponse existingUser = userService.getUserById(userId);
        if (!authenticatedEmail.equals(existingUser.getEmail())) {
            log.warn("User with email {} attempted to delete data for user ID: {} with email: {}", 
                authenticatedEmail, userId, existingUser.getEmail());
            throw new IllegalStateException("Forbidden - User can only access their own data");
        }
        
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves the details of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current user details",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Authenticated user not found in database",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Authenticated user not found in database\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Getting current user details");
        
        // Extract email from JWT token
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        UserResponse response = userService.getUserByEmail(authenticatedEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users (Admin operation)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of users",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Getting all users");
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }
} 