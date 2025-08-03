package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionType Enum Tests")
class TransactionTypeTest {

    @Test
    @DisplayName("Should have correct enum values")
    void shouldHaveCorrectEnumValues() {
        // Given & When
        TransactionType[] values = TransactionType.values();

        // Then
        assertThat(values).hasSize(2);
        assertThat(values).containsExactlyInAnyOrder(
            TransactionType.DEPOSIT, 
            TransactionType.WITHDRAWAL
        );
    }

    @Test
    @DisplayName("Should return correct values for valueOf")
    void shouldReturnCorrectValuesForValueOf() {
        // When & Then
        assertThat(TransactionType.valueOf("DEPOSIT")).isEqualTo(TransactionType.DEPOSIT);
        assertThat(TransactionType.valueOf("WITHDRAWAL")).isEqualTo(TransactionType.WITHDRAWAL);
    }

    @Test
    @DisplayName("Should have correct string representation")
    void shouldHaveCorrectStringRepresentation() {
        // When & Then
        assertThat(TransactionType.DEPOSIT.toString()).isEqualTo("DEPOSIT");
        assertThat(TransactionType.DEPOSIT.name()).isEqualTo("DEPOSIT");
        assertThat(TransactionType.WITHDRAWAL.toString()).isEqualTo("WITHDRAWAL");
        assertThat(TransactionType.WITHDRAWAL.name()).isEqualTo("WITHDRAWAL");
    }

    @Test
    @DisplayName("Should maintain consistent ordinals")
    void shouldMaintainConsistentOrdinals() {
        // When & Then
        assertThat(TransactionType.DEPOSIT.ordinal()).isEqualTo(0);
        assertThat(TransactionType.WITHDRAWAL.ordinal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should support enum comparison")
    void shouldSupportEnumComparison() {
        // Given
        TransactionType deposit1 = TransactionType.DEPOSIT;
        TransactionType deposit2 = TransactionType.DEPOSIT;
        TransactionType withdrawal = TransactionType.WITHDRAWAL;

        // When & Then
        assertThat(deposit1).isEqualTo(deposit2);
        assertThat(deposit1 == deposit2).isTrue();
        assertThat(deposit1).isNotEqualTo(withdrawal);
        assertThat(deposit1.compareTo(deposit2)).isEqualTo(0);
        assertThat(deposit1.compareTo(withdrawal)).isLessThan(0);
        assertThat(withdrawal.compareTo(deposit1)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should support switch statements")
    void shouldSupportSwitchStatements() {
        // Test DEPOSIT
        String depositResult = switch (TransactionType.DEPOSIT) {
            case DEPOSIT -> "Money In";
            case WITHDRAWAL -> "Money Out";
        };
        assertThat(depositResult).isEqualTo("Money In");

        // Test WITHDRAWAL
        String withdrawalResult = switch (TransactionType.WITHDRAWAL) {
            case DEPOSIT -> "Money In";
            case WITHDRAWAL -> "Money Out";
        };
        assertThat(withdrawalResult).isEqualTo("Money Out");
    }

    @Test
    @DisplayName("Should support traditional switch statements")
    void shouldSupportTraditionalSwitchStatements() {
        // Given
        TransactionType[] types = {TransactionType.DEPOSIT, TransactionType.WITHDRAWAL};
        
        for (TransactionType type : types) {
            String result;
            
            // When
            switch (type) {
                case DEPOSIT:
                    result = "Adding funds";
                    break;
                case WITHDRAWAL:
                    result = "Removing funds";
                    break;
                default:
                    result = "Unknown operation";
                    break;
            }

            // Then
            if (type == TransactionType.DEPOSIT) {
                assertThat(result).isEqualTo("Adding funds");
            } else if (type == TransactionType.WITHDRAWAL) {
                assertThat(result).isEqualTo("Removing funds");
            }
        }
    }

    @Test
    @DisplayName("Should be usable in collections")
    void shouldBeUsableInCollections() {
        // Given
        java.util.Set<TransactionType> typeSet = java.util.EnumSet.allOf(TransactionType.class);

        // When & Then
        assertThat(typeSet).hasSize(2);
        assertThat(typeSet).contains(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL);
    }

    @Test
    @DisplayName("Should support business logic scenarios")
    void shouldSupportBusinessLogicScenarios() {
        // Given
        TransactionType deposit = TransactionType.DEPOSIT;
        TransactionType withdrawal = TransactionType.WITHDRAWAL;

        // When & Then - Simulate business logic
        assertThat(isIncreasingBalance(deposit)).isTrue();
        assertThat(isIncreasingBalance(withdrawal)).isFalse();
        
        assertThat(requiresSufficientFunds(deposit)).isFalse();
        assertThat(requiresSufficientFunds(withdrawal)).isTrue();
    }

    // Helper methods for business logic testing
    private boolean isIncreasingBalance(TransactionType type) {
        return type == TransactionType.DEPOSIT;
    }

    private boolean requiresSufficientFunds(TransactionType type) {
        return type == TransactionType.WITHDRAWAL;
    }

    @Test
    @DisplayName("Should be serializable")
    void shouldBeSerializable() {
        // Given
        TransactionType deposit = TransactionType.DEPOSIT;
        TransactionType withdrawal = TransactionType.WITHDRAWAL;

        // When & Then
        assertThat(deposit).isInstanceOf(Enum.class);
        assertThat(withdrawal).isInstanceOf(Enum.class);
        // Enums are inherently serializable in Java
        assertThat(deposit).isNotNull();
        assertThat(withdrawal).isNotNull();
    }
}
