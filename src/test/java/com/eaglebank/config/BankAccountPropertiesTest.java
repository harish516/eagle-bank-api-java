package com.eaglebank.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@DisplayName("Bank Account Properties Tests")
class BankAccountPropertiesTest {

    private BankAccountProperties bankAccountProperties;

    @BeforeEach
    void setUp() {
        bankAccountProperties = new BankAccountProperties();
    }

    @Nested
    @DisplayName("Configuration and Annotation Tests")
    class ConfigurationAndAnnotationTests {

        @Test
        @DisplayName("Should have Component annotation")
        void shouldHaveComponentAnnotation() {
            // Then
            assertThat(BankAccountProperties.class.isAnnotationPresent(Component.class))
                .isTrue();
        }

        @Test
        @DisplayName("Should have ConfigurationProperties annotation with correct prefix")
        void shouldHaveConfigurationPropertiesAnnotationWithCorrectPrefix() {
            // Given
            ConfigurationProperties annotation = BankAccountProperties.class.getAnnotation(ConfigurationProperties.class);
            
            // Then
            assertThat(annotation).isNotNull();
            assertThat(annotation.prefix()).isEqualTo("eagle-bank.bank-account");
        }

        @Test
        @DisplayName("Should be a valid Spring configuration properties class")
        void shouldBeValidSpringConfigurationPropertiesClass() {
            // Then
            assertThat(BankAccountProperties.class.isAnnotationPresent(Component.class))
                .isTrue();
            assertThat(BankAccountProperties.class.isAnnotationPresent(ConfigurationProperties.class))
                .isTrue();
            // Class should not be abstract
            assertThat(java.lang.reflect.Modifier.isAbstract(BankAccountProperties.class.getModifiers()))
                .isFalse();
        }
    }

    @Nested
    @DisplayName("Default Sort Code Tests")
    class DefaultSortCodeTests {

        @Test
        @DisplayName("Should have default sort code set to 10-10-10")
        void shouldHaveDefaultSortCodeSetToCorrectValue() {
            // Then
            assertThat(bankAccountProperties.getDefaultSortCode()).isEqualTo("10-10-10");
        }

        @Test
        @DisplayName("Should allow setting custom sort code")
        void shouldAllowSettingCustomSortCode() {
            // Given
            String customSortCode = "20-20-20";

            // When
            bankAccountProperties.setDefaultSortCode(customSortCode);

            // Then
            assertThat(bankAccountProperties.getDefaultSortCode()).isEqualTo(customSortCode);
        }

        @Test
        @DisplayName("Should handle null sort code")
        void shouldHandleNullSortCode() {
            // When
            bankAccountProperties.setDefaultSortCode(null);

            // Then
            assertThat(bankAccountProperties.getDefaultSortCode()).isNull();
        }

        @Test
        @DisplayName("Should handle empty sort code")
        void shouldHandleEmptySortCode() {
            // When
            bankAccountProperties.setDefaultSortCode("");

            // Then
            assertThat(bankAccountProperties.getDefaultSortCode()).isEmpty();
        }

        @Test
        @DisplayName("Should handle various sort code formats")
        void shouldHandleVariousSortCodeFormats() {
            // Given
            String[] sortCodes = {
                "10-10-10",
                "20-30-40",
                "123456",
                "12-34-56",
                "ABCDEF"
            };

            // When/Then
            for (String sortCode : sortCodes) {
                bankAccountProperties.setDefaultSortCode(sortCode);
                assertThat(bankAccountProperties.getDefaultSortCode()).isEqualTo(sortCode);
            }
        }
    }

    @Nested
    @DisplayName("Object Method Tests")
    class ObjectMethodTests {

        @Test
        @DisplayName("Should support equals and hashCode contract")
        void shouldSupportEqualsAndHashCodeContract() {
            // Given
            BankAccountProperties properties1 = new BankAccountProperties();
            BankAccountProperties properties2 = new BankAccountProperties();
            properties1.setDefaultSortCode("10-10-10");
            properties2.setDefaultSortCode("10-10-10");

            // Then
            assertThat(properties1).isEqualTo(properties2);
            assertThat(properties1.hashCode()).isEqualTo(properties2.hashCode());
        }

        @Test
        @DisplayName("Should handle equals with different sort codes")
        void shouldHandleEqualsWithDifferentSortCodes() {
            // Given
            BankAccountProperties properties1 = new BankAccountProperties();
            BankAccountProperties properties2 = new BankAccountProperties();
            properties1.setDefaultSortCode("10-10-10");
            properties2.setDefaultSortCode("20-20-20");

            // Then
            assertThat(properties1).isNotEqualTo(properties2);
        }

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Given
            bankAccountProperties.setDefaultSortCode("10-10-10");

            // When
            String toString = bankAccountProperties.toString();

            // Then
            assertThat(toString).contains("BankAccountProperties");
            assertThat(toString).contains("defaultSortCode");
            assertThat(toString).contains("10-10-10");
        }

        @Test
        @DisplayName("Should handle toString with null sort code")
        void shouldHandleToStringWithNullSortCode() {
            // Given
            bankAccountProperties.setDefaultSortCode(null);

            // When
            String toString = bankAccountProperties.toString();

            // Then
            assertThat(toString).contains("BankAccountProperties");
            assertThat(toString).contains("defaultSortCode");
        }
    }

    @Nested
    @DisplayName("Property Binding Tests")
    class PropertyBindingTests {

        @Test
        @DisplayName("Should have getter for default sort code")
        void shouldHaveGetterForDefaultSortCode() throws Exception {
            // Given
            java.lang.reflect.Method getterMethod = BankAccountProperties.class.getMethod("getDefaultSortCode");
            
            // Then
            assertThat(getterMethod).isNotNull();
            assertThat(getterMethod.getReturnType()).isEqualTo(String.class);
            assertThat(java.lang.reflect.Modifier.isPublic(getterMethod.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Should have setter for default sort code")
        void shouldHaveSetterForDefaultSortCode() throws Exception {
            // Given
            java.lang.reflect.Method setterMethod = BankAccountProperties.class.getMethod("setDefaultSortCode", String.class);
            
            // Then
            assertThat(setterMethod).isNotNull();
            assertThat(setterMethod.getReturnType()).isEqualTo(void.class);
            assertThat(java.lang.reflect.Modifier.isPublic(setterMethod.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Should support property access through getter and setter")
        void shouldSupportPropertyAccessThroughGetterAndSetter() {
            // Given
            String testSortCode = "99-88-77";

            // When
            bankAccountProperties.setDefaultSortCode(testSortCode);
            String retrievedSortCode = bankAccountProperties.getDefaultSortCode();

            // Then
            assertThat(retrievedSortCode).isEqualTo(testSortCode);
        }
    }
}
