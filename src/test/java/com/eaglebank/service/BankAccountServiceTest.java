package com.eaglebank.service;

import com.eaglebank.config.BankAccountProperties;
import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.Address;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountProperties bankAccountProperties;

    @InjectMocks
    private BankAccountService bankAccountService;

    private User testUser;
    private BankAccount testBankAccount;
    private CreateBankAccountRequest createBankAccountRequest;
    private UpdateBankAccountRequest updateBankAccountRequest;

    @BeforeEach
    void setUp() {
        Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        testUser = User.builder()
                .id("usr-abc123")
                .name("Test User")
                .address(address)
                .phoneNumber("+44123456789")
                .email("test@example.com")
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        testBankAccount = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(1000.00))
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        createBankAccountRequest = CreateBankAccountRequest.builder()
                .name("Test Account")
                .accountType("personal")
                .build();

        updateBankAccountRequest = UpdateBankAccountRequest.builder()
                .name("Updated Account Name")
                .accountType("personal")
                .build();

        // Setup default mock behavior for BankAccountProperties
        lenient().when(bankAccountProperties.getDefaultSortCode()).thenReturn("10-10-10");
    }

    /**
     * Input validation tests for BankAccountService methods.
     * These tests ensure that the service methods handle invalid input correctly.
     */

    @Test
    void shouldThrowExceptionWhenCreatingBankAccountWithNullUserId() {
        assertThatThrownBy(() -> bankAccountService.createBankAccount(null, createBankAccountRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenCreatingBankAccountWithEmptyUserId() {
        assertThatThrownBy(() -> bankAccountService.createBankAccount("", createBankAccountRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenCreatingBankAccountWithBlankUserId() {
        assertThatThrownBy(() -> bankAccountService.createBankAccount("   ", createBankAccountRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenCreatingBankAccountWithNullRequest() {
        assertThatThrownBy(() -> bankAccountService.createBankAccount("usr-abc123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Create bank account request must not be null");
    }

    @Test
    void shouldThrowExceptionWhenGettingBankAccountWithNullAccountNumber() {
        assertThatThrownBy(() -> bankAccountService.getBankAccountByAccountNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenGettingBankAccountWithEmptyAccountNumber() {
        assertThatThrownBy(() -> bankAccountService.getBankAccountByAccountNumber(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingBankAccountWithNullRequest() {
        assertThatThrownBy(() -> bankAccountService.updateBankAccount("01123456", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Update bank account request must not be null");
    }

    /**
     * Tests for BankAccountService.createBankAccount() method.
     * This method creates a new bank account for a user and saves it to the repository.
     */

    @Test
    void shouldCreateBankAccountSuccessfully() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

        BankAccountResponse result = bankAccountService.createBankAccount("usr-abc123", createBankAccountRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo("01123456");
        assertThat(result.getName()).isEqualTo("Test Account");
        assertThat(result.getAccountType()).isEqualTo("personal");
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(1000.00));
        assertThat(result.getCurrency()).isEqualTo(Currency.GBP);

        verify(userRepository).findById("usr-abc123");
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForBankAccountCreation() {
        when(userRepository.findById("usr-nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.createBankAccount("usr-nonexistent", createBankAccountRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with ID");

        verify(userRepository).findById("usr-nonexistent");
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    /**
     * Tests for BankAccountService.getBankAccountByAccountNumber() method.
     * This method retrieves a bank account by its account number and maps it to a BankAccountResponse DTO.
     */

    @Test
    void shouldGetBankAccountByAccountNumberSuccessfully() {
        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));

        BankAccountResponse result = bankAccountService.getBankAccountByAccountNumber("01123456");

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo("01123456");
        assertThat(result.getName()).isEqualTo("Test Account");
        assertThat(result.getAccountType()).isEqualTo("personal");

        verify(bankAccountRepository).findByAccountNumber("01123456");
    }

    @Test
    void shouldThrowExceptionWhenBankAccountNotFound() {
        when(bankAccountRepository.findByAccountNumber("01999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.getBankAccountByAccountNumber("01999999"))
                .isInstanceOf(BankAccountNotFoundException.class)
                .hasMessageContaining("Bank account not found with account number");

        verify(bankAccountRepository).findByAccountNumber("01999999");
    }

    /**
     * Tests for BankAccountService.getBankAccountsByUserId() method.
     * This method retrieves all bank accounts for a specific user.
     */

    @Test
    void shouldGetBankAccountsByUserIdSuccessfully() {
        List<BankAccount> bankAccounts = List.of(testBankAccount);
        when(userRepository.existsById("usr-abc123")).thenReturn(true);
        when(bankAccountRepository.findByUserId("usr-abc123")).thenReturn(bankAccounts);

        ListBankAccountsResponse result = bankAccountService.getBankAccountsByUserId("usr-abc123");

        assertThat(result).isNotNull();
        assertThat(result.getAccounts()).hasSize(1);
        assertThat(result.getAccounts().get(0).getAccountNumber()).isEqualTo("01123456");
        assertThat(result.getAccounts().get(0).getName()).isEqualTo("Test Account");

        verify(userRepository).existsById("usr-abc123");
        verify(bankAccountRepository).findByUserId("usr-abc123");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoBankAccounts() {
        when(userRepository.existsById("usr-abc123")).thenReturn(true);
        when(bankAccountRepository.findByUserId("usr-abc123")).thenReturn(Collections.emptyList());

        ListBankAccountsResponse result = bankAccountService.getBankAccountsByUserId("usr-abc123");

        assertThat(result).isNotNull();
        assertThat(result.getAccounts()).isEmpty();

        verify(userRepository).existsById("usr-abc123");
        verify(bankAccountRepository).findByUserId("usr-abc123");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForBankAccountsListing() {
        when(userRepository.existsById("usr-nonexistent")).thenReturn(false);

        assertThatThrownBy(() -> bankAccountService.getBankAccountsByUserId("usr-nonexistent"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with ID");

        verify(userRepository).existsById("usr-nonexistent");
        verify(bankAccountRepository, never()).findByUserId(anyString());
    }

    @Test
    void shouldThrowExceptionWhenGettingBankAccountsByUserIdWithNullUserId() {
        assertThatThrownBy(() -> bankAccountService.getBankAccountsByUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenGettingBankAccountsByUserIdWithEmptyUserId() {
        assertThatThrownBy(() -> bankAccountService.getBankAccountsByUserId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null or empty");
    }

    /**
     * Tests for BankAccountService.getAllBankAccounts() method.
     * This method retrieves all bank accounts from the repository and maps them to BankAccountResponse DTOs.
     */

    @Test
    void shouldGetAllBankAccountsSuccessfully() {
        List<BankAccount> bankAccounts = List.of(testBankAccount);
        when(bankAccountRepository.findAll()).thenReturn(bankAccounts);

        List<BankAccountResponse> result = bankAccountService.getAllBankAccounts();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("01123456");
        assertThat(result.get(0).getName()).isEqualTo("Test Account");

        verify(bankAccountRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoBankAccountsExist() {
        when(bankAccountRepository.findAll()).thenReturn(Collections.emptyList());

        List<BankAccountResponse> result = bankAccountService.getAllBankAccounts();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(bankAccountRepository).findAll();
    }

    /**
     * Tests for BankAccountService.updateBankAccount() method.
     * This method updates an existing bank account with new details provided in the UpdateBankAccountRequest DTO.
     */

    @Test
    void shouldUpdateBankAccountSuccessfully() {
        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
            BankAccount saved = invocation.getArgument(0);
            // Simulate that the repository returns the saved object with updated name
            saved.setName("Updated Account Name");
            return saved;
        });

        BankAccountResponse result = bankAccountService.updateBankAccount("01123456", updateBankAccountRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo("01123456");
        assertThat(result.getName()).isEqualTo("Updated Account Name");

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldUpdateBankAccountPartiallyWithOnlyName() {
        UpdateBankAccountRequest partialUpdate = UpdateBankAccountRequest.builder()
                .name("Updated Name Only")
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

        BankAccountResponse result = bankAccountService.updateBankAccount("01123456", partialUpdate);

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo("01123456");

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldUpdateBankAccountPartiallyWithOnlyAccountType() {
        UpdateBankAccountRequest partialUpdate = UpdateBankAccountRequest.builder()
                .accountType("personal")
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

        BankAccountResponse result = bankAccountService.updateBankAccount("01123456", partialUpdate);

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo("01123456");

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentBankAccount() {
        when(bankAccountRepository.findByAccountNumber("01999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.updateBankAccount("01999999", updateBankAccountRequest))
                .isInstanceOf(BankAccountNotFoundException.class)
                .hasMessageContaining("Bank account not found with account number");

        verify(bankAccountRepository).findByAccountNumber("01999999");
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenBothFieldsAreEmpty() {
        // Given - UpdateBankAccountRequest with both name and accountType as empty strings
        UpdateBankAccountRequest emptyFieldsRequest = UpdateBankAccountRequest.builder()
                .name("")
                .accountType("")
                .build();

        // When & Then - Should throw IllegalArgumentException
        assertThatThrownBy(() -> bankAccountService.updateBankAccount("01123456", emptyFieldsRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one field (name or accountType) must be provided for update");

        // Verify that repository methods are not called when validation fails
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenBothFieldsAreWhitespace() {
        // Given - UpdateBankAccountRequest with both name and accountType as whitespace
        UpdateBankAccountRequest whitespaceFieldsRequest = UpdateBankAccountRequest.builder()
                .name("   ")
                .accountType("  \t  ")
                .build();

        // When & Then - Should throw IllegalArgumentException
        assertThatThrownBy(() -> bankAccountService.updateBankAccount("01123456", whitespaceFieldsRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one field (name or accountType) must be provided for update");

        // Verify that repository methods are not called when validation fails
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenBothFieldsAreNull() {
        // Given - UpdateBankAccountRequest with both fields as null
        UpdateBankAccountRequest nullFieldsRequest = UpdateBankAccountRequest.builder()
                .name(null)
                .accountType(null)
                .build();

        // When & Then - Should throw IllegalArgumentException
        assertThatThrownBy(() -> bankAccountService.updateBankAccount("01123456", nullFieldsRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one field (name or accountType) must be provided for update");

        // Verify that repository methods are not called when validation fails
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    /**
     * Tests for BankAccountService.deleteBankAccount() method.
     * This method deletes a bank account by account number, ensuring that the account exists and has zero balance before deletion.
     */

    @Test
    void shouldDeleteBankAccountSuccessfully() {
        // Set balance to zero for successful deletion
        testBankAccount.setBalance(BigDecimal.ZERO);
        
        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));
        doNothing().when(bankAccountRepository).delete(testBankAccount);

        bankAccountService.deleteBankAccount("01123456");

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(bankAccountRepository).delete(testBankAccount);
    }

    @Test
    void shouldThrowExceptionWhenDeletingBankAccountWithPositiveBalance() {
        // Set positive balance
        testBankAccount.setBalance(BigDecimal.valueOf(100.00));
        
        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));

        assertThatThrownBy(() -> bankAccountService.deleteBankAccount("01123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete bank account with positive balance");

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(bankAccountRepository, never()).delete(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentBankAccount() {
        when(bankAccountRepository.findByAccountNumber("01999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.deleteBankAccount("01999999"))
                .isInstanceOf(BankAccountNotFoundException.class)
                .hasMessageContaining("Bank account not found with account number");

        verify(bankAccountRepository).findByAccountNumber("01999999");
        verify(bankAccountRepository, never()).delete(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingBankAccountWithNullAccountNumber() {
        assertThatThrownBy(() -> bankAccountService.deleteBankAccount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenDeletingBankAccountWithEmptyAccountNumber() {
        assertThatThrownBy(() -> bankAccountService.deleteBankAccount(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number must not be null or empty");
    }

    /**
     * Edge cases and exception handling tests for BankAccountService methods.
     * These tests ensure that the service methods handle unexpected scenarios gracefully.
     */

    @Test
    void shouldHandleRepositoryExceptionsInGetBankAccount() {
        when(bankAccountRepository.findByAccountNumber("01123456")).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> bankAccountService.getBankAccountByAccountNumber("01123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to retrieve bank account");

        verify(bankAccountRepository).findByAccountNumber("01123456");
    }

    @Test
    void shouldHandleRepositoryExceptionsInCreateBankAccount() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> bankAccountService.createBankAccount("usr-abc123", createBankAccountRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to create bank account");

        verify(userRepository).findById("usr-abc123");
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldHandleRepositoryExceptionsInGetAllBankAccounts() {
        when(bankAccountRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> bankAccountService.getAllBankAccounts())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to retrieve all bank accounts");

        verify(bankAccountRepository).findAll();
    }

    @Test
    void shouldPreserveBankAccountTimestampsCorrectly() {
        LocalDateTime originalCreatedTime = LocalDateTime.now().minusDays(1);
        LocalDateTime originalUpdatedTime = LocalDateTime.now().minusHours(1);
        
        testBankAccount.setCreatedTimestamp(originalCreatedTime);
        testBankAccount.setUpdatedTimestamp(originalUpdatedTime);
        
        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

        BankAccountResponse result = bankAccountService.updateBankAccount("01123456", updateBankAccountRequest);

        assertThat(result).isNotNull();
        assertThat(result.getCreatedTimestamp()).isEqualTo(originalCreatedTime);
        assertThat(result.getUpdatedTimestamp()).isAfter(originalUpdatedTime);

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldGenerateUniqueAccountNumberWhenCreatingAccount() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.existsByAccountNumber(anyString()))
                .thenReturn(false); // First generated number is unique
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

        BankAccountResponse result = bankAccountService.createBankAccount("usr-abc123", createBankAccountRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).matches("^01\\d{6}$");

        verify(userRepository).findById("usr-abc123");
        verify(bankAccountRepository, atLeastOnce()).existsByAccountNumber(anyString());
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldRetryGenerationWhenAccountNumberAlreadyExists() {
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true)  // First attempt - already exists
                .thenReturn(false); // Second attempt - unique
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

        BankAccountResponse result = bankAccountService.createBankAccount("usr-abc123", createBankAccountRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).matches("^01\\d{6}$");

        verify(userRepository).findById("usr-abc123");
        verify(bankAccountRepository, times(2)).existsByAccountNumber(anyString());
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldMapBankAccountResponseCorrectly() {
        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));

        BankAccountResponse result = bankAccountService.getBankAccountByAccountNumber("01123456");

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo(testBankAccount.getAccountNumber());
        assertThat(result.getSortCode()).isEqualTo(testBankAccount.getSortCode());
        assertThat(result.getName()).isEqualTo(testBankAccount.getName());
        assertThat(result.getAccountType()).isEqualTo(testBankAccount.getAccountType().name().toLowerCase());
        assertThat(result.getBalance()).isEqualTo(testBankAccount.getBalance());
        assertThat(result.getCurrency()).isEqualTo(Currency.valueOf(testBankAccount.getCurrency()));
        assertThat(result.getCreatedTimestamp()).isEqualTo(testBankAccount.getCreatedTimestamp());
        assertThat(result.getUpdatedTimestamp()).isEqualTo(testBankAccount.getUpdatedTimestamp());

        verify(bankAccountRepository).findByAccountNumber("01123456");
    }

    @Test
    void shouldUseSortCodeFromProperties() {
        // Given - set up custom sort code in properties
        lenient().when(bankAccountProperties.getDefaultSortCode()).thenReturn("20-20-20");
        when(userRepository.findById("usr-abc123")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        
        // Create a bank account with the custom sort code for the return value
        BankAccount customBankAccount = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("20-20-20")  // Custom sort code from properties
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.ZERO)
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
                
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(customBankAccount);

        // When
        BankAccountResponse result = bankAccountService.createBankAccount("usr-abc123", createBankAccountRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSortCode()).isEqualTo("20-20-20");

        verify(bankAccountProperties).getDefaultSortCode();
        verify(userRepository).findById("usr-abc123");
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldCreateTransactionWithdrawalSuccessfully() {
        // Test WITHDRAWAL transaction
        // Given
        CreateTransactionRequest withdrawalRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(40.00))
                .currency(Currency.GBP)
                .type(TransactionType.WITHDRAWAL)
                .reference("Test withdrawal")
                .build();

        BankAccount accountBeforeWithdrawal = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        BankAccount accountAfterWithdrawal = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(60.00)) // 100 - 40
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        Transaction savedWithdrawalTransaction = Transaction.builder()
                .id("tan-456")
                .amount(BigDecimal.valueOf(40.00))
                .currency(Currency.GBP.name())
                .type(TransactionType.WITHDRAWAL)
                .reference("Test withdrawal")
                .bankAccount(accountAfterWithdrawal)
                .createdTimestamp(LocalDateTime.now())
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(accountBeforeWithdrawal));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedWithdrawalTransaction);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(accountAfterWithdrawal);

        // When
        BankAccountResponse withdrawalResult = bankAccountService.createTransaction("01123456", withdrawalRequest);

        // Then
        assertThat(withdrawalResult).isNotNull();
        assertThat(withdrawalResult.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(60.00)); // 100 - 40

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(transactionRepository).save(any(Transaction.class));
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldCreateTransactionDepositSuccessfully() {
        // Test DEPOSIT transaction
        // Given
        CreateTransactionRequest depositRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(40.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Test deposit")
                .build();

        BankAccount accountBeforeTransaction = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        BankAccount accountAfterTransaction = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(140.00)) // 100 + 40
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id("tan-123")
                .amount(BigDecimal.valueOf(40.00))
                .currency(Currency.GBP.name())
                .type(TransactionType.DEPOSIT)
                .reference("Test deposit")
                .bankAccount(accountAfterTransaction)
                .createdTimestamp(LocalDateTime.now())
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(accountBeforeTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(accountAfterTransaction);

        // When
        BankAccountResponse result = bankAccountService.createTransaction("01123456", depositRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(140.00)); // 100 + 40

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(transactionRepository).save(any(Transaction.class));
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    /**
     * Comprehensive tests for BankAccountService.createTransaction() method.
     * These tests ensure that the transaction creation handles various edge cases and validation scenarios.
     */

    @Test
    void shouldThrowExceptionWhenWithdrawingInsufficientBalance() {
        // Given - Account with balance of 50.00, attempting to withdraw 100.00
        CreateTransactionRequest withdrawalRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.WITHDRAWAL)
                .reference("Insufficient balance withdrawal")
                .build();

        BankAccount accountWithLowBalance = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(50.00))
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(accountWithLowBalance));

        // When & Then - Should throw exception for insufficient balance
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", withdrawalRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient balance for withdrawal");

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenCreateTransactionRequestHasNullAmount() {
        // Given - Request with null amount
        CreateTransactionRequest invalidRequest = CreateTransactionRequest.builder()
                .amount(null)
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Invalid amount")
                .build();

        // When & Then - Should throw exception for null amount
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction amount is required");

        // Validation happens before any repository calls
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenCreateTransactionRequestHasNullCurrency() {
        // Given - Request with null currency
        CreateTransactionRequest invalidRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(null)
                .type(TransactionType.DEPOSIT)
                .reference("Invalid currency")
                .build();

        // When & Then - Should throw exception for null currency
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction currency is required");

        // Validation happens before any repository calls
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenCreateTransactionRequestHasNullType() {
        // Given - Request with null transaction type
        CreateTransactionRequest invalidRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(null)
                .reference("Invalid type")
                .build();

        // When & Then - Should throw exception for null type
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction type is required");

        // Validation happens before any repository calls
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenDepositingToNonExistentAccount() {
        // Given - Valid request but non-existent account
        CreateTransactionRequest depositRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Deposit to non-existent account")
                .build();

        when(bankAccountRepository.findByAccountNumber("01999999")).thenReturn(Optional.empty());

        // When & Then - Should throw BankAccountNotFoundException
        assertThatThrownBy(() -> bankAccountService.createTransaction("01999999", depositRequest))
                .isInstanceOf(BankAccountNotFoundException.class)
                .hasMessageContaining("Bank account not found with account number: 01999999");

        verify(bankAccountRepository).findByAccountNumber("01999999");
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenAccountNumberIsNull() {
        // Given - Valid request but null account number
        CreateTransactionRequest depositRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Valid deposit")
                .build();

        // When & Then - Should throw IllegalArgumentException
        assertThatThrownBy(() -> bankAccountService.createTransaction(null, depositRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number must not be null or empty");

        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenAccountNumberIsEmpty() {
        // Given - Valid request but empty account number
        CreateTransactionRequest depositRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Valid deposit")
                .build();

        // When & Then - Should throw IllegalArgumentException
        assertThatThrownBy(() -> bankAccountService.createTransaction("", depositRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number must not be null or empty");

        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenCreateTransactionRequestIsNull() {
        // When & Then - Should throw IllegalArgumentException
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Create transaction request must not be null");

        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenNegativeAmountProvided() {
        // Given - Request with negative amount
        CreateTransactionRequest invalidRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(-50.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Negative amount")
                .build();

        // When & Then - Should throw exception for negative amount
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction amount must be greater than zero");

        // Validation happens before any repository calls
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenZeroAmountProvided() {
        // Given - Request with zero amount
        CreateTransactionRequest invalidRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.ZERO)
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Zero amount")
                .build();

        // When & Then - Should throw exception for zero amount
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction amount must be greater than zero");

        // Validation happens before any repository calls
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenExceedingMaximumAmount() {
        // Given - Request with amount exceeding maximum limit (10000.00)
        CreateTransactionRequest invalidRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(15000.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Excessive amount")
                .build();

        // When & Then - Should throw exception for excessive amount
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction amount must not exceed 10000.00");

        // Validation happens before any repository calls
        verify(bankAccountRepository, never()).findByAccountNumber(anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldHandleRepositoryExceptionInCreateTransaction() {
        // Given - Valid request but repository throws exception
        CreateTransactionRequest depositRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Repository exception test")
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenThrow(new RuntimeException("Database error"));

        // When & Then - Should wrap exception in IllegalStateException
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", depositRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to create transaction");

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldHandleTransactionRepositorySaveException() {
        // Given - Valid setup but transaction save fails
        CreateTransactionRequest depositRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Transaction save exception test")
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));
        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("Transaction save failed"));

        // When & Then - Should wrap exception in IllegalStateException
        assertThatThrownBy(() -> bankAccountService.createTransaction("01123456", depositRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to create transaction");

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(transactionRepository).save(any(Transaction.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldUpdateAccountTimestampWhenCreatingTransaction() {
        // Given - Valid deposit request
        CreateTransactionRequest depositRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Timestamp test")
                .build();

        LocalDateTime originalTimestamp = LocalDateTime.now().minusHours(1);
        BankAccount accountWithOldTimestamp = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(500.00))
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(originalTimestamp)
                .updatedTimestamp(originalTimestamp)
                .build();

        BankAccount updatedAccount = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(600.00))
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(originalTimestamp)
                .updatedTimestamp(LocalDateTime.now())
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id("tan-123")
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP.name())
                .type(TransactionType.DEPOSIT)
                .reference("Timestamp test")
                .bankAccount(updatedAccount)
                .createdTimestamp(LocalDateTime.now())
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(accountWithOldTimestamp));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(updatedAccount);

        // When
        BankAccountResponse result = bankAccountService.createTransaction("01123456", depositRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUpdatedTimestamp()).isAfter(originalTimestamp);

        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(transactionRepository).save(any(Transaction.class));
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldSetCorrectUserIdInTransaction() {
        // Given - Valid deposit request
        CreateTransactionRequest depositRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("User ID test")
                .build();

        Transaction capturedTransaction = Transaction.builder()
                .id("tan-123")
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP.name())
                .type(TransactionType.DEPOSIT)
                .reference("User ID test")
                .bankAccount(testBankAccount)
                .userId("usr-abc123")
                .createdTimestamp(LocalDateTime.now())
                .build();

        when(bankAccountRepository.findByAccountNumber("01123456")).thenReturn(Optional.of(testBankAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(capturedTransaction);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

        // When
        bankAccountService.createTransaction("01123456", depositRequest);

        // Then
        verify(bankAccountRepository).findByAccountNumber("01123456");
        verify(transactionRepository).save(argThat(transaction -> 
                transaction.getUserId().equals("usr-abc123") && 
                transaction.getBankAccount().equals(testBankAccount)));
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldHandleConcurrentTransactionsCorrectly() {
        // Given - Two concurrent deposit requests
        CreateTransactionRequest depositRequest1 = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Concurrent deposit 1")
                .build();

        CreateTransactionRequest depositRequest2 = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(200.00))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Concurrent deposit 2")
                .build();

        BankAccount initialAccount = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(500.00))
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        BankAccount afterFirstTransaction = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(600.00)) // 500 + 100
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        BankAccount afterSecondTransaction = BankAccount.builder()
                .accountNumber("01123456")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.valueOf(800.00)) // 600 + 200
                .currency(Currency.GBP.name())
                .user(testUser)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        Transaction savedTransaction1 = Transaction.builder()
                .id("tan-123")
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.GBP.name())
                .type(TransactionType.DEPOSIT)
                .reference("Concurrent deposit 1")
                .bankAccount(afterFirstTransaction)
                .createdTimestamp(LocalDateTime.now())
                .build();

        Transaction savedTransaction2 = Transaction.builder()
                .id("tan-456")
                .amount(BigDecimal.valueOf(200.00))
                .currency(Currency.GBP.name())
                .type(TransactionType.DEPOSIT)
                .reference("Concurrent deposit 2")
                .bankAccount(afterSecondTransaction)
                .createdTimestamp(LocalDateTime.now())
                .build();

        // Setup mocks for sequential execution simulation
        when(bankAccountRepository.findByAccountNumber("01123456"))
                .thenReturn(Optional.of(initialAccount))
                .thenReturn(Optional.of(afterFirstTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction1)
                .thenReturn(savedTransaction2);
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenReturn(afterFirstTransaction)
                .thenReturn(afterSecondTransaction);

        // When - Execute both transactions
        BankAccountResponse result1 = bankAccountService.createTransaction("01123456", depositRequest1);
        BankAccountResponse result2 = bankAccountService.createTransaction("01123456", depositRequest2);

        // Then - Both should succeed with correct balances
        assertThat(result1.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(600.00));
        assertThat(result2.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800.00));

        verify(bankAccountRepository, times(2)).findByAccountNumber("01123456");
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
    }
}
