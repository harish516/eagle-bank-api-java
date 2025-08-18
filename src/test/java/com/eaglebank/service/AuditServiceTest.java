package com.eaglebank.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Tests")
class AuditServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    private AuditService auditService;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(objectMapper);
        
        // Setup log capture
        logger = (Logger) LoggerFactory.getLogger(AuditService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
        logger.setLevel(Level.DEBUG);
    }

    @Nested
    @DisplayName("logSecurityEvent Tests")
    class LogSecurityEventTests {

        @Test
        @DisplayName("Should log warning for security failure events")
        void shouldLogWarningForSecurityFailureEvents() throws Exception {
            // Given
            Map<String, Object> details = Map.of("reason", "invalid_credentials");
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"test\"}");

            // When
            auditService.logSecurityEvent(
                AuditService.SecurityEvent.AUTHENTICATION_FAILURE,
                "user123",
                "192.168.1.1",
                "Mozilla/5.0",
                "login",
                "authenticate",
                "FAILURE",
                details
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.WARN);
            assertThat(logEvents.get(0).getFormattedMessage()).contains("SECURITY_AUDIT:");
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }

        @Test
        @DisplayName("Should log info for successful events")
        void shouldLogInfoForSuccessfulEvents() throws Exception {
            // Given
            Map<String, Object> details = Map.of("resource", "user_profile");
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"test\"}");

            // When
            auditService.logSecurityEvent(
                AuditService.SecurityEvent.AUTHENTICATION_SUCCESS,
                "user123",
                "192.168.1.1",
                "Mozilla/5.0",
                "login",
                "authenticate",
                "SUCCESS",
                details
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.INFO);
            assertThat(logEvents.get(0).getFormattedMessage()).contains("SECURITY_AUDIT:");
        }

        @Test
        @DisplayName("Should log debug for other events")
        void shouldLogDebugForOtherEvents() throws Exception {
            // Given
            Map<String, Object> details = Map.of("operation", "view_profile");
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"test\"}");

            // When
            auditService.logSecurityEvent(
                AuditService.SecurityEvent.SENSITIVE_OPERATION,
                "user123",
                "192.168.1.1",
                "Mozilla/5.0",
                "profile",
                "view",
                "SUCCESS",
                details
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.DEBUG);
        }

        @Test
        @DisplayName("Should handle ObjectMapper exceptions gracefully")
        void shouldHandleObjectMapperExceptionsGracefully() throws Exception {
            // Given
            Map<String, Object> details = Map.of("test", "value");
            when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON serialization failed"));

            // When
            auditService.logSecurityEvent(
                AuditService.SecurityEvent.AUTHENTICATION_SUCCESS,
                "user123",
                "192.168.1.1",
                "Mozilla/5.0",
                "login",
                "authenticate",
                "SUCCESS",
                details
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.ERROR);
            assertThat(logEvents.get(0).getFormattedMessage()).contains("Failed to write audit log");
        }

        @Test
        @DisplayName("Should handle null parameters")
        void shouldHandleNullParameters() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"test\"}");

            // When
            auditService.logSecurityEvent(
                AuditService.SecurityEvent.AUTHENTICATION_SUCCESS,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

            // Then - Should not throw exception
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }
    }

    @Nested
    @DisplayName("logAuthenticationAttempt Tests")
    class LogAuthenticationAttemptTests {

        @Test
        @DisplayName("Should log successful authentication attempt")
        void shouldLogSuccessfulAuthenticationAttempt() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"auth_success\"}");

            // When
            auditService.logAuthenticationAttempt("user123", "192.168.1.1", "Mozilla/5.0", true, null);

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.INFO);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }

        @Test
        @DisplayName("Should log failed authentication attempt with reason")
        void shouldLogFailedAuthenticationAttemptWithReason() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"auth_failure\"}");

            // When
            auditService.logAuthenticationAttempt("user123", "192.168.1.1", "Mozilla/5.0", false, "invalid_password");

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.WARN);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }

        @Test
        @DisplayName("Should log failed authentication attempt without reason")
        void shouldLogFailedAuthenticationAttemptWithoutReason() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"auth_failure\"}");

            // When
            auditService.logAuthenticationAttempt("user123", "192.168.1.1", "Mozilla/5.0", false, null);

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.WARN);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }
    }

    @Nested
    @DisplayName("logAuthorizationFailure Tests")
    class LogAuthorizationFailureTests {

        @Test
        @DisplayName("Should log authorization failure with reason")
        void shouldLogAuthorizationFailureWithReason() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"auth_failure\"}");

            // When
            auditService.logAuthorizationFailure(
                "user123",
                "192.168.1.1", 
                "Mozilla/5.0",
                "/api/v1/admin/users",
                "GET",
                "insufficient_privileges"
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.WARN);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }
    }

    @Nested
    @DisplayName("logRateLimitExceeded Tests")
    class LogRateLimitExceededTests {

        @Test
        @DisplayName("Should log rate limit exceeded event")
        void shouldLogRateLimitExceededEvent() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"rate_limit\"}");

            // When
            auditService.logRateLimitExceeded(
                "user123",
                "192.168.1.1",
                "Mozilla/5.0",
                "/api/v1/users",
                "per_minute"
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.WARN);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }
    }

    @Nested
    @DisplayName("logSensitiveOperation Tests")
    class LogSensitiveOperationTests {

        @Test
        @DisplayName("Should log account deletion operation")
        void shouldLogAccountDeletionOperation() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"account_deletion\"}");

            // When
            auditService.logSensitiveOperation(
                "user123",
                "192.168.1.1",
                "Mozilla/5.0",
                "delete_account",
                "/api/v1/accounts/123",
                true
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.WARN);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }

        @Test
        @DisplayName("Should log user management operation")
        void shouldLogUserManagementOperation() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"user_management\"}");

            // When
            auditService.logSensitiveOperation(
                "admin123",
                "192.168.1.1",
                "Mozilla/5.0",
                "create_user",
                "/api/v1/users",
                true
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.DEBUG);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }

        @Test
        @DisplayName("Should log financial transaction operation")
        void shouldLogFinancialTransactionOperation() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"financial_transaction\"}");

            // When
            auditService.logSensitiveOperation(
                "user123",
                "192.168.1.1",
                "Mozilla/5.0",
                "create_transaction",
                "/api/v1/transactions",
                false
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.DEBUG);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }

        @Test
        @DisplayName("Should log generic sensitive operation")
        void shouldLogGenericSensitiveOperation() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"sensitive_operation\"}");

            // When
            auditService.logSensitiveOperation(
                "user123",
                "192.168.1.1",
                "Mozilla/5.0",
                "custom_operation",
                "/api/v1/custom",
                true
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            assertThat(logEvents.get(0).getLevel()).isEqualTo(Level.DEBUG);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }

        @Test
        @DisplayName("Should handle case insensitive operation names")
        void shouldHandleCaseInsensitiveOperationNames() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"user_management\"}");

            // When
            auditService.logSensitiveOperation(
                "admin123",
                "192.168.1.1",
                "Mozilla/5.0",
                "UPDATE_USER",
                "/api/v1/users/123",
                true
            );

            // Then
            List<ILoggingEvent> logEvents = logAppender.list;
            assertThat(logEvents).hasSize(1);
            verify(objectMapper).writeValueAsString(any(AuditService.AuditLogEntry.class));
        }
    }

    @Nested
    @DisplayName("SecurityEvent Enum Tests")
    class SecurityEventEnumTests {

        @Test
        @DisplayName("Should contain all expected security event types")
        void shouldContainAllExpectedSecurityEventTypes() {
            // When & Then
            AuditService.SecurityEvent[] events = AuditService.SecurityEvent.values();
            
            assertThat(events).containsExactlyInAnyOrder(
                AuditService.SecurityEvent.AUTHENTICATION_SUCCESS,
                AuditService.SecurityEvent.AUTHENTICATION_FAILURE,
                AuditService.SecurityEvent.AUTHORIZATION_SUCCESS,
                AuditService.SecurityEvent.AUTHORIZATION_FAILURE,
                AuditService.SecurityEvent.RATE_LIMIT_EXCEEDED,
                AuditService.SecurityEvent.ACCOUNT_DELETION,
                AuditService.SecurityEvent.USER_MANAGEMENT,
                AuditService.SecurityEvent.FINANCIAL_TRANSACTION,
                AuditService.SecurityEvent.SENSITIVE_OPERATION,
                AuditService.SecurityEvent.SUSPICIOUS_ACTIVITY
            );
        }

        @Test
        @DisplayName("Should allow valueOf for all event types")
        void shouldAllowValueOfForAllEventTypes() {
            // When & Then
            assertThat(AuditService.SecurityEvent.valueOf("AUTHENTICATION_SUCCESS"))
                .isEqualTo(AuditService.SecurityEvent.AUTHENTICATION_SUCCESS);
            assertThat(AuditService.SecurityEvent.valueOf("ACCOUNT_DELETION"))
                .isEqualTo(AuditService.SecurityEvent.ACCOUNT_DELETION);
        }
    }

    @Nested
    @DisplayName("AuditLogEntry Tests")
    class AuditLogEntryTests {

        @Test
        @DisplayName("Should create AuditLogEntry with builder")
        void shouldCreateAuditLogEntryWithBuilder() {
            // Given
            LocalDateTime timestamp = LocalDateTime.now();
            Map<String, Object> details = Map.of("key", "value");

            // When
            AuditService.AuditLogEntry entry = AuditService.AuditLogEntry.builder()
                .timestamp(timestamp)
                .event(AuditService.SecurityEvent.AUTHENTICATION_SUCCESS)
                .userId("user123")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .resource("login")
                .action("authenticate")
                .result("SUCCESS")
                .details(details)
                .build();

            // Then
            assertThat(entry.getTimestamp()).isEqualTo(timestamp);
            assertThat(entry.getEvent()).isEqualTo(AuditService.SecurityEvent.AUTHENTICATION_SUCCESS);
            assertThat(entry.getUserId()).isEqualTo("user123");
            assertThat(entry.getIpAddress()).isEqualTo("192.168.1.1");
            assertThat(entry.getUserAgent()).isEqualTo("Mozilla/5.0");
            assertThat(entry.getResource()).isEqualTo("login");
            assertThat(entry.getAction()).isEqualTo("authenticate");
            assertThat(entry.getResult()).isEqualTo("SUCCESS");
            assertThat(entry.getDetails()).isEqualTo(details);
        }

        @Test
        @DisplayName("Should create AuditLogEntry with null values")
        void shouldCreateAuditLogEntryWithNullValues() {
            // When
            AuditService.AuditLogEntry entry = AuditService.AuditLogEntry.builder()
                .timestamp(null)
                .event(null)
                .userId(null)
                .ipAddress(null)
                .userAgent(null)
                .resource(null)
                .action(null)
                .result(null)
                .details(null)
                .build();

            // Then
            assertThat(entry.getTimestamp()).isNull();
            assertThat(entry.getEvent()).isNull();
            assertThat(entry.getUserId()).isNull();
            assertThat(entry.getIpAddress()).isNull();
            assertThat(entry.getUserAgent()).isNull();
            assertThat(entry.getResource()).isNull();
            assertThat(entry.getAction()).isNull();
            assertThat(entry.getResult()).isNull();
            assertThat(entry.getDetails()).isNull();
        }

        @Test
        @DisplayName("Should support method chaining in builder")
        void shouldSupportMethodChainingInBuilder() {
            // When
            AuditService.AuditLogEntry entry = AuditService.AuditLogEntry.builder()
                .userId("user123")
                .ipAddress("192.168.1.1")
                .result("SUCCESS")
                .build();

            // Then
            assertThat(entry.getUserId()).isEqualTo("user123");
            assertThat(entry.getIpAddress()).isEqualTo("192.168.1.1");
            assertThat(entry.getResult()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("Should handle empty details map")
        void shouldHandleEmptyDetailsMap() {
            // When
            AuditService.AuditLogEntry entry = AuditService.AuditLogEntry.builder()
                .details(new HashMap<>())
                .build();

            // Then
            assertThat(entry.getDetails()).isNotNull();
            assertThat(entry.getDetails()).isEmpty();
        }
    }
}
