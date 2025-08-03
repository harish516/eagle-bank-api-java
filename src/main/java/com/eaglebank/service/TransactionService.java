package com.eaglebank.service;

import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Transaction;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.ListTransactionsResponse;
import com.eaglebank.dto.TransactionResponse;
import com.eaglebank.exception.BankAccountNotFoundException;
import com.eaglebank.exception.CustomAccessDeniedException;
import com.eaglebank.exception.TransactionNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final Random random = new Random();

    /**
     * Creates a new transaction for the specified bank account
     *
     * @param accountNumber the account number
     * @param request the transaction creation request
     * @param currentUserId the current authenticated user ID
     * @return the created transaction response
     * @throws BankAccountNotFoundException if the bank account is not found
     * @throws CustomAccessDeniedException if the user doesn't own the account
     * @throws IllegalArgumentException if the transaction would result in insufficient funds
     */
    public TransactionResponse createTransaction(String accountNumber, CreateTransactionRequest request, String currentUserId) {
        log.info("Creating transaction for account {} by user {}", accountNumber, currentUserId);
        
        // Find the bank account
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with account number: " + accountNumber));
        
        // Check ownership
        if (!bankAccount.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to create transaction for account {} owned by {}", 
                    currentUserId, accountNumber, bankAccount.getUser().getId());
            throw new CustomAccessDeniedException("You don't have permission to create transactions for this account");
        }
        
        // Validate withdrawal doesn't exceed balance
        if (request.getType() == TransactionType.WITHDRAWAL && 
            bankAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Available balance: " + bankAccount.getBalance());
        }
        
        // Create transaction
        Transaction transaction = Transaction.builder()
                .id(generateTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency().name())
                .type(request.getType())
                .reference(request.getReference())
                .bankAccount(bankAccount)
                .userId(currentUserId)
                .build();
        
        // Process the transaction (updates account balance)
        transaction.process();
        
        // Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Save updated bank account balance
        bankAccountRepository.save(bankAccount);
        
        log.info("Transaction {} created successfully for account {}", savedTransaction.getId(), accountNumber);
        return mapToTransactionResponse(savedTransaction);
    }

    /**
     * Retrieves all transactions for a specific bank account
     *
     * @param accountNumber the account number
     * @param currentUserId the current authenticated user ID
     * @return list of transactions wrapped in response object
     * @throws BankAccountNotFoundException if the bank account is not found
     * @throws CustomAccessDeniedException if the user doesn't own the account
     */
    @Transactional(readOnly = true)
    public ListTransactionsResponse getTransactionsByAccountNumber(String accountNumber, String currentUserId) {
        log.info("Retrieving transactions for account {} by user {}", accountNumber, currentUserId);
        
        // Find the bank account to verify ownership
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with account number: " + accountNumber));
        
        // Check ownership
        if (!bankAccount.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to access transactions for account {} owned by {}", 
                    currentUserId, accountNumber, bankAccount.getUser().getId());
            throw new CustomAccessDeniedException("You don't have permission to view transactions for this account");
        }
        
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        
        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} transactions for account {}", transactions.size(), accountNumber);
        return ListTransactionsResponse.builder()
                .transactions(transactionResponses)
                .build();
    }

    /**
     * Retrieves a specific transaction by account number and transaction ID
     *
     * @param accountNumber the account number
     * @param transactionId the transaction ID
     * @param currentUserId the current authenticated user ID
     * @return the transaction response
     * @throws BankAccountNotFoundException if the bank account is not found
     * @throws TransactionNotFoundException if the transaction is not found
     * @throws CustomAccessDeniedException if the user doesn't own the account
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String accountNumber, String transactionId, String currentUserId) {
        log.info("Retrieving transaction {} for account {} by user {}", transactionId, accountNumber, currentUserId);
        
        // Find the bank account to verify ownership
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with account number: " + accountNumber));
        
        // Check ownership
        if (!bankAccount.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to access transaction {} for account {} owned by {}", 
                    currentUserId, transactionId, accountNumber, bankAccount.getUser().getId());
            throw new CustomAccessDeniedException("You don't have permission to view transactions for this account");
        }
        
        Transaction transaction = transactionRepository.findByAccountNumberAndTransactionId(accountNumber, transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId + " for account: " + accountNumber));
        
        log.info("Retrieved transaction {} for account {}", transactionId, accountNumber);
        return mapToTransactionResponse(transaction);
    }

    /**
     * Deletes a transaction by account number and transaction ID
     * Note: This is typically not allowed in real banking systems, but included for completeness
     *
     * @param accountNumber the account number
     * @param transactionId the transaction ID
     * @param currentUserId the current authenticated user ID
     * @throws BankAccountNotFoundException if the bank account is not found
     * @throws TransactionNotFoundException if the transaction is not found
     * @throws CustomAccessDeniedException if the user doesn't own the account
     */
    public void deleteTransaction(String accountNumber, String transactionId, String currentUserId) {
        log.info("Deleting transaction {} for account {} by user {}", transactionId, accountNumber, currentUserId);
        
        // Find the bank account to verify ownership
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with account number: " + accountNumber));
        
        // Check ownership
        if (!bankAccount.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to delete transaction {} for account {} owned by {}", 
                    currentUserId, transactionId, accountNumber, bankAccount.getUser().getId());
            throw new CustomAccessDeniedException("You don't have permission to delete transactions for this account");
        }
        
        Transaction transaction = transactionRepository.findByAccountNumberAndTransactionId(accountNumber, transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId + " for account: " + accountNumber));
        
        // Reverse the transaction (update account balance)
        if (transaction.getType() == TransactionType.DEPOSIT) {
            bankAccount.withdraw(transaction.getAmount());
        } else {
            bankAccount.deposit(transaction.getAmount());
        }
        
        // Delete transaction
        transactionRepository.delete(transaction);
        
        // Save updated bank account balance
        bankAccountRepository.save(bankAccount);
        
        log.info("Transaction {} deleted successfully for account {}", transactionId, accountNumber);
    }

    /**
     * Generates a unique transaction ID with the format "tan-{random}"
     *
     * @return the generated transaction ID
     */
    private String generateTransactionId() {
        String transactionId;
        do {
            transactionId = "tan-" + Math.abs(random.nextLong());
        } while (transactionRepository.existsById(transactionId));
        return transactionId;
    }

    /**
     * Maps a Transaction entity to a TransactionResponse DTO
     *
     * @param transaction the transaction entity
     * @return the transaction response DTO
     */
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .currency(com.eaglebank.domain.Currency.valueOf(transaction.getCurrency()))
                .type(transaction.getType())
                .reference(transaction.getReference())
                .userId(transaction.getUserId())
                .createdTimestamp(transaction.getCreatedTimestamp())
                .build();
    }
}
