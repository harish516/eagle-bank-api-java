package com.eaglebank.controller;

import com.eaglebank.dto.BadRequestErrorResponse;
import com.eaglebank.dto.BankAccountResponse;
import com.eaglebank.dto.CreateBankAccountRequest;
import com.eaglebank.dto.ErrorResponse;
import com.eaglebank.dto.ListBankAccountsResponse;
import com.eaglebank.dto.UpdateBankAccountRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.exception.CustomAccessDeniedException;
import com.eaglebank.service.BankAccountService;
import com.eaglebank.service.UserService;
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

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bank Account Management", description = "APIs for managing bank accounts in the Eagle Bank system")
public class BankAccountController extends BaseController {

    private final BankAccountService bankAccountService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new bank account", description = "Creates a new bank account for the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bank account created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Authenticated user not found",
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
    public ResponseEntity<BankAccountResponse> createBankAccount(
            @Valid @RequestBody CreateBankAccountRequest request,
            Authentication authentication) {
        log.info("Creating bank account with name: {}", request.getName());
        
        // Extract email from JWT token and get the user
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // Get the user by email to get their ID
        UserResponse user = userService.getUserByEmail(authenticatedEmail);
        
        BankAccountResponse response = bankAccountService.createBankAccount(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get bank account by account number", description = "Retrieves bank account details by account number. Users can only access their own accounts.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bank account found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccountResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only access their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Forbidden - User can only access their own accounts\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Bank account not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Bank account not found\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<BankAccountResponse> getBankAccountByAccountNumber(
            @Parameter(description = "Account number", required = true) @PathVariable String accountNumber,
            Authentication authentication) {
        log.info("Getting bank account by account number: {}", accountNumber);
        
        // Extract email from JWT token
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // Get the authenticated user
        UserResponse user = userService.getUserByEmail(authenticatedEmail);
        
        // Get the requested bank account
        BankAccountResponse requestedAccount = bankAccountService.getBankAccountByAccountNumber(accountNumber);
        
        // Get all accounts for the authenticated user to check ownership
        ListBankAccountsResponse userAccountsResponse = bankAccountService.getBankAccountsByUserId(user.getId());
        
        // Check if the authenticated user owns this account
        boolean isOwner = userAccountsResponse.getAccounts().stream()
                .anyMatch(account -> account.getAccountNumber().equals(requestedAccount.getAccountNumber()));
        
        if (!isOwner) {
            log.warn("User with email {} attempted to access account number: {} which they don't own", 
                authenticatedEmail, accountNumber);
            throw new IllegalStateException("Forbidden - User can only access their own accounts");
        }
        
        return ResponseEntity.ok(requestedAccount);
    }

    @GetMapping
    @Operation(summary = "Get user bank accounts", description = "Retrieves all bank accounts of authenticated user. Users can only access their own accounts.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bank accounts found",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only access their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Forbidden - User can only access their own accounts\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
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
    public ResponseEntity<ListBankAccountsResponse> getBankAccounts(Authentication authentication) {
        log.info("Getting user bank accounts");
        
        // Extract email from JWT token
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // Get the authenticated user
        UserResponse authenticatedUser = userService.getUserByEmail(authenticatedEmail);
        
        // Check if the authenticated user is requesting their own accounts
        if (authenticatedUser == null || authenticatedUser.getId() == null) {
            throw new IllegalStateException("Forbidden - User ID cannot be found for authenticated user");
        }
        
        ListBankAccountsResponse response = bankAccountService.getBankAccountsByUserId(authenticatedUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountNumber}")
    @Operation(summary = "Update bank account", description = "Updates bank account details. Users can only update their own accounts.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bank account updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only update their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Forbidden - User can only update their own accounts\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Bank account not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Bank account not found\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<BankAccountResponse> updateBankAccount(
            @Parameter(description = "Account number", required = true) @PathVariable String accountNumber,
            @Valid @RequestBody UpdateBankAccountRequest request,
            Authentication authentication) {
        log.info("Updating bank account with account number: {}", accountNumber);
        
        // Extract email from JWT token
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }

        // Get the requested bank account
        BankAccountResponse requestedAccount = bankAccountService.getBankAccountByAccountNumber(accountNumber);
        
        // Get the authenticated user
        UserResponse user = userService.getUserByEmail(authenticatedEmail);
        
        // Get all accounts for the authenticated user to check ownership
        ListBankAccountsResponse userAccountsResponse = bankAccountService.getBankAccountsByUserId(user.getId());
        
        // Check if the authenticated user owns this account
        boolean isOwner = userAccountsResponse.getAccounts().stream()
                .anyMatch(account -> account.getAccountNumber().equals(requestedAccount.getAccountNumber()));
        
        if (!isOwner) {
            log.warn("User with email {} attempted to update account number: {} which they don't own", 
                authenticatedEmail, accountNumber);
            throw new IllegalStateException("Forbidden - User can only update their own accounts");
        }
        
        BankAccountResponse response = bankAccountService.updateBankAccount(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Delete bank account", description = "Deletes a bank account. Users can only delete their own accounts.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Bank account deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only delete their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Forbidden - User can only delete their own accounts\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Bank account not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Bank account not found\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete account with associated transactions",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Cannot delete account with associated transactions\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Void> deleteBankAccount(
            @Parameter(description = "Account number", required = true) @PathVariable String accountNumber,
            Authentication authentication) {
        log.info("Deleting bank account with account number: {}", accountNumber);
        
        // Extract email from JWT token
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }

        // Get the requested bank account
        BankAccountResponse requestedAccount = bankAccountService.getBankAccountByAccountNumber(accountNumber);
        
        // Get the authenticated user
        UserResponse user = userService.getUserByEmail(authenticatedEmail);
        
        // Get all accounts for the authenticated user to check ownership
        ListBankAccountsResponse userAccountsResponse = bankAccountService.getBankAccountsByUserId(user.getId());
        
        // Check if the authenticated user owns this account
        boolean isOwner = userAccountsResponse.getAccounts().stream()
                .anyMatch(account -> account.getAccountNumber().equals(requestedAccount.getAccountNumber()));
        
        if (!isOwner) {
            log.warn("User with email {} attempted to delete account number: {} which they don't own", 
                authenticatedEmail, accountNumber);
            throw new IllegalStateException("Forbidden - User can only delete their own accounts");
        }
        
        bankAccountService.deleteBankAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }
}
