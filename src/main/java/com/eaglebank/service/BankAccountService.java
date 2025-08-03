package com.eaglebank.service;

import com.eaglebank.service.interfaces.BankAccountServiceInterface;
import com.eaglebank.config.BankAccountProperties;
import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.Transaction;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.domain.User;
import com.eaglebank.dto.BankAccountResponse;
import com.eaglebank.dto.CreateBankAccountRequest;
import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.ListBankAccountsResponse;
import com.eaglebank.dto.UpdateBankAccountRequest;
import com.eaglebank.exception.BankAccountNotFoundException;
import com.eaglebank.exception.UserNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.TransactionRepository;
import com.eaglebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BankAccountService implements BankAccountServiceInterface {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountProperties bankAccountProperties;
    private final Random random = new Random();

    public BankAccountResponse createBankAccount(String userId, CreateBankAccountRequest request) {
        log.info("Creating bank account for user ID: {}", userId);
        
        // Validate user ID
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be null or empty");
        }
        
        // Validate request
        if (request == null) {
            throw new IllegalArgumentException("Create bank account request must not be null");
        }
        
        try {
            // Find the user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            
            // Generate unique account number
            String accountNumber = generateUniqueAccountNumber();
            
            // Create bank account
            BankAccount bankAccount = BankAccount.builder()
                    .accountNumber(accountNumber)
                    .sortCode(bankAccountProperties.getDefaultSortCode())
                    .name(request.getName())
                    .accountType(AccountType.valueOf(request.getAccountType().toUpperCase()))
                    .balance(BigDecimal.ZERO)
                    .currency(Currency.GBP.name())
                    .user(user)
                    .createdTimestamp(LocalDateTime.now())
                    .updatedTimestamp(LocalDateTime.now())
                    .build();

            BankAccount savedBankAccount = bankAccountRepository.save(bankAccount);
            return mapToBankAccountResponse(savedBankAccount);
            
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof UserNotFoundException) {
                throw e; // Re-throw validation and not found exceptions
            }
            throw new IllegalStateException("Failed to create bank account for user ID: " + userId, e);
        }
    }

    public BankAccountResponse getBankAccountByAccountNumber(String accountNumber) {
        log.info("Getting bank account by account number: {}", accountNumber);
        
        // Validate account number
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be null or empty");
        }
        
        try {
            BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with account number: " + accountNumber));
            return mapToBankAccountResponse(bankAccount);
        } catch (Exception e) {
            if (e instanceof BankAccountNotFoundException) {
                throw e; // Re-throw validation and not found exceptions
            }
            throw new IllegalStateException("Failed to retrieve bank account with account number: " + accountNumber, e);
        }
    }

    public ListBankAccountsResponse getBankAccountsByUserId(String userId) {
        log.info("Getting bank accounts for user ID: {}", userId);
        
        // Validate user ID
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be null or empty");
        }
        
        try {
            // Verify user exists
            if (!userRepository.existsById(userId)) {
                throw new UserNotFoundException("User not found with ID: " + userId);
            }
            
            List<BankAccount> bankAccounts = bankAccountRepository.findByUserId(userId);
            List<BankAccountResponse> bankAccountResponses = bankAccounts.stream()
                    .map(this::mapToBankAccountResponse)
                    .collect(Collectors.toList());
            
            return ListBankAccountsResponse.builder()
                    .accounts(bankAccountResponses)
                    .build();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof UserNotFoundException) {
                throw e; // Re-throw validation and not found exceptions
            }
            throw new IllegalStateException("Failed to retrieve bank accounts for user ID: " + userId, e);
        }
    }

    public List<BankAccountResponse> getAllBankAccounts() {
        log.info("Getting all bank accounts");
        
        try {
            return bankAccountRepository.findAll().stream()
                    .map(this::mapToBankAccountResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve all bank accounts", e);
        }
    }

    public BankAccountResponse updateBankAccount(String accountNumber, UpdateBankAccountRequest request) {
        log.info("Updating bank account with account number: {}", accountNumber);
        
        // Validate account number
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be null or empty");
        }
        
        // Validate request
        if (request == null) {
            throw new IllegalArgumentException("Update bank account request must not be null");
        }
        
        // Validate that at least one field is provided for update
        request.validate();
        
        try {
            BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with account number: " + accountNumber));

            // Update fields if provided and not empty
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                bankAccount.setName(request.getName());
            }
            if (request.getAccountType() != null && !request.getAccountType().trim().isEmpty()) {
                bankAccount.setAccountType(AccountType.valueOf(request.getAccountType().toUpperCase()));
            }

            bankAccount.setUpdatedTimestamp(LocalDateTime.now());
            BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
            return mapToBankAccountResponse(updatedBankAccount);
            
        } catch (Exception e) {
            if (e instanceof BankAccountNotFoundException) {
                throw e; // Re-throw not found exceptions
            }
            throw new IllegalStateException("Failed to update bank account with account number: " + accountNumber, e);
        }
    }

    public void deleteBankAccount(String accountNumber) {
        log.info("Deleting bank account with account number: {}", accountNumber);
        
        // Validate account number
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be null or empty");
        }
        
        try {
            BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with account number: " + accountNumber));

            // Check if account has balance
            if (bankAccount.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalStateException("Cannot delete bank account with positive balance");
            }

            bankAccountRepository.delete(bankAccount);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof BankAccountNotFoundException || e instanceof IllegalStateException) {
                throw e; // Re-throw validation, not found, and state exceptions
            }
            throw new IllegalStateException("Failed to delete bank account with account number: " + accountNumber, e);
        }
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            // Generate 6-digit random number and prefix with "01"
            int randomNumber = 100000 + random.nextInt(900000);
            accountNumber = "01" + randomNumber;
        } while (bankAccountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }

    private BankAccountResponse mapToBankAccountResponse(BankAccount bankAccount) {
        return BankAccountResponse.builder()
                .accountNumber(bankAccount.getAccountNumber())
                .sortCode(bankAccount.getSortCode())
                .name(bankAccount.getName())
                .accountType(bankAccount.getAccountType().name().toLowerCase())
                .balance(bankAccount.getBalance())
                .currency(Currency.valueOf(bankAccount.getCurrency()))
                .createdTimestamp(bankAccount.getCreatedTimestamp())
                .updatedTimestamp(bankAccount.getUpdatedTimestamp())
                .build();
    }

    public BankAccountResponse createTransaction(String accountNumber, CreateTransactionRequest request) {
        log.info("Creating transaction for account: {}", accountNumber);

        // Validate input parameters
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be null or empty");
        }
        if (request == null) {
            throw new IllegalArgumentException("Create transaction request must not be null");
        }

        try {
            // Validate transaction request
            request.validate();

            // Find the bank account
            BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new BankAccountNotFoundException(
                            "Bank account not found with account number: " + accountNumber));

            // Calculate new balance based on transaction type
            BigDecimal currentBalance = bankAccount.getBalance();
            BigDecimal newBalance;
            
            if (request.getType() == TransactionType.DEPOSIT) {
                newBalance = currentBalance.add(request.getAmount());
                log.info("Processing DEPOSIT: {} + {} = {}", currentBalance, request.getAmount(), newBalance);
            } else if (request.getType() == TransactionType.WITHDRAWAL) {
                // Check for sufficient balance before withdrawal
                if (currentBalance.compareTo(request.getAmount()) < 0) {
                    throw new IllegalArgumentException("Insufficient balance for withdrawal. Current balance: " + currentBalance + ", Requested amount: " + request.getAmount());
                }
                newBalance = currentBalance.subtract(request.getAmount());
                log.info("Processing WITHDRAWAL: {} - {} = {}", currentBalance, request.getAmount(), newBalance);
            } else {
                throw new IllegalArgumentException("Unsupported transaction type: " + request.getType());
            }

            // Create and save the transaction
            Transaction transaction = Transaction.builder()
                    .amount(request.getAmount())
                    .currency(request.getCurrency().name())
                    .type(request.getType())
                    .reference(request.getReference())
                    .bankAccount(bankAccount)
                    .userId(bankAccount.getUser().getId())
                    .createdTimestamp(LocalDateTime.now())
                    .build();

            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transaction saved with ID: {}", savedTransaction.getId());

            // Update bank account balance
            bankAccount.setBalance(newBalance);
            bankAccount.setUpdatedTimestamp(LocalDateTime.now());
            BankAccount updatedAccount = bankAccountRepository.save(bankAccount);
            
            log.info("Account balance updated from {} to {}", currentBalance, newBalance);

            // Return updated bank account response
            return mapToBankAccountResponse(updatedAccount);

        } catch (BankAccountNotFoundException e) {
            log.error("Bank account not found: {}", accountNumber);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction data for account {}: {}", accountNumber, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to create transaction for account {}: {}", accountNumber, e.getMessage());
            throw new IllegalStateException("Failed to create transaction", e);
        }
    }
}
