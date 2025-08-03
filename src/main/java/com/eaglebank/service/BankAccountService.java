package com.eaglebank.service;

import com.eaglebank.config.BankAccountProperties;
import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.User;
import com.eaglebank.dto.BankAccountResponse;
import com.eaglebank.dto.CreateBankAccountRequest;
import com.eaglebank.dto.ListBankAccountsResponse;
import com.eaglebank.dto.UpdateBankAccountRequest;
import com.eaglebank.exception.BankAccountNotFoundException;
import com.eaglebank.exception.UserNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
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
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
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
}
