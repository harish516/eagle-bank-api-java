package com.eaglebank.dto;

import com.eaglebank.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BankAccountResponse Tests")
class BankAccountResponseTest {

    private LocalDateTime testCreatedTimestamp;
    private LocalDateTime testUpdatedTimestamp;

    @BeforeEach
    void setUp() {
        testCreatedTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        testUpdatedTimestamp = LocalDateTime.of(2024, 1, 15, 11, 45, 30);
    }

    @Nested
    @DisplayName("Object Creation Tests")
    class ObjectCreationTests {

        @Test
        @DisplayName("Should create BankAccountResponse using builder with all fields")
        void shouldCreateWithBuilder() {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Personal Savings Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1500.75"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccountNumber()).isEqualTo("01234567");
            assertThat(response.getSortCode()).isEqualTo("10-10-10");
            assertThat(response.getName()).isEqualTo("Personal Savings Account");
            assertThat(response.getAccountType()).isEqualTo("personal");
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("1500.75"));
            assertThat(response.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(response.getCreatedTimestamp()).isEqualTo(testCreatedTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(testUpdatedTimestamp);
        }

        @Test
        @DisplayName("Should create BankAccountResponse using all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Given & When
            BankAccountResponse response = new BankAccountResponse(
                    "01765432",
                    "10-10-10",
                    "Business Current Account",
                    "personal",
                    new BigDecimal("2500.00"),
                    Currency.GBP,
                    testCreatedTimestamp,
                    testUpdatedTimestamp
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccountNumber()).isEqualTo("01765432");
            assertThat(response.getSortCode()).isEqualTo("10-10-10");
            assertThat(response.getName()).isEqualTo("Business Current Account");
            assertThat(response.getAccountType()).isEqualTo("personal");
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("2500.00"));
            assertThat(response.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(response.getCreatedTimestamp()).isEqualTo(testCreatedTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(testUpdatedTimestamp);
        }

        @Test
        @DisplayName("Should create BankAccountResponse using no-args constructor and setters")
        void shouldCreateWithNoArgsConstructorAndSetters() {
            // Given
            BankAccountResponse response = new BankAccountResponse();

            // When
            response.setAccountNumber("01111111");
            response.setSortCode("10-10-10");
            response.setName("Emergency Fund");
            response.setAccountType("personal");
            response.setBalance(new BigDecimal("750.25"));
            response.setCurrency(Currency.GBP);
            response.setCreatedTimestamp(testCreatedTimestamp);
            response.setUpdatedTimestamp(testUpdatedTimestamp);

            // Then
            assertThat(response.getAccountNumber()).isEqualTo("01111111");
            assertThat(response.getSortCode()).isEqualTo("10-10-10");
            assertThat(response.getName()).isEqualTo("Emergency Fund");
            assertThat(response.getAccountType()).isEqualTo("personal");
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("750.25"));
            assertThat(response.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(response.getCreatedTimestamp()).isEqualTo(testCreatedTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(testUpdatedTimestamp);
        }

        @Test
        @DisplayName("Should create BankAccountResponse with minimum balance")
        void shouldCreateWithMinimumBalance() {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01000000")
                    .sortCode("10-10-10")
                    .name("Zero Balance Account")
                    .accountType("personal")
                    .balance(new BigDecimal("0.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Should create BankAccountResponse with maximum balance")
        void shouldCreateWithMaximumBalance() {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01999999")
                    .sortCode("10-10-10")
                    .name("Maximum Balance Account")
                    .accountType("personal")
                    .balance(new BigDecimal("10000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("10000.00"));
        }
    }

    @Nested
    @DisplayName("Account Number Validation Tests")
    class AccountNumberValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"01234567", "01000000", "01999999", "01765432", "01111111"})
        @DisplayName("Should accept valid account numbers with format ^01\\d{6}$")
        void shouldAcceptValidAccountNumbers(String validAccountNumber) {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber(validAccountNumber)
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getAccountNumber()).isEqualTo(validAccountNumber);
            assertThat(response.getAccountNumber()).matches("^01\\d{6}$");
        }

        @Test
        @DisplayName("Should store account number exactly as provided")
        void shouldStoreAccountNumberExactlyAsProvided() {
            // Given
            String accountNumber = "01234567";

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber(accountNumber)
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getAccountNumber()).isEqualTo(accountNumber);
            assertThat(response.getAccountNumber()).hasSize(8);
            assertThat(response.getAccountNumber()).startsWith("01");
        }

        @Test
        @DisplayName("Should handle account number edge cases")
        void shouldHandleAccountNumberEdgeCases() {
            // Test minimum valid account number
            BankAccountResponse minResponse = BankAccountResponse.builder()
                    .accountNumber("01000000")
                    .sortCode("10-10-10")
                    .name("Min Account")
                    .accountType("personal")
                    .balance(new BigDecimal("0.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Test maximum valid account number
            BankAccountResponse maxResponse = BankAccountResponse.builder()
                    .accountNumber("01999999")
                    .sortCode("10-10-10")
                    .name("Max Account")
                    .accountType("personal")
                    .balance(new BigDecimal("10000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            assertThat(minResponse.getAccountNumber()).isEqualTo("01000000");
            assertThat(maxResponse.getAccountNumber()).isEqualTo("01999999");
        }
    }

    @Nested
    @DisplayName("Sort Code Validation Tests")
    class SortCodeValidationTests {

        @Test
        @DisplayName("Should accept valid sort code '10-10-10'")
        void shouldAcceptValidSortCode() {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getSortCode()).isEqualTo("10-10-10");
        }

        @Test
        @DisplayName("Should store sort code exactly as provided")
        void shouldStoreSortCodeExactlyAsProvided() {
            // Given
            String sortCode = "10-10-10";

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .sortCode(sortCode)
                    .accountNumber("01234567")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getSortCode()).isEqualTo(sortCode);
            assertThat(response.getSortCode()).contains("-");
            assertThat(response.getSortCode()).hasSize(8);
        }
    }

    @Nested
    @DisplayName("Account Type Validation Tests")
    class AccountTypeValidationTests {

        @Test
        @DisplayName("Should accept valid account type 'personal'")
        void shouldAcceptValidAccountType() {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Personal Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getAccountType()).isEqualTo("personal");
        }

        @Test
        @DisplayName("Should store account type exactly as provided")
        void shouldStoreAccountTypeExactlyAsProvided() {
            // Given
            String accountType = "personal";

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountType(accountType)
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getAccountType()).isEqualTo(accountType);
        }
    }

    @Nested
    @DisplayName("Balance Validation Tests")
    class BalanceValidationTests {

        @Test
        @DisplayName("Should accept valid balance within range 0.00 to 10000.00")
        void shouldAcceptValidBalance() {
            // Test various valid balances
            BigDecimal[] validBalances = {
                new BigDecimal("0.00"), 
                new BigDecimal("0.01"), 
                new BigDecimal("500.50"), 
                new BigDecimal("1000.00"), 
                new BigDecimal("5000.75"), 
                new BigDecimal("9999.99"), 
                new BigDecimal("10000.00")
            };

            for (BigDecimal balance : validBalances) {
                BankAccountResponse response = BankAccountResponse.builder()
                        .accountNumber("01234567")
                        .sortCode("10-10-10")
                        .name("Test Account")
                        .accountType("personal")
                        .balance(balance)
                        .currency(Currency.GBP)
                        .createdTimestamp(testCreatedTimestamp)
                        .updatedTimestamp(testUpdatedTimestamp)
                        .build();

                assertThat(response.getBalance()).isEqualTo(balance);
            }
        }

        @Test
        @DisplayName("Should handle minimum balance of 0.00")
        void shouldHandleMinimumBalance() {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Empty Account")
                    .accountType("personal")
                    .balance(new BigDecimal("0.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("0.00"));
            assertThat(response.getBalance()).isGreaterThanOrEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Should handle maximum balance of 10000.00")
        void shouldHandleMaximumBalance() {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Full Account")
                    .accountType("personal")
                    .balance(new BigDecimal("10000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("10000.00"));
            assertThat(response.getBalance()).isLessThanOrEqualTo(new BigDecimal("10000.00"));
        }

        @Test
        @DisplayName("Should handle decimal precision correctly")
        void shouldHandleDecimalPrecisionCorrectly() {
            // Given
            BigDecimal[] precisionBalances = {
                new BigDecimal("0.01"), 
                new BigDecimal("0.99"), 
                new BigDecimal("1.23"), 
                new BigDecimal("99.99"), 
                new BigDecimal("1234.56"), 
                new BigDecimal("9999.99")
            };

            for (BigDecimal balance : precisionBalances) {
                // When
                BankAccountResponse response = BankAccountResponse.builder()
                        .accountNumber("01234567")
                        .sortCode("10-10-10")
                        .name("Precision Test Account")
                        .accountType("personal")
                        .balance(balance)
                        .currency(Currency.GBP)
                        .createdTimestamp(testCreatedTimestamp)
                        .updatedTimestamp(testUpdatedTimestamp)
                        .build();

                // Then
                assertThat(response.getBalance()).isEqualTo(balance);
            }
        }
    }

    @Nested
    @DisplayName("Currency Validation Tests")
    class CurrencyValidationTests {

        @Test
        @DisplayName("Should accept valid currency 'GBP'")
        void shouldAcceptValidCurrency() {
            // Given & When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("GBP Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getCurrency()).isEqualTo(Currency.GBP);
        }

        @Test
        @DisplayName("Should store currency exactly as provided")
        void shouldStoreCurrencyExactlyAsProvided() {
            // Given
            Currency currency = Currency.GBP;

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .currency(currency)
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getCurrency()).isEqualTo(currency);
            assertThat(response.getCurrency().name()).hasSize(3);
            assertThat(response.getCurrency().name()).isEqualTo("GBP");
        }
    }

    @Nested
    @DisplayName("Name Field Tests")
    class NameFieldTests {

        @Test
        @DisplayName("Should accept various account names")
        void shouldAcceptVariousAccountNames() {
            // Given
            String[] validNames = {
                    "Personal Savings Account",
                    "Business Current Account",
                    "Emergency Fund",
                    "Holiday Savings",
                    "John's Account",
                    "Account 123",
                    "A"
            };

            for (String name : validNames) {
                // When
                BankAccountResponse response = BankAccountResponse.builder()
                        .accountNumber("01234567")
                        .sortCode("10-10-10")
                        .name(name)
                        .accountType("personal")
                        .balance(new BigDecimal("1000.00"))
                        .currency(Currency.GBP)
                        .createdTimestamp(testCreatedTimestamp)
                        .updatedTimestamp(testUpdatedTimestamp)
                        .build();

                // Then
                assertThat(response.getName()).isEqualTo(name);
            }
        }

        @Test
        @DisplayName("Should store name exactly as provided")
        void shouldStoreNameExactlyAsProvided() {
            // Given
            String accountName = "My Special Account Name";

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .name(accountName)
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getName()).isEqualTo(accountName);
        }

        @Test
        @DisplayName("Should handle names with special characters")
        void shouldHandleNamesWithSpecialCharacters() {
            // Given
            String specialName = "John & Jane's Joint Account - Main";

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .name(specialName)
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response.getName()).isEqualTo(specialName);
        }
    }

    @Nested
    @DisplayName("Timestamp Field Tests")
    class TimestampFieldTests {

        @Test
        @DisplayName("Should handle created and updated timestamps correctly")
        void shouldHandleTimestampsCorrectly() {
            // Given
            LocalDateTime created = LocalDateTime.of(2024, 1, 1, 9, 0, 0);
            LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 15, 30, 45);

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Timestamp Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(created)
                    .updatedTimestamp(updated)
                    .build();

            // Then
            assertThat(response.getCreatedTimestamp()).isEqualTo(created);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(updated);
            assertThat(response.getUpdatedTimestamp()).isAfter(response.getCreatedTimestamp());
        }

        @Test
        @DisplayName("Should handle same created and updated timestamps")
        void shouldHandleSameCreatedAndUpdatedTimestamps() {
            // Given
            LocalDateTime sameTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Same Timestamp Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(sameTimestamp)
                    .updatedTimestamp(sameTimestamp)
                    .build();

            // Then
            assertThat(response.getCreatedTimestamp()).isEqualTo(sameTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(sameTimestamp);
            assertThat(response.getCreatedTimestamp()).isEqualTo(response.getUpdatedTimestamp());
        }

        @Test
        @DisplayName("Should handle future timestamps")
        void shouldHandleFutureTimestamps() {
            // Given
            LocalDateTime futureTimestamp = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Future Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(futureTimestamp)
                    .updatedTimestamp(futureTimestamp)
                    .build();

            // Then
            assertThat(response.getCreatedTimestamp()).isEqualTo(futureTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(futureTimestamp);
        }
    }

    @Nested
    @DisplayName("Object Equality and Builder Tests")
    class ObjectEqualityAndBuilderTests {

        @Test
        @DisplayName("Should create equal objects with same values")
        void shouldCreateEqualObjectsWithSameValues() {
            // Given
            BankAccountResponse response1 = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            BankAccountResponse response2 = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should create different objects with different values")
        void shouldCreateDifferentObjectsWithDifferentValues() {
            // Given
            BankAccountResponse response1 = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Account One")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            BankAccountResponse response2 = BankAccountResponse.builder()
                    .accountNumber("01765432")
                    .sortCode("10-10-10")
                    .name("Account Two")
                    .accountType("personal")
                    .balance(new BigDecimal("2000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should support builder pattern modifications")
        void shouldSupportBuilderPatternModifications() {
            // Given
            BankAccountResponse originalResponse = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Original Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // When
            BankAccountResponse modifiedResponse = originalResponse.toBuilder()
                    .name("Modified Account")
                    .balance(new BigDecimal("2000.00"))
                    .build();

            // Then
            assertThat(originalResponse.getName()).isEqualTo("Original Account");
            assertThat(originalResponse.getBalance()).isEqualTo(new BigDecimal("1000.00"));
            assertThat(modifiedResponse.getName()).isEqualTo("Modified Account");
            assertThat(modifiedResponse.getBalance()).isEqualTo(new BigDecimal("2000.00"));
            assertThat(modifiedResponse.getAccountNumber()).isEqualTo("01234567"); // Unchanged
        }

        @Test
        @DisplayName("Should have proper toString representation")
        void shouldHaveProperToStringRepresentation() {
            // Given
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // When
            String toString = response.toString();

            // Then
            assertThat(toString).contains("BankAccountResponse");
            assertThat(toString).contains("01234567");
            assertThat(toString).contains("10-10-10");
            assertThat(toString).contains("Test Account");
            assertThat(toString).contains("personal");
            assertThat(toString).contains("1000.0");
            assertThat(toString).contains("GBP");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Should handle null object comparison")
        void shouldHandleNullObjectComparison() {
            // Given
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Test Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1000.00"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // When & Then
            assertThat(response).isNotEqualTo(null);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should handle concurrent object creation")
        void shouldHandleConcurrentObjectCreation() throws InterruptedException {
            // Given
            int numberOfThreads = 10;
            Thread[] threads = new Thread[numberOfThreads];
            BankAccountResponse[] responses = new BankAccountResponse[numberOfThreads];

            // When
            for (int i = 0; i < numberOfThreads; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    responses[index] = BankAccountResponse.builder()
                            .accountNumber("0123456" + index)
                            .sortCode("10-10-10")
                            .name("Concurrent Account " + index)
                            .accountType("personal")
                            .balance(new BigDecimal("1000.00").add(new BigDecimal(index)))
                            .currency(Currency.GBP)
                            .createdTimestamp(testCreatedTimestamp)
                            .updatedTimestamp(testUpdatedTimestamp)
                            .build();
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            for (int i = 0; i < numberOfThreads; i++) {
                assertThat(responses[i]).isNotNull();
                assertThat(responses[i].getName()).isEqualTo("Concurrent Account " + i);
                assertThat(responses[i].getBalance()).isEqualTo(new BigDecimal("1000.00").add(new BigDecimal(i)));
            }
        }

        @Test
        @DisplayName("Should handle balance precision edge cases")
        void shouldHandleBalancePrecisionEdgeCases() {
            // Given
            BigDecimal[] edgeCaseBalances = {
                    new BigDecimal("0.001"), // More than 2 decimal places
                    new BigDecimal("0.009"), // Rounding edge case
                    new BigDecimal("9999.999"), // Near maximum with extra precision
                    new BigDecimal("1234.567") // Multiple decimal places
            };

            for (BigDecimal balance : edgeCaseBalances) {
                // When
                BankAccountResponse response = BankAccountResponse.builder()
                        .accountNumber("01234567")
                        .sortCode("10-10-10")
                        .name("Precision Test Account")
                        .accountType("personal")
                        .balance(balance)
                        .currency(Currency.GBP)
                        .createdTimestamp(testCreatedTimestamp)
                        .updatedTimestamp(testUpdatedTimestamp)
                        .build();

                // Then
                assertThat(response.getBalance()).isEqualTo(balance);
            }
        }
    }

    @Nested
    @DisplayName("Integration with Builder Pattern Tests")
    class IntegrationWithBuilderPatternTests {

        @Test
        @DisplayName("Should support fluent builder chain")
        void shouldSupportFluentBuilderChain() {
            // When
            BankAccountResponse response = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .name("Fluent Account")
                    .accountType("personal")
                    .balance(new BigDecimal("1500.75"))
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .build();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccountNumber()).isEqualTo("01234567");
            assertThat(response.getSortCode()).isEqualTo("10-10-10");
            assertThat(response.getName()).isEqualTo("Fluent Account");
            assertThat(response.getAccountType()).isEqualTo("personal");
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("1500.75"));
            assertThat(response.getCurrency()).isEqualTo(Currency.GBP);
            assertThat(response.getCreatedTimestamp()).isEqualTo(testCreatedTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(testUpdatedTimestamp);
        }

        @Test
        @DisplayName("Should support partial builder usage")
        void shouldSupportPartialBuilderUsage() {
            // Given
            BankAccountResponse.BankAccountResponseBuilder builder = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .accountType("personal")
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp);

            // When
            BankAccountResponse response1 = builder
                    .name("Account One")
                    .balance(new BigDecimal("1000.00"))
                    .build();

            BankAccountResponse response2 = BankAccountResponse.builder()
                    .accountNumber("01234567")
                    .sortCode("10-10-10")
                    .accountType("personal")
                    .currency(Currency.GBP)
                    .createdTimestamp(testCreatedTimestamp)
                    .updatedTimestamp(testUpdatedTimestamp)
                    .name("Account Two")
                    .balance(new BigDecimal("2000.00"))
                    .build();

            // Then
            assertThat(response1.getName()).isEqualTo("Account One");
            assertThat(response1.getBalance()).isEqualTo(new BigDecimal("1000.00"));
            assertThat(response2.getName()).isEqualTo("Account Two");
            assertThat(response2.getBalance()).isEqualTo(new BigDecimal("2000.00"));
        }
    }
}
