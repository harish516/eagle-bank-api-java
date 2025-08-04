package com.eaglebank.controller;

import com.eaglebank.dto.BadRequestErrorResponse;
import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.ErrorResponse;
import com.eaglebank.dto.ListTransactionsResponse;
import com.eaglebank.dto.TransactionResponse;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.exception.CustomAccessDeniedException;
import com.eaglebank.service.interfaces.TransactionServiceInterface;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts/{accountNumber}/transactions")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Transaction Management", description = "APIs for managing transactions within bank accounts")
public class TransactionController extends BaseController {

    private final TransactionServiceInterface transactionService;
    private final UserServiceInterface userService;

    @PostMapping
    @Operation(summary = "Create a new transaction", description = "Creates a new transaction (deposit/withdrawal) for the specified bank account")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient funds",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorResponse.class),
                    examples = {
                        @ExampleObject(name = "Invalid Data", value = "{\"message\": \"Amount must be positive\", \"timestamp\": \"2024-01-15T10:30:00\"}"),
                        @ExampleObject(name = "Insufficient Funds", value = "{\"message\": \"Insufficient funds. Available balance: 500.00, Requested amount: 1000.00\", \"timestamp\": \"2024-01-15T10:30:00\"}")
                    })),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only create transactions for their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"You don't have permission to create transactions for this account\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Bank account not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Bank account not found with account number: 01234567\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<TransactionResponse> createTransaction(
            @Parameter(description = "Bank account number", required = true, example = "01234567") 
            @PathVariable String accountNumber,
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication authentication) {
        log.info("Creating transaction for account: {} with type: {} and amount: {}", 
                accountNumber, request.getType(), request.getAmount());
        
        try {
            // Extract email from JWT token and get the user
            String authenticatedEmail = getAuthenticatedEmail(authentication);
            if (authenticatedEmail == null) {
                log.warn("Transaction creation denied - no authenticated email found for account: {}", accountNumber);
                throw new CustomAccessDeniedException("No authenticated email found - access denied");
            }
            
            // Get the user by email to get their ID
            UserResponse user = userService.getUserByEmail(authenticatedEmail);
            log.debug("User {} attempting to create transaction for account: {}", user.getId(), accountNumber);
            
            TransactionResponse response = transactionService.createTransaction(accountNumber, request, user.getId());
            log.info("Transaction created successfully - id: {}, account: {}, user: {}, amount: {}", 
                    response.getId(), accountNumber, user.getId(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Transaction creation failed - account: {}, type: {}, amount: {}, error: {}", 
                    accountNumber, request.getType(), request.getAmount(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    @Operation(summary = "Get all transactions for an account", description = "Retrieves all transactions for the specified bank account. Users can only access transactions for their own accounts.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ListTransactionsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only access transactions for their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"You don't have permission to access transactions for this account\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Bank account not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Bank account not found with account number: 01234567\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<ListTransactionsResponse> getTransactionsByAccount(
            @Parameter(description = "Bank account number", required = true, example = "01234567") 
            @PathVariable String accountNumber,
            Authentication authentication) {
        log.info("Getting transactions for account: {}", accountNumber);
        
        // Extract email from JWT token and get the user
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // Get the user by email to get their ID
        UserResponse user = userService.getUserByEmail(authenticatedEmail);
        
        ListTransactionsResponse response = transactionService.getTransactionsByAccountNumber(accountNumber, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get a specific transaction", description = "Retrieves a specific transaction by ID for the specified bank account. Users can only access transactions for their own accounts.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only access transactions for their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"You don't have permission to access this transaction\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Bank account or transaction not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                        @ExampleObject(name = "Account Not Found", value = "{\"message\": \"Bank account not found with account number: 01234567\", \"timestamp\": \"2024-01-15T10:30:00\"}"),
                        @ExampleObject(name = "Transaction Not Found", value = "{\"message\": \"Transaction not found with ID: tan-abc123\", \"timestamp\": \"2024-01-15T10:30:00\"}")
                    })),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "Bank account number", required = true, example = "01234567") 
            @PathVariable String accountNumber,
            @Parameter(description = "Transaction ID", required = true, example = "tan-abc123") 
            @PathVariable String transactionId,
            Authentication authentication) {
        log.info("Getting transaction {} for account: {}", transactionId, accountNumber);
        
        // Extract email from JWT token and get the user
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // Get the user by email to get their ID
        UserResponse user = userService.getUserByEmail(authenticatedEmail);
        
        TransactionResponse response = transactionService.getTransactionById(accountNumber, transactionId, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{transactionId}")
    @Operation(summary = "Update a transaction", description = "Updates an existing transaction for the specified bank account. Users can only update transactions for their own accounts.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only update transactions for their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"You don't have permission to update this transaction\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Bank account or transaction not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                        @ExampleObject(name = "Account Not Found", value = "{\"message\": \"Bank account not found with account number: 01234567\", \"timestamp\": \"2024-01-15T10:30:00\"}"),
                        @ExampleObject(name = "Transaction Not Found", value = "{\"message\": \"Transaction not found with ID: tan-abc123\", \"timestamp\": \"2024-01-15T10:30:00\"}")
                    })),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(description = "Bank account number", required = true, example = "01234567") 
            @PathVariable String accountNumber,
            @Parameter(description = "Transaction ID", required = true, example = "tan-abc123") 
            @PathVariable String transactionId,
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication authentication) {
        log.info("Updating transaction {} for account: {}", transactionId, accountNumber);
        
        // Extract email from JWT token and get the user
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // Get the user by email to get their ID
        UserResponse user = userService.getUserByEmail(authenticatedEmail);
        
        TransactionResponse response = transactionService.updateTransaction(accountNumber, transactionId, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{transactionId}")
    @Operation(summary = "Delete a transaction", description = "Deletes an existing transaction for the specified bank account. Users can only delete transactions for their own accounts.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "oauth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"Unauthorized\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only delete transactions for their own accounts",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"You don't have permission to delete this transaction\", \"timestamp\": \"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Bank account or transaction not found",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                        @ExampleObject(name = "Account Not Found", value = "{\"message\": \"Bank account not found with account number: 01234567\", \"timestamp\": \"2024-01-15T10:30:00\"}"),
                        @ExampleObject(name = "Transaction Not Found", value = "{\"message\": \"Transaction not found with ID: tan-abc123\", \"timestamp\": \"2024-01-15T10:30:00\"}")
                    })),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        value = "{\"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\"}")))
    })
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Bank account number", required = true, example = "01234567") 
            @PathVariable String accountNumber,
            @Parameter(description = "Transaction ID", required = true, example = "tan-abc123") 
            @PathVariable String transactionId,
            Authentication authentication) {
        log.info("Deleting transaction {} for account: {}", transactionId, accountNumber);
        
        // Extract email from JWT token and get the user
        String authenticatedEmail = getAuthenticatedEmail(authentication);
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("No authenticated email found - access denied");
        }
        
        // Get the user by email to get their ID
        UserResponse user = userService.getUserByEmail(authenticatedEmail);
        
        transactionService.deleteTransaction(accountNumber, transactionId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
