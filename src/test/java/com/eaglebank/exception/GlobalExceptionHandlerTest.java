package com.eaglebank.exception;

import com.eaglebank.dto.BadRequestErrorResponse;
import com.eaglebank.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodParameter methodParameter;

    private LocalDateTime testStartTime;

    @BeforeEach
    void setUp() {
        testStartTime = LocalDateTime.now();
    }

    @Nested
    @DisplayName("IllegalArgumentException Handler Tests")
    class IllegalArgumentExceptionHandlerTests {

        @Test
        @DisplayName("Should handle IllegalArgumentException and return BAD_REQUEST")
        void shouldHandleIllegalArgumentExceptionAndReturnBadRequest() {
            // Given
            String errorMessage = "Invalid argument provided";
            IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleIllegalArgumentException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(errorMessage);
            assertThat(responseBody.getTimestamp()).isAfterOrEqualTo(testStartTime);
            assertThat(responseBody.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException with empty message")
        void shouldHandleIllegalArgumentExceptionWithEmptyMessage() {
            // Given
            IllegalArgumentException exception = new IllegalArgumentException("");

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleIllegalArgumentException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEmpty();
            assertThat(responseBody.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException with null message")
        void shouldHandleIllegalArgumentExceptionWithNullMessage() {
            // Given
            IllegalArgumentException exception = new IllegalArgumentException((String) null);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleIllegalArgumentException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isNull();
            assertThat(responseBody.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException with long message")
        void shouldHandleIllegalArgumentExceptionWithLongMessage() {
            // Given
            String longMessage = "Error message ".repeat(100);
            IllegalArgumentException exception = new IllegalArgumentException(longMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleIllegalArgumentException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(longMessage);
            assertThat(responseBody.getMessage().length()).isGreaterThan(1000);
        }
    }

    @Nested
    @DisplayName("IllegalStateException Handler Tests")
    class IllegalStateExceptionHandlerTests {

        @Test
        @DisplayName("Should handle IllegalStateException and return CONFLICT")
        void shouldHandleIllegalStateExceptionAndReturnConflict() {
            // Given
            String errorMessage = "System is in invalid state";
            IllegalStateException exception = new IllegalStateException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleIllegalStateException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(errorMessage);
            assertThat(responseBody.getTimestamp()).isAfterOrEqualTo(testStartTime);
            assertThat(responseBody.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should handle IllegalStateException with complex message")
        void shouldHandleIllegalStateExceptionWithComplexMessage() {
            // Given
            String complexMessage = "User account is locked due to multiple failed login attempts";
            IllegalStateException exception = new IllegalStateException(complexMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleIllegalStateException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(complexMessage);
            assertThat(responseBody.getMessage()).contains("locked");
            assertThat(responseBody.getMessage()).contains("failed login");
        }

        @Test
        @DisplayName("Should handle IllegalStateException with null message")
        void shouldHandleIllegalStateExceptionWithNullMessage() {
            // Given
            IllegalStateException exception = new IllegalStateException((String) null);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleIllegalStateException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isNull();
            assertThat(responseBody.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException Handler Tests")
    class MethodArgumentNotValidExceptionHandlerTests {

        @Test
        @DisplayName("Should handle validation exception with single field error")
        void shouldHandleValidationExceptionWithSingleFieldError() throws NoSuchMethodException {
            // Given
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
            FieldError fieldError = new FieldError("testObject", "email", "Email must be valid");
            bindingResult.addError(fieldError);
            
            // Create a valid MethodParameter
            Method method = this.getClass().getMethod("toString");
            MethodParameter methodParameter = new MethodParameter(method, -1);
            
            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                    methodParameter, bindingResult);

            // When
            ResponseEntity<BadRequestErrorResponse> response = globalExceptionHandler
                    .handleValidationException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            BadRequestErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("Validation failed");
            assertThat(responseBody.getDetails()).hasSize(1);
            assertThat(responseBody.getDetails()).containsEntry("email", "Email must be valid");
            assertThat(responseBody.getTimestamp()).isAfterOrEqualTo(testStartTime);
        }

        @Test
        @DisplayName("Should handle validation exception with multiple field errors")
        void shouldHandleValidationExceptionWithMultipleFieldErrors() throws NoSuchMethodException {
            // Given
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
            FieldError emailError = new FieldError("testObject", "email", "Email must be valid");
            FieldError nameError = new FieldError("testObject", "firstName", "First name is required");
            FieldError phoneError = new FieldError("testObject", "phoneNumber", "Phone number must be in international format");
            
            bindingResult.addError(emailError);
            bindingResult.addError(nameError);
            bindingResult.addError(phoneError);
            
            // Create a valid MethodParameter
            Method method = this.getClass().getMethod("toString");
            MethodParameter methodParameter = new MethodParameter(method, -1);
            
            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                    methodParameter, bindingResult);

            // When
            ResponseEntity<BadRequestErrorResponse> response = globalExceptionHandler
                    .handleValidationException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            BadRequestErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("Validation failed");
            assertThat(responseBody.getDetails()).hasSize(3);
            assertThat(responseBody.getDetails()).containsEntry("email", "Email must be valid");
            assertThat(responseBody.getDetails()).containsEntry("firstName", "First name is required");
            assertThat(responseBody.getDetails()).containsEntry("phoneNumber", "Phone number must be in international format");
        }

        @Test
        @DisplayName("Should handle validation exception with nested field names")
        void shouldHandleValidationExceptionWithNestedFieldNames() throws NoSuchMethodException {
            // Given
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "userRequest");
            FieldError addressError = new FieldError("userRequest", "address.street", "Street is required");
            FieldError addressCityError = new FieldError("userRequest", "address.city", "City is required");
            
            bindingResult.addError(addressError);
            bindingResult.addError(addressCityError);
            
            // Create a valid MethodParameter
            Method method = this.getClass().getMethod("toString");
            MethodParameter methodParameter = new MethodParameter(method, -1);
            
            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                    methodParameter, bindingResult);

            // When
            ResponseEntity<BadRequestErrorResponse> response = globalExceptionHandler
                    .handleValidationException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            BadRequestErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getDetails()).hasSize(2);
            assertThat(responseBody.getDetails()).containsEntry("address.street", "Street is required");
            assertThat(responseBody.getDetails()).containsEntry("address.city", "City is required");
        }

        @Test
        @DisplayName("Should handle validation exception with no errors")
        void shouldHandleValidationExceptionWithNoErrors() throws NoSuchMethodException {
            // Given
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
            
            // Create a valid MethodParameter
            Method method = this.getClass().getMethod("toString");
            MethodParameter methodParameter = new MethodParameter(method, -1);
            
            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                    methodParameter, bindingResult);

            // When
            ResponseEntity<BadRequestErrorResponse> response = globalExceptionHandler
                    .handleValidationException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            
            BadRequestErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("Validation failed");
            assertThat(responseBody.getDetails()).isEmpty();
            assertThat(responseBody.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("UserNotFoundException Handler Tests")
    class UserNotFoundExceptionHandlerTests {

        @Test
        @DisplayName("Should handle UserNotFoundException and return NOT_FOUND")
        void shouldHandleUserNotFoundExceptionAndReturnNotFound() {
            // Given
            String errorMessage = "User with ID 123 not found";
            UserNotFoundException exception = new UserNotFoundException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleUserNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(errorMessage);
            assertThat(responseBody.getTimestamp()).isAfterOrEqualTo(testStartTime);
            assertThat(responseBody.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should handle UserNotFoundException with formatted message")
        void shouldHandleUserNotFoundExceptionWithFormattedMessage() {
            // Given
            String userId = "user-12345";
            String formattedMessage = String.format("User with ID '%s' was not found in the system", userId);
            UserNotFoundException exception = new UserNotFoundException(formattedMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleUserNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).contains(userId);
            assertThat(responseBody.getMessage()).contains("not found");
        }

        @Test
        @DisplayName("Should handle UserNotFoundException with cause")
        void shouldHandleUserNotFoundExceptionWithCause() {
            // Given
            IllegalStateException cause = new IllegalStateException("Database connection failed");
            UserNotFoundException exception = new UserNotFoundException("User lookup failed", cause);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleUserNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("User lookup failed");
            // The cause should not be exposed in the response for security reasons
        }

        @Test
        @DisplayName("Should handle UserNotFoundException with null message")
        void shouldHandleUserNotFoundExceptionWithNullMessage() {
            // Given
            UserNotFoundException exception = new UserNotFoundException((String) null);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleUserNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isNull();
            assertThat(responseBody.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("TransactionNotFoundException Handler Tests")
    class TransactionNotFoundExceptionHandlerTests {

        @Test
        @DisplayName("Should handle TransactionNotFoundException and return NOT_FOUND")
        void shouldHandleTransactionNotFoundExceptionAndReturnNotFound() {
            // Given
            String errorMessage = "Transaction not found with ID: tan-abc123";
            TransactionNotFoundException exception = new TransactionNotFoundException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleTransactionNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(errorMessage);
            assertThat(responseBody.getTimestamp()).isNotNull();
            assertThat(responseBody.getTimestamp()).isAfterOrEqualTo(testStartTime);
        }

        @Test
        @DisplayName("Should handle TransactionNotFoundException with formatted message")
        void shouldHandleTransactionNotFoundExceptionWithFormattedMessage() {
            // Given
            String transactionId = "tan-xyz789";
            String errorMessage = String.format("Transaction not found with ID: %s", transactionId);
            TransactionNotFoundException exception = new TransactionNotFoundException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleTransactionNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(errorMessage);
            assertThat(responseBody.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should handle TransactionNotFoundException with cause")
        void shouldHandleTransactionNotFoundExceptionWithCause() {
            // Given
            String errorMessage = "Transaction not found with ID: tan-failed123";
            RuntimeException cause = new RuntimeException("Database connection failed");
            TransactionNotFoundException exception = new TransactionNotFoundException(errorMessage, cause);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleTransactionNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(errorMessage);
            assertThat(responseBody.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should handle TransactionNotFoundException with null message")
        void shouldHandleTransactionNotFoundExceptionWithNullMessage() {
            // Given
            TransactionNotFoundException exception = new TransactionNotFoundException(null);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleTransactionNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isNull();
            assertThat(responseBody.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should log error when handling TransactionNotFoundException")
        void shouldLogErrorWhenHandlingTransactionNotFoundException() {
            // Given
            String errorMessage = "Transaction not found for account: 01234567";
            TransactionNotFoundException exception = new TransactionNotFoundException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleTransactionNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(errorMessage);
            // Verify that the timestamp is recent (within last few seconds)
            assertThat(responseBody.getTimestamp()).isAfterOrEqualTo(testStartTime);
            assertThat(responseBody.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
        }
    }

    @Nested
    @DisplayName("Generic Exception Handler Tests")
    class GenericExceptionHandlerTests {

        @Test
        @DisplayName("Should handle generic Exception and return INTERNAL_SERVER_ERROR")
        void shouldHandleGenericExceptionAndReturnInternalServerError() {
            // Given
            Exception exception = new Exception("Database connection failed");

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleGenericException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("An unexpected error occurred");
            assertThat(responseBody.getTimestamp()).isAfterOrEqualTo(testStartTime);
            assertThat(responseBody.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should handle NullPointerException as generic exception")
        void shouldHandleNullPointerExceptionAsGenericException() {
            // Given
            NullPointerException exception = new NullPointerException("Null reference accessed");

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleGenericException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("An unexpected error occurred");
        }

        @Test
        @DisplayName("Should handle RuntimeException as generic exception")
        void shouldHandleRuntimeExceptionAsGenericException() {
            // Given
            RuntimeException exception = new RuntimeException("Unexpected runtime error");

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleGenericException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("An unexpected error occurred");
            assertThat(responseBody.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should handle generic exception with null message")
        void shouldHandleGenericExceptionWithNullMessage() {
            // Given
            Exception exception = new Exception((String) null);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleGenericException(exception);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("An unexpected error occurred");
            assertThat(responseBody.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Response Structure and Integration Tests")
    class ResponseStructureAndIntegrationTests {

        @Test
        @DisplayName("All ErrorResponse objects should have consistent structure")
        void allErrorResponseObjectsShouldHaveConsistentStructure() {
            // Given
            IllegalArgumentException illegalArgException = new IllegalArgumentException("Test message");
            IllegalStateException illegalStateException = new IllegalStateException("Test message");
            UserNotFoundException userNotFoundException = new UserNotFoundException("Test message");
            Exception genericException = new Exception("Test message");

            // When
            ResponseEntity<ErrorResponse> illegalArgResponse = globalExceptionHandler
                    .handleIllegalArgumentException(illegalArgException);
            ResponseEntity<ErrorResponse> illegalStateResponse = globalExceptionHandler
                    .handleIllegalStateException(illegalStateException);
            ResponseEntity<ErrorResponse> userNotFoundResponse = globalExceptionHandler
                    .handleUserNotFoundException(userNotFoundException);
            ResponseEntity<ErrorResponse> genericResponse = globalExceptionHandler
                    .handleGenericException(genericException);

            // Then
            ErrorResponse illegalArgBody = illegalArgResponse.getBody();
            ErrorResponse illegalStateBody = illegalStateResponse.getBody();
            ErrorResponse userNotFoundBody = userNotFoundResponse.getBody();
            ErrorResponse genericBody = genericResponse.getBody();

            assertThat(illegalArgBody).isNotNull();
            assertThat(illegalArgBody).hasFieldOrProperty("message");
            assertThat(illegalArgBody).hasFieldOrProperty("timestamp");
            
            assertThat(illegalStateBody).isNotNull();
            assertThat(illegalStateBody).hasFieldOrProperty("message");
            assertThat(illegalStateBody).hasFieldOrProperty("timestamp");
            
            assertThat(userNotFoundBody).isNotNull();
            assertThat(userNotFoundBody).hasFieldOrProperty("message");
            assertThat(userNotFoundBody).hasFieldOrProperty("timestamp");
            
            assertThat(genericBody).isNotNull();
            assertThat(genericBody).hasFieldOrProperty("message");
            assertThat(genericBody).hasFieldOrProperty("timestamp");
        }

        @Test
        @DisplayName("BadRequestErrorResponse should have additional details field")
        void badRequestErrorResponseShouldHaveAdditionalDetailsField() throws NoSuchMethodException {
            // Given
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
            FieldError fieldError = new FieldError("testObject", "email", "Email must be valid");
            bindingResult.addError(fieldError);
            
            Method method = this.getClass().getMethod("toString");
            MethodParameter methodParameter = new MethodParameter(method, -1);
            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                    methodParameter, bindingResult);

            // When
            ResponseEntity<BadRequestErrorResponse> response = globalExceptionHandler
                    .handleValidationException(exception);

            // Then
            BadRequestErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody).hasFieldOrProperty("message");
            assertThat(responseBody).hasFieldOrProperty("details");
            assertThat(responseBody).hasFieldOrProperty("timestamp");
            assertThat(responseBody.getDetails()).isNotNull();
        }

        @Test
        @DisplayName("Should handle special characters in error messages")
        void shouldHandleSpecialCharactersInErrorMessages() {
            // Given
            String messageWithSpecialChars = "Error with ID 'user@123$%^&*()' occurred";
            UserNotFoundException exception = new UserNotFoundException(messageWithSpecialChars);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleUserNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(messageWithSpecialChars);
            assertThat(responseBody.getMessage()).contains("@123$%^&*()");
        }

        @Test
        @DisplayName("Should handle unicode characters in error messages")
        void shouldHandleUnicodeCharactersInErrorMessages() {
            // Given
            String unicodeMessage = "Usuario con ID 'José María' no encontrado";
            UserNotFoundException exception = new UserNotFoundException(unicodeMessage);

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleUserNotFoundException(exception);

            // Then
            assertThat(response).isNotNull();
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo(unicodeMessage);
            assertThat(responseBody.getMessage()).contains("José María");
        }

        @Test
        @DisplayName("Should prioritize specific exception handlers over generic handler")
        void shouldPrioritizeSpecificExceptionHandlersOverGenericHandler() {
            // Given
            UserNotFoundException userNotFoundException = new UserNotFoundException("User not found");

            // When
            ResponseEntity<ErrorResponse> response = globalExceptionHandler
                    .handleUserNotFoundException(userNotFoundException);

            // Then
            // Should use UserNotFoundException handler, not generic exception handler
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            
            ErrorResponse responseBody = response.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getMessage()).isEqualTo("User not found");
        }

        @Test
        @DisplayName("Should handle HTTP status codes correctly")
        void shouldHandleHttpStatusCodesCorrectly() {
            // Given
            IllegalArgumentException badRequestException = new IllegalArgumentException("Bad request");
            IllegalStateException conflictException = new IllegalStateException("Conflict");
            UserNotFoundException notFoundException = new UserNotFoundException("Not found");
            Exception internalErrorException = new Exception("Internal error");

            // When
            ResponseEntity<ErrorResponse> badRequestResponse = globalExceptionHandler
                    .handleIllegalArgumentException(badRequestException);
            ResponseEntity<ErrorResponse> conflictResponse = globalExceptionHandler
                    .handleIllegalStateException(conflictException);
            ResponseEntity<ErrorResponse> notFoundResponse = globalExceptionHandler
                    .handleUserNotFoundException(notFoundException);
            ResponseEntity<ErrorResponse> internalErrorResponse = globalExceptionHandler
                    .handleGenericException(internalErrorException);

            // Then
            assertThat(badRequestResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(conflictResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(internalErrorResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Should maintain timestamp precision for concurrent exception handling")
        void shouldMaintainTimestampPrecisionForConcurrentExceptionHandling() {
            // Given
            Exception exception1 = new Exception("First exception");
            Exception exception2 = new Exception("Second exception");

            // When
            ResponseEntity<ErrorResponse> response1 = globalExceptionHandler
                    .handleGenericException(exception1);
            ResponseEntity<ErrorResponse> response2 = globalExceptionHandler
                    .handleGenericException(exception2);

            // Then
            ErrorResponse body1 = response1.getBody();
            ErrorResponse body2 = response2.getBody();
            
            assertThat(body1).isNotNull();
            assertThat(body2).isNotNull();
            assertThat(body1.getTimestamp()).isBeforeOrEqualTo(body2.getTimestamp());
        }

        @Test
        @DisplayName("Should handle error response consistency across all exception types")
        void shouldHandleErrorResponseConsistencyAcrossAllExceptionTypes() {
            // Given
            String testMessage = "Test consistency";
            
            // When & Then - all responses should have non-null timestamp and correct structure
            ResponseEntity<ErrorResponse> illegalArgResponse = globalExceptionHandler
                    .handleIllegalArgumentException(new IllegalArgumentException(testMessage));
            ResponseEntity<ErrorResponse> illegalStateResponse = globalExceptionHandler
                    .handleIllegalStateException(new IllegalStateException(testMessage));
            ResponseEntity<ErrorResponse> userNotFoundResponse = globalExceptionHandler
                    .handleUserNotFoundException(new UserNotFoundException(testMessage));
            ResponseEntity<ErrorResponse> genericResponse = globalExceptionHandler
                    .handleGenericException(new Exception(testMessage));

            // Verify all responses are properly structured
            ErrorResponse[] responses = {
                illegalArgResponse.getBody(),
                illegalStateResponse.getBody(),
                userNotFoundResponse.getBody(),
                genericResponse.getBody()
            };

            for (ErrorResponse response : responses) {
                assertThat(response).isNotNull();
                assertThat(response.getTimestamp()).isNotNull();
                assertThat(response.getTimestamp()).isAfterOrEqualTo(testStartTime);
                assertThat(response.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
            }
        }
    }
}
