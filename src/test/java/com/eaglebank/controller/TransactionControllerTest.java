package com.eaglebank.controller;

import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.ListTransactionsResponse;
import com.eaglebank.dto.TransactionResponse;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.service.interfaces.TransactionServiceInterface;
import com.eaglebank.service.interfaces.UserServiceInterface;
import com.eaglebank.service.AuditService;
import com.eaglebank.config.CustomAuthenticationEntryPoint;
import com.eaglebank.config.CustomAccessDeniedHandler;
import com.eaglebank.filter.RateLimitFilter;
import com.eaglebank.interceptor.SecurityAuditInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class)
@TestPropertySource(properties = {
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.example.com",
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://test.example.com/.well-known/jwks.json"
})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionServiceInterface transactionService;

    @MockBean
    private UserServiceInterface userService;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private SecurityAuditInterceptor securityAuditInterceptor;

    @MockBean
    private AuditService auditService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateTransactionRequest createTransactionRequest;
    private TransactionResponse transactionResponse;
    private ListTransactionsResponse listTransactionsResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        createTransactionRequest = CreateTransactionRequest.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .currency(Currency.GBP)
                .reference("Test deposit")
                .build();

        transactionResponse = TransactionResponse.builder()
                .id("tan-123")
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .currency(Currency.GBP)
                .reference("Test deposit")
                .userId("usr-123")
                .createdTimestamp(LocalDateTime.now())
                .build();

        listTransactionsResponse = ListTransactionsResponse.builder()
                .transactions(Collections.singletonList(transactionResponse))
                .build();

        userResponse = UserResponse.builder()
                .id("usr-123")
                .email("test@example.com")
                .name("Test User")
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createTransaction_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userResponse);
        when(transactionService.createTransaction(eq("01234567"), any(CreateTransactionRequest.class), eq("usr-123")))
                .thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/accounts/01234567/transactions")
                        .with(jwt().jwt(jwt -> jwt.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTransactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("tan-123"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.reference").value("Test deposit"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTransactionsByAccount_WithValidAccount_ShouldReturnTransactions() throws Exception {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userResponse);
        when(transactionService.getTransactionsByAccountNumber("01234567", "usr-123"))
                .thenReturn(listTransactionsResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/accounts/01234567/transactions")
                        .with(jwt().jwt(jwt -> jwt.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].id").value("tan-123"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTransactionById_WithValidIds_ShouldReturnTransaction() throws Exception {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userResponse);
        when(transactionService.getTransactionById("01234567", "tan-123", "usr-123"))
                .thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/accounts/01234567/transactions/tan-123")
                        .with(jwt().jwt(jwt -> jwt.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("tan-123"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"));
    }

    @Test
    void createTransaction_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/accounts/01234567/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTransactionRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTransactionsByAccount_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/accounts/01234567/transactions"))
                .andExpect(status().isUnauthorized());
    }
}
