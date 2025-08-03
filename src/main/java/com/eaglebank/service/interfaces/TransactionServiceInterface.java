package com.eaglebank.service.interfaces;

import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.ListTransactionsResponse;
import com.eaglebank.dto.TransactionResponse;

/**
 * Interface for Transaction service operations.
 * Defines the contract for transaction management functionality.
 */
public interface TransactionServiceInterface {

    /**
     * Creates a new transaction for a bank account.
     *
     * @param accountNumber the account number for the transaction
     * @param request the transaction creation request
     * @param currentUserId the current authenticated user ID
     * @return the created transaction response
     * @throws IllegalArgumentException if parameters are invalid or insufficient funds
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws com.eaglebank.exception.CustomAccessDeniedException if user doesn't own the account
     * @throws IllegalStateException if transaction creation fails
     */
    TransactionResponse createTransaction(String accountNumber, CreateTransactionRequest request, String currentUserId);

    /**
     * Retrieves all transactions for a specific bank account.
     *
     * @param accountNumber the account number to get transactions for
     * @param currentUserId the current authenticated user ID
     * @return list of transactions response
     * @throws IllegalArgumentException if accountNumber or currentUserId is invalid
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws com.eaglebank.exception.CustomAccessDeniedException if user doesn't own the account
     * @throws IllegalStateException if retrieval fails
     */
    ListTransactionsResponse getTransactionsByAccountNumber(String accountNumber, String currentUserId);

    /**
     * Retrieves a specific transaction by account number and transaction ID.
     *
     * @param accountNumber the account number
     * @param transactionId the transaction ID
     * @param currentUserId the current authenticated user ID
     * @return the transaction response
     * @throws IllegalArgumentException if parameters are invalid
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws com.eaglebank.exception.TransactionNotFoundException if transaction is not found
     * @throws com.eaglebank.exception.CustomAccessDeniedException if user doesn't own the account
     * @throws IllegalStateException if retrieval fails
     */
    TransactionResponse getTransactionById(String accountNumber, String transactionId, String currentUserId);

    /**
     * Updates an existing transaction.
     *
     * @param accountNumber the account number
     * @param transactionId the transaction ID to update
     * @param request the transaction update request
     * @param currentUserId the current authenticated user ID
     * @return the updated transaction response
     * @throws IllegalArgumentException if parameters are invalid
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws com.eaglebank.exception.TransactionNotFoundException if transaction is not found
     * @throws com.eaglebank.exception.CustomAccessDeniedException if user doesn't own the account
     * @throws IllegalStateException if update fails
     */
    TransactionResponse updateTransaction(String accountNumber, String transactionId, CreateTransactionRequest request, String currentUserId);

    /**
     * Deletes a transaction.
     *
     * @param accountNumber the account number
     * @param transactionId the transaction ID to delete
     * @param currentUserId the current authenticated user ID
     * @throws IllegalArgumentException if parameters are invalid
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws com.eaglebank.exception.TransactionNotFoundException if transaction is not found
     * @throws com.eaglebank.exception.CustomAccessDeniedException if user doesn't own the account
     * @throws IllegalStateException if deletion fails
     */
    void deleteTransaction(String accountNumber, String transactionId, String currentUserId);
}
