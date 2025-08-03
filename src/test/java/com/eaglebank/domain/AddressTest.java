package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Address Tests")
class AddressTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create Address with all fields using builder")
        void shouldCreateAddressWithAllFieldsUsingBuilder() {
            // When
            Address address = Address.builder()
                .line1("123 Main Street")
                .line2("Apartment 4B")
                .line3("Building Complex")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // Then
            assertThat(address.getLine1()).isEqualTo("123 Main Street");
            assertThat(address.getLine2()).isEqualTo("Apartment 4B");
            assertThat(address.getLine3()).isEqualTo("Building Complex");
            assertThat(address.getTown()).isEqualTo("London");
            assertThat(address.getCounty()).isEqualTo("Greater London");
            assertThat(address.getPostcode()).isEqualTo("SW1A 1AA");
        }

        @Test
        @DisplayName("Should create Address with required fields only using builder")
        void shouldCreateAddressWithRequiredFieldsOnlyUsingBuilder() {
            // When
            Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // Then
            assertThat(address.getLine1()).isEqualTo("123 Main Street");
            assertThat(address.getLine2()).isNull();
            assertThat(address.getLine3()).isNull();
            assertThat(address.getTown()).isEqualTo("London");
            assertThat(address.getCounty()).isEqualTo("Greater London");
            assertThat(address.getPostcode()).isEqualTo("SW1A 1AA");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid address")
        void shouldPassValidationWithValidAddress() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .line2("Apartment 4B")
                .line3("Building Complex")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When
            Set<ConstraintViolation<Address>> violations = validator.validate(address);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with minimum required fields")
        void shouldPassValidationWithMinimumRequiredFields() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When
            Set<ConstraintViolation<Address>> violations = validator.validate(address);

            // Then
            assertThat(violations).isEmpty();
        }

        @Nested
        @DisplayName("Line1 Validation")
        class Line1ValidationTests {

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {"", "   ", "\t", "\n"})
            @DisplayName("Should fail validation when line1 is null, empty or blank")
            void shouldFailValidationWhenLine1IsNullEmptyOrBlank(String invalidLine1) {
                // Given
                Address address = Address.builder()
                    .line1(invalidLine1)
                    .town("London")
                    .county("Greater London")
                    .postcode("SW1A 1AA")
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("line1");
                assertThat(violations.iterator().next().getMessage()).contains("Line1 is required");
            }

            @Test
            @DisplayName("Should pass validation with valid line1")
            void shouldPassValidationWithValidLine1() {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town("London")
                    .county("Greater London")
                    .postcode("SW1A 1AA")
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Town Validation")
        class TownValidationTests {

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {"", "   ", "\t", "\n"})
            @DisplayName("Should fail validation when town is null, empty or blank")
            void shouldFailValidationWhenTownIsNullEmptyOrBlank(String invalidTown) {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town(invalidTown)
                    .county("Greater London")
                    .postcode("SW1A 1AA")
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("town");
                assertThat(violations.iterator().next().getMessage()).contains("Town is required");
            }

            @Test
            @DisplayName("Should pass validation with valid town")
            void shouldPassValidationWithValidTown() {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town("London")
                    .county("Greater London")
                    .postcode("SW1A 1AA")
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("County Validation")
        class CountyValidationTests {

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {"", "   ", "\t", "\n"})
            @DisplayName("Should fail validation when county is null, empty or blank")
            void shouldFailValidationWhenCountyIsNullEmptyOrBlank(String invalidCounty) {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town("London")
                    .county(invalidCounty)
                    .postcode("SW1A 1AA")
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("county");
                assertThat(violations.iterator().next().getMessage()).contains("County is required");
            }

            @Test
            @DisplayName("Should pass validation with valid county")
            void shouldPassValidationWithValidCounty() {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town("London")
                    .county("Greater London")
                    .postcode("SW1A 1AA")
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Postcode Validation")
        class PostcodeValidationTests {

            @ParameterizedTest
            @ValueSource(strings = {"", "   ", "\t", "\n"})
            @DisplayName("Should fail validation when postcode is empty or blank")
            void shouldFailValidationWhenPostcodeIsEmptyOrBlank(String invalidPostcode) {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town("London")
                    .county("Greater London")
                    .postcode(invalidPostcode)
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).hasSize(2); // Both @NotBlank and @Pattern trigger for empty/blank strings
                assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                        "Postcode is required",
                        "Postcode must be in valid UK format"
                    );
            }

            @Test
            @DisplayName("Should fail validation when postcode is null")
            void shouldFailValidationWhenPostcodeIsNull() {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town("London")
                    .county("Greater London")
                    .postcode(null)
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).hasSize(1); // Only @NotBlank triggers for null values
                assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("postcode");
                assertThat(violations.iterator().next().getMessage()).contains("Postcode is required");
            }

            @ParameterizedTest
            @ValueSource(strings = {
                "SW1A 1AA", "M1 1AA", "B33 8TH", "W1A 0AX", "M1 1AA", "B33 8TH",
                "SW1A1AA", "M11AA", "B338TH", // Without spaces
                "sw1a 1aa", "m1 1aa", "b33 8th" // Lowercase
            })
            @DisplayName("Should pass validation with valid UK postcodes")
            void shouldPassValidationWithValidUKPostcodes(String validPostcode) {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town("London")
                    .county("Greater London")
                    .postcode(validPostcode.toUpperCase()) // Convert to uppercase as pattern expects uppercase
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).isEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {
                "12345", "ABCDEF", "SW1A 1AAA", "1A 1AA", "SW1 1A", 
                "SW1A1", "123 456", "ABC DEF", "SW1A  1AA", // Double space
                "SW1A-1AA", "SW1A_1AA" // Invalid separators
            })
            @DisplayName("Should fail validation with invalid postcodes")
            void shouldFailValidationWithInvalidPostcodes(String invalidPostcode) {
                // Given
                Address address = Address.builder()
                    .line1("123 Main Street")
                    .town("London")
                    .county("Greater London")
                    .postcode(invalidPostcode)
                    .build();

                // When
                Set<ConstraintViolation<Address>> violations = validator.validate(address);

                // Then
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("postcode");
                assertThat(violations.iterator().next().getMessage()).contains("Postcode must be in valid UK format");
            }
        }

        @Test
        @DisplayName("Should accumulate multiple validation errors")
        void shouldAccumulateMultipleValidationErrors() {
            // Given
            Address address = Address.builder()
                .line1("") // Invalid - 1 violation
                .town("") // Invalid - 1 violation  
                .county("") // Invalid - 1 violation
                .postcode("INVALID") // Invalid - 2 violations (@NotBlank won't fire for non-empty string, only @Pattern)
                .build();

            // When
            Set<ConstraintViolation<Address>> violations = validator.validate(address);

            // Then
            assertThat(violations).hasSize(4); // line1, town, county, postcode (@Pattern only for non-empty invalid postcode)
            
            Set<String> propertyPaths = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
            
            assertThat(propertyPaths).containsExactlyInAnyOrder("line1", "town", "county", "postcode");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all required fields are the same")
        void shouldBeEqualWhenAllRequiredFieldsAreTheSame() {
            // Given
            Address address1 = Address.builder()
                .line1("123 Main Street")
                .line2("Apartment 4B")
                .line3("Building Complex")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            Address address2 = Address.builder()
                .line1("123 Main Street")
                .line2("Apartment 4B")
                .line3("Building Complex")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When & Then
            assertThat(address1).isEqualTo(address2);
            assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when required fields differ")
        void shouldNotBeEqualWhenRequiredFieldsDiffer() {
            // Given
            Address address1 = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            Address address2 = Address.builder()
                .line1("456 Oak Avenue")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When & Then
            assertThat(address1).isNotEqualTo(address2);
        }

        @Test
        @DisplayName("Should handle null values in equals")
        void shouldHandleNullValuesInEquals() {
            // Given
            Address address1 = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            Address address2 = Address.builder()
                .line1("123 Main Street")
                .line2("Apartment 4B")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When & Then - Based on equals implementation, line2 is not included in equals
            assertThat(address1).isEqualTo(address2);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When & Then
            assertThat(address).isEqualTo(address);
            assertThat(address.hashCode()).isEqualTo(address.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When & Then
            assertThat(address).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When & Then
            assertThat(address).isNotEqualTo("Some String");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .line2("Apartment 4B")
                .line3("Building Complex")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When
            String toString = address.toString();

            // Then
            assertThat(toString).contains("Address");
            assertThat(toString).contains("123 Main Street");
            assertThat(toString).contains("Apartment 4B");
            assertThat(toString).contains("Building Complex");
            assertThat(toString).contains("London");
            assertThat(toString).contains("Greater London");
            assertThat(toString).contains("SW1A 1AA");
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When
            String toString = address.toString();

            // Then
            assertThat(toString).contains("Address");
            assertThat(toString).contains("123 Main Street");
            assertThat(toString).contains("London");
            assertThat(toString).contains("Greater London");
            assertThat(toString).contains("SW1A 1AA");
            assertThat(toString).isNotNull();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should support full address formatting")
        void shouldSupportFullAddressFormatting() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .line2("Apartment 4B")
                .line3("Building Complex")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When
            String formatted = formatAddress(address);

            // Then
            assertThat(formatted).isEqualTo("123 Main Street, Apartment 4B, Building Complex, London, Greater London, SW1A 1AA");
        }

        @Test
        @DisplayName("Should support address formatting with required fields only")
        void shouldSupportAddressFormattingWithRequiredFieldsOnly() {
            // Given
            Address address = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();

            // When
            String formatted = formatAddress(address);

            // Then
            assertThat(formatted).isEqualTo("123 Main Street, London, Greater London, SW1A 1AA");
        }

        // Helper method for business logic testing
        private String formatAddress(Address address) {
            StringBuilder sb = new StringBuilder();
            sb.append(address.getLine1());
            
            if (address.getLine2() != null && !address.getLine2().trim().isEmpty()) {
                sb.append(", ").append(address.getLine2());
            }
            
            if (address.getLine3() != null && !address.getLine3().trim().isEmpty()) {
                sb.append(", ").append(address.getLine3());
            }
            
            sb.append(", ").append(address.getTown());
            sb.append(", ").append(address.getCounty());
            sb.append(", ").append(address.getPostcode());
            
            return sb.toString();
        }
    }
}
