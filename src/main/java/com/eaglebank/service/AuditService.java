package com.eaglebank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for audit logging to track security-relevant events.
 * Logs authentication attempts, authorization failures, and sensitive operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final ObjectMapper objectMapper;

    /**
     * Logs a security event with structured information.
     *
     * @param event the type of security event
     * @param userId the user ID associated with the event (if applicable)
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     * @param resource the resource being accessed
     * @param action the action being performed
     * @param result the result of the action (SUCCESS, FAILURE, etc.)
     * @param details additional details about the event
     */
    public void logSecurityEvent(SecurityEvent event, String userId, String ipAddress, 
                               String userAgent, String resource, String action, 
                               String result, Map<String, Object> details) {
        try {
            AuditLogEntry logEntry = AuditLogEntry.builder()
                    .timestamp(LocalDateTime.now())
                    .event(event)
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .resource(resource)
                    .action(action)
                    .result(result)
                    .details(details)
                    .build();

            String jsonLog = objectMapper.writeValueAsString(logEntry);
            
            // Log with appropriate level based on event type
            switch (event) {
                case AUTHENTICATION_FAILURE, AUTHORIZATION_FAILURE, RATE_LIMIT_EXCEEDED,
                     ACCOUNT_DELETION, SUSPICIOUS_ACTIVITY -> log.warn("SECURITY_AUDIT: {}", jsonLog);
                case AUTHENTICATION_SUCCESS, AUTHORIZATION_SUCCESS -> log.info("SECURITY_AUDIT: {}", jsonLog);
                default -> log.debug("SECURITY_AUDIT: {}", jsonLog);
            }
        } catch (Exception e) {
            log.error("Failed to write audit log", e);
        }
    }

    /**
     * Logs an authentication attempt.
     *
     * @param userId the user ID attempting authentication
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     * @param success whether the authentication was successful
     * @param failureReason the reason for failure (if applicable)
     */
    public void logAuthenticationAttempt(String userId, String ipAddress, String userAgent, 
                                       boolean success, String failureReason) {
        SecurityEvent event = success ? SecurityEvent.AUTHENTICATION_SUCCESS : SecurityEvent.AUTHENTICATION_FAILURE;
        String result = success ? "SUCCESS" : "FAILURE";
        
        Map<String, Object> details = Map.of(
                "failureReason", failureReason != null ? failureReason : "N/A"
        );
        
        logSecurityEvent(event, userId, ipAddress, userAgent, "authentication", "login", result, details);
    }

    /**
     * Logs an authorization failure.
     *
     * @param userId the user ID that was denied access
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     * @param resource the resource that was requested
     * @param action the action that was attempted
     * @param reason the reason for denial
     */
    public void logAuthorizationFailure(String userId, String ipAddress, String userAgent, 
                                      String resource, String action, String reason) {
        Map<String, Object> details = Map.of(
                "denialReason", reason
        );
        
        logSecurityEvent(SecurityEvent.AUTHORIZATION_FAILURE, userId, ipAddress, userAgent, 
                        resource, action, "DENIED", details);
    }

    /**
     * Logs a rate limit exceeded event.
     *
     * @param userId the user ID that exceeded the rate limit
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     * @param endpoint the endpoint that was rate limited
     * @param rateLimitType the type of rate limit that was exceeded
     */
    public void logRateLimitExceeded(String userId, String ipAddress, String userAgent, 
                                   String endpoint, String rateLimitType) {
        Map<String, Object> details = Map.of(
                "endpoint", endpoint,
                "rateLimitType", rateLimitType
        );
        
        logSecurityEvent(SecurityEvent.RATE_LIMIT_EXCEEDED, userId, ipAddress, userAgent, 
                        endpoint, "request", "RATE_LIMITED", details);
    }

    /**
     * Logs a sensitive operation.
     *
     * @param userId the user ID performing the operation
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     * @param operation the type of sensitive operation
     * @param targetResource the resource being modified
     * @param success whether the operation was successful
     */
    public void logSensitiveOperation(String userId, String ipAddress, String userAgent, 
                                    String operation, String targetResource, boolean success) {
        SecurityEvent event = switch (operation.toLowerCase()) {
            case "delete_account", "delete_user" -> SecurityEvent.ACCOUNT_DELETION;
            case "create_user", "update_user" -> SecurityEvent.USER_MANAGEMENT;
            case "create_transaction", "update_transaction" -> SecurityEvent.FINANCIAL_TRANSACTION;
            default -> SecurityEvent.SENSITIVE_OPERATION;
        };
        
        String result = success ? "SUCCESS" : "FAILURE";
        
        Map<String, Object> details = Map.of(
                "operation", operation,
                "targetResource", targetResource
        );
        
        logSecurityEvent(event, userId, ipAddress, userAgent, targetResource, operation, result, details);
    }

    /**
     * Enum defining different types of security events for audit logging.
     */
    public enum SecurityEvent {
        AUTHENTICATION_SUCCESS,
        AUTHENTICATION_FAILURE,
        AUTHORIZATION_SUCCESS,
        AUTHORIZATION_FAILURE,
        RATE_LIMIT_EXCEEDED,
        ACCOUNT_DELETION,
        USER_MANAGEMENT,
        FINANCIAL_TRANSACTION,
        SENSITIVE_OPERATION,
        SUSPICIOUS_ACTIVITY
    }

    /**
     * Data class representing an audit log entry.
     */
    public static class AuditLogEntry {
        private LocalDateTime timestamp;
        private SecurityEvent event;
        private String userId;
        private String ipAddress;
        private String userAgent;
        private String resource;
        private String action;
        private String result;
        private Map<String, Object> details;

        public static AuditLogEntryBuilder builder() {
            return new AuditLogEntryBuilder();
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public SecurityEvent getEvent() { return event; }
        public String getUserId() { return userId; }
        public String getIpAddress() { return ipAddress; }
        public String getUserAgent() { return userAgent; }
        public String getResource() { return resource; }
        public String getAction() { return action; }
        public String getResult() { return result; }
        public Map<String, Object> getDetails() { return details; }

        public static class AuditLogEntryBuilder {
            private LocalDateTime timestamp;
            private SecurityEvent event;
            private String userId;
            private String ipAddress;
            private String userAgent;
            private String resource;
            private String action;
            private String result;
            private Map<String, Object> details;

            public AuditLogEntryBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public AuditLogEntryBuilder event(SecurityEvent event) { this.event = event; return this; }
            public AuditLogEntryBuilder userId(String userId) { this.userId = userId; return this; }
            public AuditLogEntryBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
            public AuditLogEntryBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
            public AuditLogEntryBuilder resource(String resource) { this.resource = resource; return this; }
            public AuditLogEntryBuilder action(String action) { this.action = action; return this; }
            public AuditLogEntryBuilder result(String result) { this.result = result; return this; }
            public AuditLogEntryBuilder details(Map<String, Object> details) { this.details = details; return this; }

            public AuditLogEntry build() {
                AuditLogEntry entry = new AuditLogEntry();
                entry.timestamp = this.timestamp;
                entry.event = this.event;
                entry.userId = this.userId;
                entry.ipAddress = this.ipAddress;
                entry.userAgent = this.userAgent;
                entry.resource = this.resource;
                entry.action = this.action;
                entry.result = this.result;
                entry.details = this.details;
                return entry;
            }
        }
    }
}
