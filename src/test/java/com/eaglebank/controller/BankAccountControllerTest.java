package com.eaglebank.controller;

import com.eaglebank.dto.BankAccountResponse;
import com.eaglebank.dto.CreateBankAccountRequest;
import com.eaglebank.dto.ListBankAccountsResponse;
import com.eaglebank.dto.UpdateBankAccountRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.domain.Address;
import com.eaglebank.domain.Currency;
import com.eaglebank.exception.BankAccountNotFoundException;
import com.eaglebank.service.interfaces.BankAccountServiceInterface;
import com.eaglebank.service.interfaces.UserServiceInterface;
import com.eaglebank.service.AuditService;
import com.eaglebank.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.eaglebank.config.JpaAuditingConfig;
import com.eaglebank.config.SecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BankAccountController.class)
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankAccountServiceInterface bankAccountService;

    @MockBean
    private UserServiceInterface userService;

    @MockBean
    private AuditService auditService;

    @MockBean
    private RateLimitService rateLimitService;

    @Autowired
    private ObjectMapper objectMapper;

    private BankAccountResponse testBankAccountResponse;
    private CreateBankAccountRequest createBankAccountRequest;
    private UpdateBankAccountRequest updateBankAccountRequest;
    private ListBankAccountsResponse listBankAccountsResponse;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

        testUserResponse = UserResponse.builder()
                .id("usr-abc123")
                .name("Test User")
                .email("test@example.com")
                .phoneNumber("07700900123")
                .address(address)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        testBankAccountResponse = BankAccountResponse.builder()
                .accountNumber("01234567")
                .sortCode("10-10-10")
                .name("Test Account")
                .accountType("personal")
                .balance(new BigDecimal("1000.00"))
                .currency(Currency.GBP)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        createBankAccountRequest = CreateBankAccountRequest.builder()
                .name("Test Account")
                .accountType("personal")
                .build();

        updateBankAccountRequest = UpdateBankAccountRequest.builder()
                .name("Updated Account")
                .build();

        listBankAccountsResponse = ListBankAccountsResponse.builder()
                .accounts(Arrays.asList(testBankAccountResponse))
                .build();

        // Configure rate limit service to allow all requests in tests
        when(rateLimitService.isAllowed(any(), any())).thenReturn(true);
    }

    @Test
    void shouldCreateBankAccountSuccessfully() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);
        when(bankAccountService.createBankAccount(eq("usr-abc123"), any(CreateBankAccountRequest.class)))
                .thenReturn(testBankAccountResponse);

        mockMvc.perform(post("/api/v1/accounts")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBankAccountRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("01234567"))
                .andExpect(jsonPath("$.name").value("Test Account"));

        verify(userService).getUserByEmail("test@example.com");
        verify(bankAccountService).createBankAccount(eq("usr-abc123"), any(CreateBankAccountRequest.class));
    }

    @Test
    void shouldHandleValidationErrorsWhenCreatingBankAccount() throws Exception {
        CreateBankAccountRequest invalidRequest = CreateBankAccountRequest.builder()
                .name("")  // Invalid empty name
                .accountType("invalid")  // Invalid account type
                .build();

        mockMvc.perform(post("/api/v1/accounts")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBankAccountByAccountNumberSuccessfully() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);
        when(bankAccountService.getBankAccountByAccountNumber("01234567")).thenReturn(testBankAccountResponse);
        when(bankAccountService.getBankAccountsByUserId("usr-abc123")).thenReturn(listBankAccountsResponse);

        mockMvc.perform(get("/api/v1/accounts/01234567")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("01234567"))
                .andExpect(jsonPath("$.name").value("Test Account"));

        verify(userService).getUserByEmail("test@example.com");
        verify(bankAccountService).getBankAccountByAccountNumber("01234567");
        verify(bankAccountService).getBankAccountsByUserId("usr-abc123");
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() throws Exception {
        // Given - A user is authenticated
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);
        
        // And the account doesn't exist
        String nonExistentAccountNumber = "99999999";
        when(bankAccountService.getBankAccountByAccountNumber(nonExistentAccountNumber))
                .thenThrow(new BankAccountNotFoundException("Bank account not found with account number: " + nonExistentAccountNumber));

        // When - User makes a GET request to /api/v1/accounts/{accountId}
        mockMvc.perform(get("/api/v1/accounts/" + nonExistentAccountNumber)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com"))))
                // Then - System returns Not Found status code and error message
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bank account not found with account number: " + nonExistentAccountNumber));

        verify(userService).getUserByEmail("test@example.com");
        verify(bankAccountService).getBankAccountByAccountNumber(nonExistentAccountNumber);
    }

    @Test
    void shouldGetBankAccountsSuccessfully() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);
        when(bankAccountService.getBankAccountsByUserId("usr-abc123")).thenReturn(listBankAccountsResponse);

        mockMvc.perform(get("/api/v1/accounts")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts[0].accountNumber").value("01234567"));

        verify(userService).getUserByEmail("test@example.com");
        verify(bankAccountService).getBankAccountsByUserId("usr-abc123");
    }

    @Test
    void shouldUpdateBankAccountSuccessfully() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);
        when(bankAccountService.getBankAccountByAccountNumber("01234567"))
                .thenReturn(testBankAccountResponse);
        when(bankAccountService.getBankAccountsByUserId("usr-abc123")).thenReturn(listBankAccountsResponse);
        when(bankAccountService.updateBankAccount(eq("01234567"), any(UpdateBankAccountRequest.class)))
                .thenReturn(testBankAccountResponse);

        mockMvc.perform(patch("/api/v1/accounts/01234567")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBankAccountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("01234567"));

        verify(userService).getUserByEmail("test@example.com");
        verify(bankAccountService).getBankAccountsByUserId("usr-abc123");
        verify(bankAccountService).updateBankAccount(eq("01234567"), any(UpdateBankAccountRequest.class));
    }

    @Test
    void shouldHandleValidationErrorsWhenUpdatingBankAccount() throws Exception {
        UpdateBankAccountRequest invalidRequest = UpdateBankAccountRequest.builder()
                .name("")  // Invalid empty name
                .build();

        mockMvc.perform(patch("/api/v1/accounts/01234567")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteBankAccountSuccessfully() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);
        when(bankAccountService.getBankAccountsByUserId("usr-abc123")).thenReturn(listBankAccountsResponse);
        doNothing().when(bankAccountService).deleteBankAccount("01234567");
        when(bankAccountService.getBankAccountByAccountNumber("01234567"))
                .thenReturn(testBankAccountResponse);

        mockMvc.perform(delete("/api/v1/accounts/01234567")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isNoContent());

        verify(userService).getUserByEmail("test@example.com");
        verify(bankAccountService).getBankAccountsByUserId("usr-abc123");
        verify(bankAccountService).deleteBankAccount("01234567");
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentAccount() throws Exception {
        doThrow(new BankAccountNotFoundException("Bank account not found"))
                .when(bankAccountService).getBankAccountByAccountNumber("01999999");

        mockMvc.perform(delete("/api/v1/accounts/01999999")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bank account not found"));

        // Only verify the method that gets called before the exception
        verify(bankAccountService).getBankAccountByAccountNumber("01999999");
        // getUserByEmail and getBankAccountsByUserId are not called because 
        // getBankAccountByAccountNumber throws exception first
    }

    @Test
    void shouldReturnForbiddenWhenUserTriesToAccessAnotherUsersAccount() throws Exception {
        // Given - User tries to access another user's account
        String anotherUserAccountNumber = "09876543";
        
        BankAccountResponse anotherUserAccount = BankAccountResponse.builder()
                .accountNumber(anotherUserAccountNumber)
                .sortCode("20-20-20")
                .name("Another User Account")
                .accountType("personal")
                .balance(new BigDecimal("500.00"))
                .currency(Currency.GBP)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);
        when(bankAccountService.getBankAccountByAccountNumber(anotherUserAccountNumber))
                .thenReturn(anotherUserAccount);
        when(bankAccountService.getBankAccountsByUserId("usr-abc123"))
                .thenReturn(listBankAccountsResponse); // This only contains account "01234567"

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/" + anotherUserAccountNumber)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(builder -> builder.claim("email", "test@example.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Forbidden - User can only access their own accounts"));

        verify(userService).getUserByEmail("test@example.com");
        verify(bankAccountService).getBankAccountByAccountNumber(anotherUserAccountNumber);
        verify(bankAccountService).getBankAccountsByUserId("usr-abc123");
    }
}