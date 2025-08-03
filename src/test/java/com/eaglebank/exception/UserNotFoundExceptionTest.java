package com.eaglebank.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserNotFoundException Tests")
class UserNotFoundExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            // Given
            String message = "User with ID 123 not found";

            // When
            UserNotFoundException exception = new UserNotFoundException(message);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("Should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            // Given
            String message = null;

            // When
            UserNotFoundException exception = new UserNotFoundException(message);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("Should create exception with empty message")
        void shouldCreateExceptionWithEmptyMessage() {
            // Given
            String message = "";

            // When
            UserNotFoundException exception = new UserNotFoundException(message);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEmpty();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            // Given
            String message = "User lookup failed";
            Throwable cause = new RuntimeException("Database connection error");

            // When
            UserNotFoundException exception = new UserNotFoundException(message, cause);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create exception with null message and cause")
        void shouldCreateExceptionWithNullMessageAndCause() {
            // Given
            String message = null;
            Throwable cause = new IllegalArgumentException("Invalid user ID");

            // When
            UserNotFoundException exception = new UserNotFoundException(message, cause);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create exception with message and null cause")
        void shouldCreateExceptionWithMessageAndNullCause() {
            // Given
            String message = "User not found in database";
            Throwable cause = null;

            // When
            UserNotFoundException exception = new UserNotFoundException(message, cause);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should extend RuntimeException")
        void shouldExtendRuntimeException() {
            // Given
            UserNotFoundException exception = new UserNotFoundException("Test message");

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception).isInstanceOf(Exception.class);
            assertThat(exception).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("Should be unchecked exception")
        void shouldBeUncheckedException() {
            // Given & When
            UserNotFoundException exception = new UserNotFoundException("Test message");

            // Then
            // Since it extends RuntimeException, it's an unchecked exception
            // This test verifies the inheritance hierarchy
            assertThat(RuntimeException.class.isAssignableFrom(UserNotFoundException.class))
                    .isTrue();
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Exception Behavior Tests")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("Should preserve stack trace information")
        void shouldPreserveStackTraceInformation() {
            // Given & When
            UserNotFoundException exception = new UserNotFoundException("User not found");

            // Then
            assertThat(exception.getStackTrace()).isNotNull();
            assertThat(exception.getStackTrace()).isNotEmpty();
            assertThat(exception.getStackTrace()[0].getClassName())
                    .contains("UserNotFoundExceptionTest");
        }

        @Test
        @DisplayName("Should preserve cause stack trace")
        void shouldPreserveCauseStackTrace() {
            // Given
            RuntimeException cause = new RuntimeException("Database error");
            UserNotFoundException exception = new UserNotFoundException("User lookup failed", cause);

            // When & Then
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getCause().getStackTrace()).isNotNull();
            assertThat(exception.getCause().getStackTrace()).isNotEmpty();
        }

        @Test
        @DisplayName("Should allow message formatting")
        void shouldAllowMessageFormatting() {
            // Given
            Long userId = 12345L;
            String formattedMessage = String.format("User with ID %d not found", userId);

            // When
            UserNotFoundException exception = new UserNotFoundException(formattedMessage);

            // Then
            assertThat(exception.getMessage()).isEqualTo("User with ID 12345 not found");
        }
    }

    @Nested
    @DisplayName("Real-World Usage Tests")
    class RealWorldUsageTests {

        @Test
        @DisplayName("Should work with typical user ID not found scenario")
        void shouldWorkWithTypicalUserIdNotFoundScenario() {
            // Given
            String userId = "user-123";
            String message = "User with ID '" + userId + "' not found";

            // When
            UserNotFoundException exception = new UserNotFoundException(message);

            // Then
            assertThat(exception.getMessage()).contains(userId);
            assertThat(exception.getMessage()).contains("not found");
        }

        @Test
        @DisplayName("Should work with email not found scenario")
        void shouldWorkWithEmailNotFoundScenario() {
            // Given
            String email = "john.doe@example.com";
            String message = "User with email '" + email + "' not found";

            // When
            UserNotFoundException exception = new UserNotFoundException(message);

            // Then
            assertThat(exception.getMessage()).contains(email);
            assertThat(exception.getMessage()).contains("not found");
        }

        @Test
        @DisplayName("Should work with repository exception as cause")
        void shouldWorkWithRepositoryExceptionAsCause() {
            // Given
            String message = "Failed to find user";
            Exception repositoryException = new RuntimeException("Connection timeout");

            // When
            UserNotFoundException exception = new UserNotFoundException(message, repositoryException);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(exception.getCause().getMessage()).contains("Connection timeout");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long message")
        void shouldHandleVeryLongMessage() {
            // Given
            String longMessage = "User not found ".repeat(100);

            // When
            UserNotFoundException exception = new UserNotFoundException(longMessage);

            // Then
            assertThat(exception.getMessage()).isEqualTo(longMessage);
            assertThat(exception.getMessage().length()).isGreaterThan(1000);
        }

        @Test
        @DisplayName("Should handle special characters in message")
        void shouldHandleSpecialCharactersInMessage() {
            // Given
            String messageWithSpecialChars = "User with ID 'user@123$%^&*()' not found";

            // When
            UserNotFoundException exception = new UserNotFoundException(messageWithSpecialChars);

            // Then
            assertThat(exception.getMessage()).isEqualTo(messageWithSpecialChars);
            assertThat(exception.getMessage()).contains("@123$%^&*()");
        }

        @Test
        @DisplayName("Should handle unicode characters in message")
        void shouldHandleUnicodeCharactersInMessage() {
            // Given
            String unicodeMessage = "Usuario con ID 'José María' no encontrado";

            // When
            UserNotFoundException exception = new UserNotFoundException(unicodeMessage);

            // Then
            assertThat(exception.getMessage()).isEqualTo(unicodeMessage);
            assertThat(exception.getMessage()).contains("José María");
        }

        @Test
        @DisplayName("Should handle nested cause exceptions")
        void shouldHandleNestedCauseExceptions() {
            // Given
            Exception rootCause = new IllegalStateException("Database corrupted");
            Exception intermediateCause = new RuntimeException("Repository error", rootCause);
            String message = "User lookup failed";

            // When
            UserNotFoundException exception = new UserNotFoundException(message, intermediateCause);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(intermediateCause);
            assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
        }
    }
}
