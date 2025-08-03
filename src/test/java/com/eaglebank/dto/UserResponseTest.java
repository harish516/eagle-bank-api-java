package com.eaglebank.dto;

import com.eaglebank.domain.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserResponse Tests")
class UserResponseTest {

    private Address validAddress;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        validAddress = Address.builder()
                .line1("123 Main Street")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 1AA")
                .build();
        
        testTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
    }

    @Nested
    @DisplayName("Object Creation Tests")
    class ObjectCreationTests {

        @Test
        @DisplayName("Should create UserResponse using builder with all fields")
        void shouldCreateWithBuilder() {
            // Given
            LocalDateTime createdTimestamp = testTimestamp;
            LocalDateTime updatedTimestamp = testTimestamp.plusHours(1);

            // When
            UserResponse response = UserResponse.builder()
                    .id("usr-123456")
                    .name("John Doe")
                    .address(validAddress)
                    .phoneNumber("+1234567890")
                    .email("john.doe@example.com")
                    .createdTimestamp(createdTimestamp)
                    .updatedTimestamp(updatedTimestamp)
                    .build();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("usr-123456");
            assertThat(response.getName()).isEqualTo("John Doe");
            assertThat(response.getAddress()).isEqualTo(validAddress);
            assertThat(response.getPhoneNumber()).isEqualTo("+1234567890");
            assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(response.getCreatedTimestamp()).isEqualTo(createdTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(updatedTimestamp);
        }

        @Test
        @DisplayName("Should create UserResponse using all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Given
            LocalDateTime createdTimestamp = testTimestamp;
            LocalDateTime updatedTimestamp = testTimestamp.plusDays(1);

            // When
            UserResponse response = new UserResponse(
                    "usr-789012",
                    "Jane Smith",
                    validAddress,
                    "+44123456789",
                    "jane.smith@test.com",
                    createdTimestamp,
                    updatedTimestamp
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("usr-789012");
            assertThat(response.getName()).isEqualTo("Jane Smith");
            assertThat(response.getAddress()).isEqualTo(validAddress);
            assertThat(response.getPhoneNumber()).isEqualTo("+44123456789");
            assertThat(response.getEmail()).isEqualTo("jane.smith@test.com");
            assertThat(response.getCreatedTimestamp()).isEqualTo(createdTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(updatedTimestamp);
        }

        @Test
        @DisplayName("Should create UserResponse using no-args constructor")
        void shouldCreateWithNoArgsConstructor() {
            // When
            UserResponse response = new UserResponse();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNull();
            assertThat(response.getName()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getPhoneNumber()).isNull();
            assertThat(response.getEmail()).isNull();
            assertThat(response.getCreatedTimestamp()).isNull();
            assertThat(response.getUpdatedTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should create with partial fields using builder")
        void shouldCreateWithPartialFields() {
            // When
            UserResponse response = UserResponse.builder()
                    .id("usr-123")
                    .name("Minimal User")
                    .email("minimal@example.com")
                    .build();

            // Then
            assertThat(response.getId()).isEqualTo("usr-123");
            assertThat(response.getName()).isEqualTo("Minimal User");
            assertThat(response.getEmail()).isEqualTo("minimal@example.com");
            assertThat(response.getAddress()).isNull();
            assertThat(response.getPhoneNumber()).isNull();
            assertThat(response.getCreatedTimestamp()).isNull();
            assertThat(response.getUpdatedTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should create with null values")
        void shouldCreateWithNullValues() {
            // When
            UserResponse response = UserResponse.builder()
                    .id(null)
                    .name(null)
                    .address(null)
                    .phoneNumber(null)
                    .email(null)
                    .createdTimestamp(null)
                    .updatedTimestamp(null)
                    .build();

            // Then
            assertThat(response.getId()).isNull();
            assertThat(response.getName()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getPhoneNumber()).isNull();
            assertThat(response.getEmail()).isNull();
            assertThat(response.getCreatedTimestamp()).isNull();
            assertThat(response.getUpdatedTimestamp()).isNull();
        }
    }

    @Nested
    @DisplayName("Property Access Tests")
    class PropertyAccessTests {

        @Test
        @DisplayName("Should set and get id property")
        void shouldSetAndGetId() {
            // Given
            UserResponse response = new UserResponse();
            String expectedId = "usr-test-123";

            // When
            response.setId(expectedId);

            // Then
            assertThat(response.getId()).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("Should set and get name property")
        void shouldSetAndGetName() {
            // Given
            UserResponse response = new UserResponse();
            String expectedName = "Test User Name";

            // When
            response.setName(expectedName);

            // Then
            assertThat(response.getName()).isEqualTo(expectedName);
        }

        @Test
        @DisplayName("Should set and get address property")
        void shouldSetAndGetAddress() {
            // Given
            UserResponse response = new UserResponse();

            // When
            response.setAddress(validAddress);

            // Then
            assertThat(response.getAddress()).isEqualTo(validAddress);
        }

        @Test
        @DisplayName("Should set and get phoneNumber property")
        void shouldSetAndGetPhoneNumber() {
            // Given
            UserResponse response = new UserResponse();
            String expectedPhoneNumber = "+9876543210";

            // When
            response.setPhoneNumber(expectedPhoneNumber);

            // Then
            assertThat(response.getPhoneNumber()).isEqualTo(expectedPhoneNumber);
        }

        @Test
        @DisplayName("Should set and get email property")
        void shouldSetAndGetEmail() {
            // Given
            UserResponse response = new UserResponse();
            String expectedEmail = "setter.test@example.com";

            // When
            response.setEmail(expectedEmail);

            // Then
            assertThat(response.getEmail()).isEqualTo(expectedEmail);
        }

        @Test
        @DisplayName("Should set and get createdTimestamp property")
        void shouldSetAndGetCreatedTimestamp() {
            // Given
            UserResponse response = new UserResponse();
            LocalDateTime expectedTimestamp = LocalDateTime.of(2023, 12, 1, 9, 0, 0);

            // When
            response.setCreatedTimestamp(expectedTimestamp);

            // Then
            assertThat(response.getCreatedTimestamp()).isEqualTo(expectedTimestamp);
        }

        @Test
        @DisplayName("Should set and get updatedTimestamp property")
        void shouldSetAndGetUpdatedTimestamp() {
            // Given
            UserResponse response = new UserResponse();
            LocalDateTime expectedTimestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

            // When
            response.setUpdatedTimestamp(expectedTimestamp);

            // Then
            assertThat(response.getUpdatedTimestamp()).isEqualTo(expectedTimestamp);
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should support fluent builder pattern")
        void shouldSupportFluentBuilderPattern() {
            // Given & When
            UserResponse response = UserResponse.builder()
                    .id("usr-fluent-test")
                    .name("Fluent User")
                    .address(validAddress)
                    .phoneNumber("+1111111111")
                    .email("fluent@example.com")
                    .createdTimestamp(testTimestamp)
                    .updatedTimestamp(testTimestamp.plusMinutes(30))
                    .build();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("usr-fluent-test");
            assertThat(response.getName()).isEqualTo("Fluent User");
        }

        @Test
        @DisplayName("Should build with only required fields")
        void shouldBuildWithOnlyRequiredFields() {
            // When
            UserResponse response = UserResponse.builder()
                    .id("usr-minimal")
                    .build();

            // Then
            assertThat(response.getId()).isEqualTo("usr-minimal");
            assertThat(response.getName()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getPhoneNumber()).isNull();
            assertThat(response.getEmail()).isNull();
            assertThat(response.getCreatedTimestamp()).isNull();
            assertThat(response.getUpdatedTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should build empty object when no fields set")
        void shouldBuildEmptyObjectWhenNoFieldsSet() {
            // When
            UserResponse response = UserResponse.builder().build();

            // Then
            assertThat(response.getId()).isNull();
            assertThat(response.getName()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getPhoneNumber()).isNull();
            assertThat(response.getEmail()).isNull();
            assertThat(response.getCreatedTimestamp()).isNull();
            assertThat(response.getUpdatedTimestamp()).isNull();
        }
    }

    @Nested
    @DisplayName("Timestamp Handling Tests")
    class TimestampHandlingTests {

        @Test
        @DisplayName("Should handle same created and updated timestamps")
        void shouldHandleSameCreatedAndUpdatedTimestamps() {
            // Given
            LocalDateTime sameTimestamp = testTimestamp;

            // When
            UserResponse response = UserResponse.builder()
                    .createdTimestamp(sameTimestamp)
                    .updatedTimestamp(sameTimestamp)
                    .build();

            // Then
            assertThat(response.getCreatedTimestamp()).isEqualTo(sameTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(sameTimestamp);
            assertThat(response.getCreatedTimestamp()).isEqualTo(response.getUpdatedTimestamp());
        }

        @Test
        @DisplayName("Should handle updated timestamp after created timestamp")
        void shouldHandleUpdatedTimestampAfterCreated() {
            // Given
            LocalDateTime createdTimestamp = testTimestamp;
            LocalDateTime updatedTimestamp = testTimestamp.plusDays(10);

            // When
            UserResponse response = UserResponse.builder()
                    .createdTimestamp(createdTimestamp)
                    .updatedTimestamp(updatedTimestamp)
                    .build();

            // Then
            assertThat(response.getCreatedTimestamp()).isEqualTo(createdTimestamp);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(updatedTimestamp);
            assertThat(response.getUpdatedTimestamp()).isAfter(response.getCreatedTimestamp());
        }

        @Test
        @DisplayName("Should handle null timestamps")
        void shouldHandleNullTimestamps() {
            // When
            UserResponse response = UserResponse.builder()
                    .createdTimestamp(null)
                    .updatedTimestamp(null)
                    .build();

            // Then
            assertThat(response.getCreatedTimestamp()).isNull();
            assertThat(response.getUpdatedTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should handle edge case timestamps")
        void shouldHandleEdgeCaseTimestamps() {
            // Given
            LocalDateTime minDateTime = LocalDateTime.MIN;
            LocalDateTime maxDateTime = LocalDateTime.MAX;

            // When
            UserResponse response = UserResponse.builder()
                    .createdTimestamp(minDateTime)
                    .updatedTimestamp(maxDateTime)
                    .build();

            // Then
            assertThat(response.getCreatedTimestamp()).isEqualTo(minDateTime);
            assertThat(response.getUpdatedTimestamp()).isEqualTo(maxDateTime);
        }
    }

    @Nested
    @DisplayName("Address Handling Tests")
    class AddressHandlingTests {

        @Test
        @DisplayName("Should handle complete address object")
        void shouldHandleCompleteAddressObject() {
            // Given
            Address completeAddress = Address.builder()
                    .line1("123 Main Street")
                    .line2("Apartment 4B")
                    .town("London")
                    .county("Greater London")
                    .postcode("SW1A 1AA")
                    .build();

            // When
            UserResponse response = UserResponse.builder()
                    .address(completeAddress)
                    .build();

            // Then
            assertThat(response.getAddress()).isEqualTo(completeAddress);
            assertThat(response.getAddress().getLine1()).isEqualTo("123 Main Street");
            assertThat(response.getAddress().getLine2()).isEqualTo("Apartment 4B");
            assertThat(response.getAddress().getTown()).isEqualTo("London");
            assertThat(response.getAddress().getCounty()).isEqualTo("Greater London");
            assertThat(response.getAddress().getPostcode()).isEqualTo("SW1A 1AA");
        }

        @Test
        @DisplayName("Should handle minimal address object")
        void shouldHandleMinimalAddressObject() {
            // Given
            Address minimalAddress = Address.builder()
                    .line1("Simple Street")
                    .town("Town")
                    .postcode("P1 2CD")
                    .build();

            // When
            UserResponse response = UserResponse.builder()
                    .address(minimalAddress)
                    .build();

            // Then
            assertThat(response.getAddress()).isEqualTo(minimalAddress);
            assertThat(response.getAddress().getLine2()).isNull();
            assertThat(response.getAddress().getCounty()).isNull();
        }

        @Test
        @DisplayName("Should handle null address")
        void shouldHandleNullAddress() {
            // When
            UserResponse response = UserResponse.builder()
                    .address(null)
                    .build();

            // Then
            assertThat(response.getAddress()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long user ID")
        void shouldHandleVeryLongUserId() {
            // Given
            String longId = "usr-" + "a".repeat(1000);

            // When
            UserResponse response = UserResponse.builder()
                    .id(longId)
                    .build();

            // Then
            assertThat(response.getId()).hasSize(1004); // "usr-" + 1000 'a's
            assertThat(response.getId()).startsWith("usr-");
        }

        @Test
        @DisplayName("Should handle special characters in all string fields")
        void shouldHandleSpecialCharactersInAllStringFields() {
            // Given
            String specialId = "usr-123@#$%";
            String specialName = "José María O'Connor-Smith 中文";
            String specialPhone = "+1234567890";
            String specialEmail = "josé.maría@université.fr";

            // When
            UserResponse response = UserResponse.builder()
                    .id(specialId)
                    .name(specialName)
                    .phoneNumber(specialPhone)
                    .email(specialEmail)
                    .build();

            // Then
            assertThat(response.getId()).isEqualTo(specialId);
            assertThat(response.getName()).isEqualTo(specialName);
            assertThat(response.getPhoneNumber()).isEqualTo(specialPhone);
            assertThat(response.getEmail()).isEqualTo(specialEmail);
        }

        @Test
        @DisplayName("Should handle empty string fields")
        void shouldHandleEmptyStringFields() {
            // When
            UserResponse response = UserResponse.builder()
                    .id("")
                    .name("")
                    .phoneNumber("")
                    .email("")
                    .build();

            // Then
            assertThat(response.getId()).isEmpty();
            assertThat(response.getName()).isEmpty();
            assertThat(response.getPhoneNumber()).isEmpty();
            assertThat(response.getEmail()).isEmpty();
        }

        @Test
        @DisplayName("Should handle whitespace-only fields")
        void shouldHandleWhitespaceOnlyFields() {
            // Given
            String whitespace = "   \t\n   ";

            // When
            UserResponse response = UserResponse.builder()
                    .id(whitespace)
                    .name(whitespace)
                    .phoneNumber(whitespace)
                    .email(whitespace)
                    .build();

            // Then
            assertThat(response.getId()).isEqualTo(whitespace);
            assertThat(response.getName()).isEqualTo(whitespace);
            assertThat(response.getPhoneNumber()).isEqualTo(whitespace);
            assertThat(response.getEmail()).isEqualTo(whitespace);
        }

        @Test
        @DisplayName("Should handle maximum length content")
        void shouldHandleMaximumLengthContent() {
            // Given
            String maxLengthString = "A".repeat(10000);

            // When
            UserResponse response = UserResponse.builder()
                    .name(maxLengthString)
                    .email("test@" + "domain".repeat(1000) + ".com")
                    .build();

            // Then
            assertThat(response.getName()).hasSize(10000);
            assertThat(response.getEmail()).contains("@");
            assertThat(response.getEmail()).endsWith(".com");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should maintain state after creation")
        void shouldMaintainStateAfterCreation() {
            // Given
            String originalId = "usr-immutable-test";
            String originalName = "Original Name";
            LocalDateTime originalCreatedTimestamp = testTimestamp;

            UserResponse response = UserResponse.builder()
                    .id(originalId)
                    .name(originalName)
                    .createdTimestamp(originalCreatedTimestamp)
                    .build();

            // When - getting references to internal state
            String idRef = response.getId();
            String nameRef = response.getName();
            LocalDateTime timestampRef = response.getCreatedTimestamp();

            // Then - original object should be unchanged
            assertThat(response.getId()).isEqualTo(originalId);
            assertThat(response.getName()).isEqualTo(originalName);
            assertThat(response.getCreatedTimestamp()).isEqualTo(originalCreatedTimestamp);
            
            // References should equal original values
            assertThat(idRef).isEqualTo(originalId);
            assertThat(nameRef).isEqualTo(originalName);
            assertThat(timestampRef).isEqualTo(originalCreatedTimestamp);
        }

        @Test
        @DisplayName("Should handle address object immutability")
        void shouldHandleAddressObjectImmutability() {
            // Given
            Address originalAddress = Address.builder()
                    .line1("Original Line 1")
                    .town("Original Town")
                    .postcode("OR1 2IG")
                    .build();

            UserResponse response = UserResponse.builder()
                    .address(originalAddress)
                    .build();

            // When - getting reference to address
            Address addressRef = response.getAddress();

            // Then - address should maintain its values
            assertThat(response.getAddress()).isEqualTo(originalAddress);
            assertThat(addressRef).isEqualTo(originalAddress);
            assertThat(addressRef.getLine1()).isEqualTo("Original Line 1");
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("Should handle all null fields safely")
        void shouldHandleAllNullFieldsSafely() {
            // Given
            UserResponse response = new UserResponse(null, null, null, null, null, null, null);

            // When & Then - should not throw any exceptions
            assertThat(response.getId()).isNull();
            assertThat(response.getName()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getPhoneNumber()).isNull();
            assertThat(response.getEmail()).isNull();
            assertThat(response.getCreatedTimestamp()).isNull();
            assertThat(response.getUpdatedTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should handle mixed null and non-null fields")
        void shouldHandleMixedNullAndNonNullFields() {
            // When
            UserResponse response = UserResponse.builder()
                    .id("usr-mixed")
                    .name(null)
                    .address(validAddress)
                    .phoneNumber(null)
                    .email("mixed@example.com")
                    .createdTimestamp(testTimestamp)
                    .updatedTimestamp(null)
                    .build();

            // Then
            assertThat(response.getId()).isEqualTo("usr-mixed");
            assertThat(response.getName()).isNull();
            assertThat(response.getAddress()).isEqualTo(validAddress);
            assertThat(response.getPhoneNumber()).isNull();
            assertThat(response.getEmail()).isEqualTo("mixed@example.com");
            assertThat(response.getCreatedTimestamp()).isEqualTo(testTimestamp);
            assertThat(response.getUpdatedTimestamp()).isNull();
        }
    }
}
