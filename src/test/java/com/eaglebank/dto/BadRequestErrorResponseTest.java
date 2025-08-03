package com.eaglebank.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BadRequestErrorResponse Tests")
class BadRequestErrorResponseTest {

    @Nested
    @DisplayName("Object Creation Tests")
    class ObjectCreationTests {

        @Test
        @DisplayName("Should create BadRequestErrorResponse using builder with all fields")
        void shouldCreateWithBuilder() {
            // Given
            String expectedMessage = "Validation failed";
            Map<String, String> expectedDetails = new HashMap<>();
            expectedDetails.put("email", "must be a valid email address");
            expectedDetails.put("name", "must not be blank");
            LocalDateTime expectedTimestamp = LocalDateTime.now();

            // When
            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message(expectedMessage)
                    .details(expectedDetails)
                    .timestamp(expectedTimestamp)
                    .build();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo(expectedMessage);
            assertThat(response.getDetails()).isEqualTo(expectedDetails);
            assertThat(response.getTimestamp()).isEqualTo(expectedTimestamp);
        }

        @Test
        @DisplayName("Should create BadRequestErrorResponse using all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Given
            String expectedMessage = "Request validation failed";
            Map<String, String> expectedDetails = new HashMap<>();
            expectedDetails.put("phoneNumber", "must be in international format");
            LocalDateTime expectedTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // When
            BadRequestErrorResponse response = new BadRequestErrorResponse(
                    expectedMessage,
                    expectedDetails,
                    expectedTimestamp
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo(expectedMessage);
            assertThat(response.getDetails()).isEqualTo(expectedDetails);
            assertThat(response.getTimestamp()).isEqualTo(expectedTimestamp);
        }

        @Test
        @DisplayName("Should create BadRequestErrorResponse using no-args constructor")
        void shouldCreateWithNoArgsConstructor() {
            // When
            BadRequestErrorResponse response = new BadRequestErrorResponse();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getDetails()).isNull();
            assertThat(response.getTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should create with empty details map")
        void shouldCreateWithEmptyDetails() {
            // Given
            Map<String, String> emptyDetails = new HashMap<>();

            // When
            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message("Validation failed")
                    .details(emptyDetails)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response.getDetails()).isEmpty();
        }

        @Test
        @DisplayName("Should create with null values")
        void shouldCreateWithNullValues() {
            // When
            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message(null)
                    .details(null)
                    .timestamp(null)
                    .build();

            // Then
            assertThat(response.getMessage()).isNull();
            assertThat(response.getDetails()).isNull();
            assertThat(response.getTimestamp()).isNull();
        }
    }

    @Nested
    @DisplayName("Property Access Tests")
    class PropertyAccessTests {

