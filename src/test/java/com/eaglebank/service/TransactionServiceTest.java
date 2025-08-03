package com.eaglebank.service;

import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.Transaction;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.domain.User;
import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.ListTransactionsResponse;
import com.eaglebank.dto.TransactionResponse;
import com.eaglebank.exception.BankAccountNotFoundException;
import com.eaglebank.exception.CustomAccessDeniedException;
import com.eaglebank.exception.TransactionNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private BankAccount testBankAccount;
    private Transaction testTransaction;
    private CreateTransactionRequest createTransactionRequest;

    private static final String USER_ID = "usr-123";
    private static final String OTHER_USER_ID = "usr-456";
    private static final String ACCOUNT_NUMBER = "acc-123456";
    private static final String TRANSACTION_ID = "tan-789";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .name("John Doe")
                .phoneNumber("+442079460958")
                .email("test@example.com")
                .address(com.eaglebank.domain.Address.builder()
                        .line1("123 Test Street")
                        .town("London")
                        .county("Greater London")
                        .postcode("SW1A 1AA")
                        .build())
                .build();

        testBankAccount = BankAccount.builder()
                .accountNumber(ACCOUNT_NUMBER)
                .sortCode("12-34-56")
                .accountType(AccountType.PERSONAL)
                .balance(new BigDecimal("1000.00"))
                .currency("GBP")
                .user(testUser)
                .build();

        testTransaction = Transaction.builder()
                .id(TRANSACTION_ID)
                .amount(new BigDecimal("100.00"))
                .currency("GBP")
                .type(TransactionType.DEPOSIT)
                .reference("Test deposit")
                .bankAccount(testBankAccount)
                .userId(USER_ID)
                .createdTimestamp(LocalDateTime.now())
                .build();

        createTransactionRequest = CreateTransactionRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency(Currency.GBP)
                .type(TransactionType.DEPOSIT)
                .reference("Test deposit")
                .build();
    }

    @Nested
    @DisplayName("Create Transaction Tests")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction successfully for deposit")
        void shouldCreateTransactionSuccessfullyForDeposit() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

            // When
            TransactionResponse result = transactionService.createTransaction(ACCOUNT_NUMBER, createTransactionRequest, USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TRANSACTION_ID);
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
            assertThat(result.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(result.getReference()).isEqualTo("Test deposit");
            assertThat(result.getUserId()).isEqualTo(USER_ID);

            verify(transactionRepository).save(any(Transaction.class));
            verify(bankAccountRepository).save(testBankAccount);
        }

        @Test
        @DisplayName("Should create transaction successfully for withdrawal")
        void shouldCreateTransactionSuccessfullyForWithdrawal() {
            // Given
            CreateTransactionRequest withdrawalRequest = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("50.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .reference("Test withdrawal")
                    .build();

            Transaction withdrawalTransaction = Transaction.builder()
                    .id("tan-withdrawal")
                    .amount(new BigDecimal("50.00"))
                    .currency("GBP")
                    .type(TransactionType.WITHDRAWAL)
                    .reference("Test withdrawal")
                    .bankAccount(testBankAccount)
                    .userId(USER_ID)
                    .createdTimestamp(LocalDateTime.now())
                    .build();

            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(withdrawalTransaction);
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

            // When
            TransactionResponse result = transactionService.createTransaction(ACCOUNT_NUMBER, withdrawalRequest, USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAWAL);
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("50.00"));

            verify(transactionRepository).save(any(Transaction.class));
            verify(bankAccountRepository).save(testBankAccount);
        }

        @Test
        @DisplayName("Should throw BankAccountNotFoundException when account not found")
        void shouldThrowBankAccountNotFoundExceptionWhenAccountNotFound() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.createTransaction(ACCOUNT_NUMBER, createTransactionRequest, USER_ID))
                    .isInstanceOf(BankAccountNotFoundException.class)
                    .hasMessage("Bank account not found with account number: " + ACCOUNT_NUMBER);

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomAccessDeniedException when user doesn't own account")
        void shouldThrowCustomAccessDeniedExceptionWhenUserDoesntOwnAccount() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));

            // When & Then
            assertThatThrownBy(() -> transactionService.createTransaction(ACCOUNT_NUMBER, createTransactionRequest, OTHER_USER_ID))
                    .isInstanceOf(CustomAccessDeniedException.class)
                    .hasMessage("You don't have permission to create transactions for this account");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when insufficient funds for withdrawal")
        void shouldThrowIllegalArgumentExceptionWhenInsufficientFunds() {
            // Given
            CreateTransactionRequest withdrawalRequest = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("1500.00")) // More than balance of 1000
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .reference("Large withdrawal")
                    .build();

            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));

            // When & Then
            assertThatThrownBy(() -> transactionService.createTransaction(ACCOUNT_NUMBER, withdrawalRequest, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient funds. Available balance: 1000.00");

            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Transactions by Account Number Tests")
    class GetTransactionsByAccountNumberTests {

        @Test
        @DisplayName("Should get transactions by account number successfully")
        void shouldGetTransactionsByAccountNumberSuccessfully() {
            // Given
            List<Transaction> transactions = Arrays.asList(testTransaction);
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(transactions);

            // When
            ListTransactionsResponse result = transactionService.getTransactionsByAccountNumber(ACCOUNT_NUMBER, USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTransactions()).hasSize(1);
            assertThat(result.getTransactions().get(0).getId()).isEqualTo(TRANSACTION_ID);
            assertThat(result.getTransactions().get(0).getAmount()).isEqualTo(new BigDecimal("100.00"));

            verify(transactionRepository).findByAccountNumber(ACCOUNT_NUMBER);
        }

        @Test
        @DisplayName("Should return empty list when no transactions found")
        void shouldReturnEmptyListWhenNoTransactionsFound() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Arrays.asList());

            // When
            ListTransactionsResponse result = transactionService.getTransactionsByAccountNumber(ACCOUNT_NUMBER, USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTransactions()).isEmpty();

            verify(transactionRepository).findByAccountNumber(ACCOUNT_NUMBER);
        }

        @Test
        @DisplayName("Should throw BankAccountNotFoundException when account not found")
        void shouldThrowBankAccountNotFoundExceptionWhenAccountNotFound() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.getTransactionsByAccountNumber(ACCOUNT_NUMBER, USER_ID))
                    .isInstanceOf(BankAccountNotFoundException.class)
                    .hasMessage("Bank account not found with account number: " + ACCOUNT_NUMBER);

            verify(transactionRepository, never()).findByAccountNumber(anyString());
        }

        @Test
        @DisplayName("Should throw CustomAccessDeniedException when user doesn't own account")
        void shouldThrowCustomAccessDeniedExceptionWhenUserDoesntOwnAccount() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));

            // When & Then
            assertThatThrownBy(() -> transactionService.getTransactionsByAccountNumber(ACCOUNT_NUMBER, OTHER_USER_ID))
                    .isInstanceOf(CustomAccessDeniedException.class)
                    .hasMessage("You don't have permission to view transactions for this account");

            verify(transactionRepository, never()).findByAccountNumber(anyString());
        }
    }

    @Nested
    @DisplayName("Get Transaction by ID Tests")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("Should get transaction by ID successfully")
        void shouldGetTransactionByIdSuccessfully() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.findByAccountNumberAndTransactionId(ACCOUNT_NUMBER, TRANSACTION_ID))
                    .thenReturn(Optional.of(testTransaction));

            // When
            TransactionResponse result = transactionService.getTransactionById(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TRANSACTION_ID);
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
            assertThat(result.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);

            verify(transactionRepository).findByAccountNumberAndTransactionId(ACCOUNT_NUMBER, TRANSACTION_ID);
        }

        @Test
        @DisplayName("Should throw BankAccountNotFoundException when account not found")
        void shouldThrowBankAccountNotFoundExceptionWhenAccountNotFound() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.getTransactionById(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                    .isInstanceOf(BankAccountNotFoundException.class)
                    .hasMessage("Bank account not found with account number: " + ACCOUNT_NUMBER);

            verify(transactionRepository, never()).findByAccountNumberAndTransactionId(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw CustomAccessDeniedException when user doesn't own account")
        void shouldThrowCustomAccessDeniedExceptionWhenUserDoesntOwnAccount() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));

            // When & Then
            assertThatThrownBy(() -> transactionService.getTransactionById(ACCOUNT_NUMBER, TRANSACTION_ID, OTHER_USER_ID))
                    .isInstanceOf(CustomAccessDeniedException.class)
                    .hasMessage("You don't have permission to view transactions for this account");

            verify(transactionRepository, never()).findByAccountNumberAndTransactionId(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw TransactionNotFoundException when transaction not found")
        void shouldThrowTransactionNotFoundExceptionWhenTransactionNotFound() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.findByAccountNumberAndTransactionId(ACCOUNT_NUMBER, TRANSACTION_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.getTransactionById(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                    .isInstanceOf(TransactionNotFoundException.class)
                    .hasMessage("Transaction not found with ID: " + TRANSACTION_ID + " for account: " + ACCOUNT_NUMBER);

            verify(transactionRepository).findByAccountNumberAndTransactionId(ACCOUNT_NUMBER, TRANSACTION_ID);
        }
    }

    @Nested
    @DisplayName("Delete Transaction Tests")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Should delete deposit transaction successfully")
        void shouldDeleteDepositTransactionSuccessfully() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.findByAccountNumberAndTransactionId(ACCOUNT_NUMBER, TRANSACTION_ID))
                    .thenReturn(Optional.of(testTransaction));
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

            // When
            transactionService.deleteTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID);

            // Then
            verify(transactionRepository).delete(testTransaction);
            verify(bankAccountRepository).save(testBankAccount);
        }

        @Test
        @DisplayName("Should delete withdrawal transaction successfully")
        void shouldDeleteWithdrawalTransactionSuccessfully() {
            // Given
            Transaction withdrawalTransaction = Transaction.builder()
                    .id(TRANSACTION_ID)
                    .amount(new BigDecimal("50.00"))
                    .currency("GBP")
                    .type(TransactionType.WITHDRAWAL)
                    .reference("Test withdrawal")
                    .bankAccount(testBankAccount)
                    .userId(USER_ID)
                    .createdTimestamp(LocalDateTime.now())
                    .build();

            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.findByAccountNumberAndTransactionId(ACCOUNT_NUMBER, TRANSACTION_ID))
                    .thenReturn(Optional.of(withdrawalTransaction));
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

            // When
            transactionService.deleteTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID);

            // Then
            verify(transactionRepository).delete(withdrawalTransaction);
            verify(bankAccountRepository).save(testBankAccount);
        }

        @Test
        @DisplayName("Should throw BankAccountNotFoundException when account not found")
        void shouldThrowBankAccountNotFoundExceptionWhenAccountNotFound() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.deleteTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                    .isInstanceOf(BankAccountNotFoundException.class)
                    .hasMessage("Bank account not found with account number: " + ACCOUNT_NUMBER);

            verify(transactionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw CustomAccessDeniedException when user doesn't own account")
        void shouldThrowCustomAccessDeniedExceptionWhenUserDoesntOwnAccount() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));

            // When & Then
            assertThatThrownBy(() -> transactionService.deleteTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, OTHER_USER_ID))
                    .isInstanceOf(CustomAccessDeniedException.class)
                    .hasMessage("You don't have permission to delete transactions for this account");

            verify(transactionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw TransactionNotFoundException when transaction not found")
        void shouldThrowTransactionNotFoundExceptionWhenTransactionNotFound() {
            // Given
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.findByAccountNumberAndTransactionId(ACCOUNT_NUMBER, TRANSACTION_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.deleteTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                    .isInstanceOf(TransactionNotFoundException.class)
                    .hasMessage("Transaction not found with ID: " + TRANSACTION_ID + " for account: " + ACCOUNT_NUMBER);

            verify(transactionRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesAndIntegrationTests {

        @Test
        @DisplayName("Should handle null reference in transaction creation")
        void shouldHandleNullReferenceInTransactionCreation() {
            // Given
            CreateTransactionRequest requestWithNullReference = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(Currency.GBP)
                    .type(TransactionType.DEPOSIT)
                    .reference(null) // Null reference
                    .build();

            Transaction transactionWithNullReference = Transaction.builder()
                    .id(TRANSACTION_ID)
                    .amount(new BigDecimal("100.00"))
                    .currency("GBP")
                    .type(TransactionType.DEPOSIT)
                    .reference(null)
                    .bankAccount(testBankAccount)
                    .userId(USER_ID)
                    .createdTimestamp(LocalDateTime.now())
                    .build();

            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transactionWithNullReference);
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

            // When
            TransactionResponse result = transactionService.createTransaction(ACCOUNT_NUMBER, requestWithNullReference, USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getReference()).isNull();

            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle exact balance withdrawal")
        void shouldHandleExactBalanceWithdrawal() {
            // Given - Withdraw exactly the account balance
            CreateTransactionRequest exactBalanceRequest = CreateTransactionRequest.builder()
                    .amount(new BigDecimal("1000.00")) // Exact balance
                    .currency(Currency.GBP)
                    .type(TransactionType.WITHDRAWAL)
                    .reference("Full withdrawal")
                    .build();

            Transaction exactBalanceTransaction = Transaction.builder()
                    .id(TRANSACTION_ID)
                    .amount(new BigDecimal("1000.00"))
                    .currency("GBP")
                    .type(TransactionType.WITHDRAWAL)
                    .reference("Full withdrawal")
                    .bankAccount(testBankAccount)
                    .userId(USER_ID)
                    .createdTimestamp(LocalDateTime.now())
                    .build();

            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(testBankAccount));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(exactBalanceTransaction);
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testBankAccount);

            // When
            TransactionResponse result = transactionService.createTransaction(ACCOUNT_NUMBER, exactBalanceRequest, USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("1000.00"));
            assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAWAL);

            verify(transactionRepository).save(any(Transaction.class));
        }
    }
}
