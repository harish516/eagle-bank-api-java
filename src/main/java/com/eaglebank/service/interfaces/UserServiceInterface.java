package com.eaglebank.service.interfaces;

import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;

import java.util.List;

/**
 * Interface for User service operations.
 * Defines the contract for user management functionality.
 */
public interface UserServiceInterface {

    /**
     * Creates a new user in the system.
     *
     * @param request the user creation request containing user details
     * @return the created user response
     * @throws IllegalArgumentException if the request is invalid
     * @throws RuntimeException if a user with the same email already exists
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param userId the unique identifier of the user
     * @return the user response
     * @throws IllegalArgumentException if userId is null or empty
     * @throws com.eaglebank.exception.UserNotFoundException if user is not found
     */
    UserResponse getUserById(String userId);

    /**
     * Updates an existing user's information.
     *
     * @param userId the unique identifier of the user to update
     * @param request the update request containing new user details
     * @return the updated user response
     * @throws IllegalArgumentException if userId is null or empty or request is invalid
     * @throws com.eaglebank.exception.UserNotFoundException if user is not found
     */
    UserResponse updateUser(String userId, UpdateUserRequest request);

    /**
     * Deletes a user from the system.
     *
     * @param userId the unique identifier of the user to delete
     * @throws IllegalArgumentException if userId is null or empty
     * @throws com.eaglebank.exception.UserNotFoundException if user is not found
     * @throws IllegalStateException if user has associated bank accounts
     */
    void deleteUser(String userId);

    /**
     * Retrieves all users in the system.
     *
     * @return list of all user responses
     * @throws IllegalStateException if retrieval fails
     */
    List<UserResponse> getAllUsers();

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user
     * @return the user response
     * @throws IllegalArgumentException if email is null or empty
     * @throws com.eaglebank.exception.UserNotFoundException if user is not found
     */
    UserResponse getUserByEmail(String email);
}