        @Test
        @DisplayName("Should set and get message property")
        void shouldSetAndGetMessage() {
            // Given
            BadRequestErrorResponse response = new BadRequestErrorResponse();
            String expectedMessage = "Test validation message";

            // When
            response.setMessage(expectedMessage);

            // Then
            assertThat(response.getMessage()).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("Should set and get details property")
        void shouldSetAndGetDetails() {
            // Given
            BadRequestErrorResponse response = new BadRequestErrorResponse();
            Map<String, String> expectedDetails = new HashMap<>();
            expectedDetails.put("field1", "error1");
            expectedDetails.put("field2", "error2");

            // When
            response.setDetails(expectedDetails);

            // Then
            assertThat(response.getDetails()).isEqualTo(expectedDetails);
            assertThat(response.getDetails()).containsEntry("field1", "error1");
            assertThat(response.getDetails()).containsEntry("field2", "error2");
        }

        @Test
        @DisplayName("Should set and get timestamp property")
        void shouldSetAndGetTimestamp() {
            // Given
            BadRequestErrorResponse response = new BadRequestErrorResponse();
            LocalDateTime expectedTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // When
            response.setTimestamp(expectedTimestamp);

            // Then
            assertThat(response.getTimestamp()).isEqualTo(expectedTimestamp);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            // Given
            Map<String, String> details = new HashMap<>();
            details.put("field", "error");
            LocalDateTime timestamp = LocalDateTime.now();

            BadRequestErrorResponse response1 = BadRequestErrorResponse.builder()
                    .message("Test message")
                    .details(details)
                    .timestamp(timestamp)
                    .build();

            BadRequestErrorResponse response2 = BadRequestErrorResponse.builder()
                    .message("Test message")
                    .details(details)
                    .timestamp(timestamp)
                    .build();

            // Then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when messages differ")
        void shouldNotBeEqualWhenMessagesDiffer() {
            // Given
            Map<String, String> details = new HashMap<>();
            LocalDateTime timestamp = LocalDateTime.now();

            BadRequestErrorResponse response1 = BadRequestErrorResponse.builder()
                    .message("Message 1")
                    .details(details)
                    .timestamp(timestamp)
                    .build();

            BadRequestErrorResponse response2 = BadRequestErrorResponse.builder()
                    .message("Message 2")
                    .details(details)
                    .timestamp(timestamp)
                    .build();

            // Then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal when details differ")
        void shouldNotBeEqualWhenDetailsDiffer() {
            // Given
            Map<String, String> details1 = new HashMap<>();
            details1.put("field1", "error1");

            Map<String, String> details2 = new HashMap<>();
            details2.put("field2", "error2");

            LocalDateTime timestamp = LocalDateTime.now();

            BadRequestErrorResponse response1 = BadRequestErrorResponse.builder()
                    .message("Test message")
                    .details(details1)
                    .timestamp(timestamp)
                    .build();

            BadRequestErrorResponse response2 = BadRequestErrorResponse.builder()
                    .message("Test message")
                    .details(details2)
                    .timestamp(timestamp)
                    .build();

            // Then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should handle null fields in equals")
        void shouldHandleNullFieldsInEquals() {
            // Given
            BadRequestErrorResponse response1 = new BadRequestErrorResponse(null, null, null);
            BadRequestErrorResponse response2 = new BadRequestErrorResponse(null, null, null);
            BadRequestErrorResponse response3 = new BadRequestErrorResponse("message", null, null);

            // Then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1).isNotEqualTo(response3);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString output")
        void shouldGenerateMeaningfulToString() {
            // Given
            Map<String, String> details = new HashMap<>();
            details.put("email", "invalid format");
            LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message("Validation failed")
                    .details(details)
                    .timestamp(timestamp)
                    .build();

            // When
            String toString = response.toString();

            // Then
            assertThat(toString).contains("BadRequestErrorResponse");
            assertThat(toString).contains("Validation failed");
            assertThat(toString).contains("email");
            assertThat(toString).contains("2024-01-15T10:30");
        }

        @Test
        @DisplayName("Should handle null fields in toString")
        void shouldHandleNullFieldsInToString() {
            // Given
            BadRequestErrorResponse response = new BadRequestErrorResponse(null, null, null);

            // When
            String toString = response.toString();

            // Then
            assertThat(toString).contains("BadRequestErrorResponse");
            assertThat(toString).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should support fluent builder pattern")
        void shouldSupportFluentBuilderPattern() {
            // Given & When
            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message("Test message")
                    .details(new HashMap<>())
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should build with partial fields")
        void shouldBuildWithPartialFields() {
            // When
            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message("Only message set")
                    .build();

            // Then
            assertThat(response.getMessage()).isEqualTo("Only message set");
            assertThat(response.getDetails()).isNull();
            assertThat(response.getTimestamp()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle large details map")
        void shouldHandleLargeDetailsMap() {
            // Given
            Map<String, String> largeDetails = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                largeDetails.put("field" + i, "error" + i);
            }

            // When
            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message("Many validation errors")
                    .details(largeDetails)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response.getDetails()).hasSize(100);
            assertThat(response.getDetails()).containsKey("field0");
            assertThat(response.getDetails()).containsKey("field99");
        }

        @Test
        @DisplayName("Should handle special characters in message and details")
        void shouldHandleSpecialCharacters() {
            // Given
            String messageWithSpecialChars = "Message with émojis 🚫 and symbols: @#$%^&*()";
            Map<String, String> detailsWithSpecialChars = new HashMap<>();
            detailsWithSpecialChars.put("field_with-special.chars", "Error with 特殊字符 and symbols");

            // When
            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message(messageWithSpecialChars)
                    .details(detailsWithSpecialChars)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response.getMessage()).isEqualTo(messageWithSpecialChars);
            assertThat(response.getDetails()).containsEntry("field_with-special.chars", "Error with 特殊字符 and symbols");
        }

        @Test
        @DisplayName("Should handle very long error messages")
        void shouldHandleVeryLongErrorMessages() {
            // Given
            String longMessage = "A".repeat(10000);
            String longErrorDetail = "B".repeat(5000);
            Map<String, String> details = new HashMap<>();
            details.put("longField", longErrorDetail);

            // When
            BadRequestErrorResponse response = BadRequestErrorResponse.builder()
                    .message(longMessage)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response.getMessage()).hasSize(10000);
            assertThat(response.getDetails().get("longField")).hasSize(5000);
        }
    }
}
