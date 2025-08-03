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
}
