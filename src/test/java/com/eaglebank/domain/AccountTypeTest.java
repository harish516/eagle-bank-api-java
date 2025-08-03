package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountType Enum Tests")
class AccountTypeTest {

    @Test
    @DisplayName("Should have correct enum values")
    void shouldHaveCorrectEnumValues() {
        // Given & When
        AccountType[] values = AccountType.values();

        // Then
        assertThat(values).hasSize(1);
        assertThat(values).containsExactly(AccountType.PERSONAL);
    }

    @Test
    @DisplayName("Should return PERSONAL for valueOf")
    void shouldReturnPersonalForValueOf() {
        // When
        AccountType accountType = AccountType.valueOf("PERSONAL");

        // Then
        assertThat(accountType).isEqualTo(AccountType.PERSONAL);
    }

    @Test
    @DisplayName("Should have correct string representation")
    void shouldHaveCorrectStringRepresentation() {
        // When & Then
        assertThat(AccountType.PERSONAL.toString()).isEqualTo("PERSONAL");
        assertThat(AccountType.PERSONAL.name()).isEqualTo("PERSONAL");
    }

    @Test
    @DisplayName("Should maintain ordinal consistency")
    void shouldMaintainOrdinalConsistency() {
        // When & Then
        assertThat(AccountType.PERSONAL.ordinal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should support enum comparison")
    void shouldSupportEnumComparison() {
        // Given
        AccountType type1 = AccountType.PERSONAL;
        AccountType type2 = AccountType.PERSONAL;

        // When & Then
        assertThat(type1).isEqualTo(type2);
        assertThat(type1 == type2).isTrue();
        assertThat(type1.compareTo(type2)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should support switch statements")
    void shouldSupportSwitchStatements() {
        // Given
        AccountType accountType = AccountType.PERSONAL;
        String result;

        // When
        switch (accountType) {
            case PERSONAL:
                result = "Personal Account";
                break;
            default:
                result = "Unknown";
                break;
        }

        // Then
        assertThat(result).isEqualTo("Personal Account");
    }

    @Test
    @DisplayName("Should be serializable")
    void shouldBeSerializable() {
        // Given
        AccountType accountType = AccountType.PERSONAL;

        // When & Then
        assertThat(accountType).isInstanceOf(Enum.class);
        // Enums are inherently serializable in Java
        assertThat(accountType).isNotNull();
    }
}
