package com.eaglebank.service.interfaces;

import com.eaglebank.dto.BankAccountResponse;
import com.eaglebank.dto.CreateBankAccountRequest;
import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.ListBankAccountsResponse;
import com.eaglebank.dto.UpdateBankAccountRequest;

import java.util.List;

/**
 * Interface for Bank Account service operations.
 * Defines the contract for bank account management and transaction functionality.
 */
public interface BankAccountServiceInterface {

    /**
     * Creates a new bank account for a user.
     *
     * @param userId the unique identifier of the user
     * @param request the bank account creation request
     * @return the created bank account response
     * @throws IllegalArgumentException if userId or request is invalid
     * @throws com.eaglebank.exception.UserNotFoundException if user is not found
     * @throws IllegalStateException if account creation fails
     */
    BankAccountResponse createBankAccount(String userId, CreateBankAccountRequest request);

    /**
     * Retrieves a bank account by its account number.
     *
     * @param accountNumber the account number to search for
     * @return the bank account response
     * @throws IllegalArgumentException if accountNumber is null or empty
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws IllegalStateException if retrieval fails
     */
    BankAccountResponse getBankAccountByAccountNumber(String accountNumber);

    /**
     * Retrieves all bank accounts for a specific user.
     *
     * @param userId the unique identifier of the user
     * @return list of bank accounts response
     * @throws IllegalArgumentException if userId is null or empty
     * @throws com.eaglebank.exception.UserNotFoundException if user is not found
     * @throws IllegalStateException if retrieval fails
     */
    ListBankAccountsResponse getBankAccountsByUserId(String userId);

    /**
     * Retrieves all bank accounts in the system.
     *
     * @return list of all bank account responses
     * @throws IllegalStateException if retrieval fails
     */
    List<BankAccountResponse> getAllBankAccounts();

    /**
     * Updates an existing bank account.
     *
     * @param accountNumber the account number to update
     * @param request the update request containing new account details
     * @return the updated bank account response
     * @throws IllegalArgumentException if accountNumber is null or empty or request is invalid
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws IllegalStateException if update fails
     */
    BankAccountResponse updateBankAccount(String accountNumber, UpdateBankAccountRequest request);

    /**
     * Deletes a bank account.
     *
     * @param accountNumber the account number to delete
     * @throws IllegalArgumentException if accountNumber is null or empty
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws IllegalStateException if account has positive balance or deletion fails
     */
    void deleteBankAccount(String accountNumber);

    /**
     * Creates a transaction for a bank account and updates the account balance.
     *
     * @param accountNumber the account number for the transaction
     * @param request the transaction creation request
     * @return the updated bank account response
     * @throws IllegalArgumentException if accountNumber or request is invalid or insufficient balance
     * @throws com.eaglebank.exception.BankAccountNotFoundException if account is not found
     * @throws IllegalStateException if transaction creation fails
     */
    BankAccountResponse createTransaction(String accountNumber, CreateTransactionRequest request);
}
