package com.eaglebank.service;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.User;
import com.eaglebank.dto.CreateUserAndAccountRequest;
import com.eaglebank.dto.CreateUserAndAccountResponse;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the composite createUserAndAccount operation in {@link UserService}.
 * These are pure unit tests (mock based) – transactional rollback semantics are verified
 * at integration level; here we assert control flow & repository interactions.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceCompositeCreationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private UserService userService;

    private CreateUserAndAccountRequest request;
    private Address address;
    private User persistedUser;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .line1("1 High Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        request = CreateUserAndAccountRequest.builder()
                .name("Jane Doe")
                .address(address)
                .phoneNumber("+441234567890")
                .email("jane.doe@example.com")
                .accountName("Jane Personal Account")
                .accountType("personal")
                .build();

        persistedUser = User.builder()
                .id("usr-test123")
                .name(request.getName())
                .address(address)
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateUserAndAccountSuccessfully() {
        // email not taken
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        // user save returns persistedUser (id populated)
        when(userRepository.save(any(User.class))).thenReturn(persistedUser);
        // account number uniqueness check – always false (unique)
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        // bank account save returns the same passed instance with timestamps preserved
        ArgumentCaptor<BankAccount> baCaptor = ArgumentCaptor.forClass(BankAccount.class);
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateUserAndAccountResponse response = userService.createUserAndAccount(request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(persistedUser.getId());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getAccountName()).isEqualTo(request.getAccountName());
        assertThat(response.getAccountType()).isEqualTo("personal");
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAccountNumber()).isNotBlank();

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).save(any(User.class));
        verify(bankAccountRepository, atLeastOnce()).existsByAccountNumber(anyString());
        verify(bankAccountRepository).save(baCaptor.capture());

        BankAccount savedAccount = baCaptor.getValue();
        assertThat(savedAccount.getUser()).isNotNull();
        assertThat(savedAccount.getAccountType()).isEqualTo(AccountType.PERSONAL);
        assertThat(savedAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldFailWhenAccountTypeInvalidAndNotPersistBankAccount() {
        // Arrange invalid account type to trigger AccountType.valueOf exception
        request.setAccountType("invalidType");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(persistedUser);
    // generation of account number will query repository; allow it
    when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.createUserAndAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant");

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).save(any(User.class));
    // Account number uniqueness check may occur, but bank account must not be saved
    verify(bankAccountRepository, atLeastOnce()).existsByAccountNumber(anyString());
    verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUserAndAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with email already exists");

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(bankAccountRepository);
    }
}
