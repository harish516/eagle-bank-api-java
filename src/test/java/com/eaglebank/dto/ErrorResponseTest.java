package com.eaglebank.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    @Nested
    @DisplayName("Object Creation Tests")
    class ObjectCreationTests {

        @Test
        @DisplayName("Should create ErrorResponse using builder with all fields")
        void shouldCreateWithBuilder() {
            // Given
            String expectedMessage = "Internal server error";
            LocalDateTime expectedTimestamp = LocalDateTime.now();

            // When
            ErrorResponse response = ErrorResponse.builder()
                    .message(expectedMessage)
                    .timestamp(expectedTimestamp)
                    .build();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo(expectedMessage);
            assertThat(response.getTimestamp()).isEqualTo(expectedTimestamp);
        }

        @Test
        @DisplayName("Should create ErrorResponse using all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Given
            String expectedMessage = "Resource not found";
            LocalDateTime expectedTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // When
            ErrorResponse response = new ErrorResponse(expectedMessage, expectedTimestamp);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo(expectedMessage);
            assertThat(response.getTimestamp()).isEqualTo(expectedTimestamp);
        }

        @Test
        @DisplayName("Should create ErrorResponse using no-args constructor")
        void shouldCreateWithNoArgsConstructor() {
            // When
            ErrorResponse response = new ErrorResponse();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should create with null values")
        void shouldCreateWithNullValues() {
            // When
            ErrorResponse response = ErrorResponse.builder()
                    .message(null)
                    .timestamp(null)
                    .build();

            // Then
            assertThat(response.getMessage()).isNull();
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
            ErrorResponse response = new ErrorResponse();
            String expectedMessage = "Test error message";

            // When
            response.setMessage(expectedMessage);

            // Then
            assertThat(response.getMessage()).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("Should set and get timestamp property")
        void shouldSetAndGetTimestamp() {
            // Given
            ErrorResponse response = new ErrorResponse();
            LocalDateTime expectedTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // When
            response.setTimestamp(expectedTimestamp);

            // Then
            assertThat(response.getTimestamp()).isEqualTo(expectedTimestamp);
        }

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            // Given
            ErrorResponse response = new ErrorResponse();

            // When
            response.setMessage(null);

            // Then
            assertThat(response.getMessage()).isNull();
        }

        @Test
        @DisplayName("Should handle null timestamp")
        void shouldHandleNullTimestamp() {
            // Given
            ErrorResponse response = new ErrorResponse();

            // When
            response.setTimestamp(null);

            // Then
            assertThat(response.getTimestamp()).isNull();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            // Given
            LocalDateTime timestamp = LocalDateTime.now();

            ErrorResponse response1 = ErrorResponse.builder()
                    .message("Test message")
                    .timestamp(timestamp)
                    .build();

            ErrorResponse response2 = ErrorResponse.builder()
                    .message("Test message")
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
            LocalDateTime timestamp = LocalDateTime.now();

            ErrorResponse response1 = ErrorResponse.builder()
                    .message("Message 1")
                    .timestamp(timestamp)
                    .build();

            ErrorResponse response2 = ErrorResponse.builder()
                    .message("Message 2")
                    .timestamp(timestamp)
                    .build();

            // Then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal when timestamps differ")
        void shouldNotBeEqualWhenTimestampsDiffer() {
            // Given
            LocalDateTime timestamp1 = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            LocalDateTime timestamp2 = LocalDateTime.of(2024, 1, 15, 11, 30, 0);

            ErrorResponse response1 = ErrorResponse.builder()
                    .message("Test message")
                    .timestamp(timestamp1)
                    .build();

            ErrorResponse response2 = ErrorResponse.builder()
                    .message("Test message")
                    .timestamp(timestamp2)
                    .build();

            // Then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should handle null fields in equals")
        void shouldHandleNullFieldsInEquals() {
            // Given
            ErrorResponse response1 = new ErrorResponse(null, null);
            ErrorResponse response2 = new ErrorResponse(null, null);
            ErrorResponse response3 = new ErrorResponse("message", null);

            // Then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1).isNotEqualTo(response3);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            ErrorResponse response = ErrorResponse.builder()
                    .message("Test")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            ErrorResponse response = ErrorResponse.builder()
                    .message("Test")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response).isNotEqualTo("Different class");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString output")
        void shouldGenerateMeaningfulToString() {
            // Given
            LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            ErrorResponse response = ErrorResponse.builder()
                    .message("Test error occurred")
                    .timestamp(timestamp)
                    .build();

            // When
            String toString = response.toString();

            // Then
            assertThat(toString).contains("ErrorResponse");
            assertThat(toString).contains("Test error occurred");
            assertThat(toString).contains("2024-01-15T10:30");
        }

        @Test
        @DisplayName("Should handle null fields in toString")
        void shouldHandleNullFieldsInToString() {
            // Given
            ErrorResponse response = new ErrorResponse(null, null);

            // When
            String toString = response.toString();

            // Then
            assertThat(toString).contains("ErrorResponse");
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
            ErrorResponse response = ErrorResponse.builder()
                    .message("Test message")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should build with partial fields")
        void shouldBuildWithPartialFields() {
            // When
            ErrorResponse response = ErrorResponse.builder()
                    .message("Only message set")
                    .build();

            // Then
            assertThat(response.getMessage()).isEqualTo("Only message set");
            assertThat(response.getTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should build with only timestamp")
        void shouldBuildWithOnlyTimestamp() {
            // Given
            LocalDateTime timestamp = LocalDateTime.now();

            // When
            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(timestamp)
                    .build();

            // Then
            assertThat(response.getMessage()).isNull();
            assertThat(response.getTimestamp()).isEqualTo(timestamp);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessage() {
            // When
            ErrorResponse response = ErrorResponse.builder()
                    .message("")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response.getMessage()).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long error message")
        void shouldHandleVeryLongErrorMessage() {
            // Given
            String longMessage = "A".repeat(10000);

            // When
            ErrorResponse response = ErrorResponse.builder()
                    .message(longMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response.getMessage()).hasSize(10000);
            assertThat(response.getMessage()).startsWith("AAAA");
        }

        @Test
        @DisplayName("Should handle special characters in message")
        void shouldHandleSpecialCharactersInMessage() {
            // Given
            String messageWithSpecialChars = "Error with émojis 🚫 and symbols: @#$%^&*()";

            // When
            ErrorResponse response = ErrorResponse.builder()
                    .message(messageWithSpecialChars)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response.getMessage()).isEqualTo(messageWithSpecialChars);
        }

        @Test
        @DisplayName("Should handle edge case timestamps")
        void shouldHandleEdgeCaseTimestamps() {
            // Given
            LocalDateTime minDateTime = LocalDateTime.MIN;
            LocalDateTime maxDateTime = LocalDateTime.MAX;

            // When
            ErrorResponse responseMin = ErrorResponse.builder()
                    .message("Min timestamp")
                    .timestamp(minDateTime)
                    .build();

            ErrorResponse responseMax = ErrorResponse.builder()
                    .message("Max timestamp")
                    .timestamp(maxDateTime)
                    .build();

            // Then
            assertThat(responseMin.getTimestamp()).isEqualTo(minDateTime);
            assertThat(responseMax.getTimestamp()).isEqualTo(maxDateTime);
        }

        @Test
        @DisplayName("Should handle whitespace-only message")
        void shouldHandleWhitespaceOnlyMessage() {
            // Given
            String whitespaceMessage = "   \t\n\r   ";

            // When
            ErrorResponse response = ErrorResponse.builder()
                    .message(whitespaceMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Then
            assertThat(response.getMessage()).isEqualTo(whitespaceMessage);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should maintain state after creation")
        void shouldMaintainStateAfterCreation() {
            // Given
            String originalMessage = "Original message";
            LocalDateTime originalTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            ErrorResponse response = ErrorResponse.builder()
                    .message(originalMessage)
                    .timestamp(originalTimestamp)
                    .build();

            // When - attempting to modify external references shouldn't affect the object
            String messageRef = response.getMessage();
            LocalDateTime timestampRef = response.getTimestamp();

            // Then
            assertThat(response.getMessage()).isEqualTo(originalMessage);
            assertThat(response.getTimestamp()).isEqualTo(originalTimestamp);
            assertThat(messageRef).isEqualTo(originalMessage);
            assertThat(timestampRef).isEqualTo(originalTimestamp);
        }
    }
}
