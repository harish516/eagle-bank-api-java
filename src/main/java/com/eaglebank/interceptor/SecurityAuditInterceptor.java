package com.eaglebank.interceptor;

import com.eaglebank.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for logging security-relevant requests and responses.
 * Integrates with the audit service to track user activities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                           @NonNull Object handler) {
        
        // Skip audit logging for non-sensitive endpoints
        if (shouldSkipAuditLogging(request.getRequestURI())) {
            return true;
        }

        String userId = getUserId();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Log authorization success for authenticated requests
        if (userId != null) {
            auditService.logSecurityEvent(
                AuditService.SecurityEvent.AUTHORIZATION_SUCCESS,
                userId,
                ipAddress,
                userAgent,
                uri,
                method,
                "ALLOWED",
                null
            );
        }

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                              @NonNull Object handler, @Nullable Exception ex) {
        
        // Skip audit logging for non-sensitive endpoints
        if (shouldSkipAuditLogging(request.getRequestURI())) {
            return;
        }

        String userId = getUserId();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Log sensitive operations based on the endpoint and method
        if (isSensitiveOperation(uri, method)) {
            boolean success = response.getStatus() < 400;
            String operation = determineOperation(uri, method);
            
            auditService.logSensitiveOperation(userId, ipAddress, userAgent, operation, uri, success);
        }
    }

    /**
     * Determines if audit logging should be skipped for the given URI.
     */
    private boolean shouldSkipAuditLogging(String requestURI) {
        return requestURI.startsWith("/actuator/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/api-docs/") ||
               requestURI.startsWith("/v3/api-docs/") ||
               requestURI.equals("/h2-console") ||
               requestURI.startsWith("/h2-console/") ||
               requestURI.endsWith(".css") ||
               requestURI.endsWith(".js") ||
               requestURI.endsWith(".ico");
    }

    /**
     * Determines if the request is for a sensitive operation that should be audited.
     */
    private boolean isSensitiveOperation(String uri, String method) {
        // User management operations
        if (uri.startsWith("/api/v1/users")) {
            return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method);
        }
        
        // Account operations
        if (uri.contains("/accounts")) {
            return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method);
        }
        
        // Transaction operations
        if (uri.contains("/transactions")) {
            return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method);
        }
        
        return false;
    }

    /**
     * Determines the operation type based on URI and method.
     */
    private String determineOperation(String uri, String method) {
        if (uri.startsWith("/api/v1/users")) {
            return switch (method) {
                case "POST" -> "create_user";
                case "PUT", "PATCH" -> "update_user";
                case "DELETE" -> "delete_user";
                default -> "user_operation";
            };
        }
        
        if (uri.contains("/accounts")) {
            return switch (method) {
                case "POST" -> "create_account";
                case "PUT", "PATCH" -> "update_account";
                case "DELETE" -> "delete_account";
                default -> "account_operation";
            };
        }
        
        if (uri.contains("/transactions")) {
            return switch (method) {
                case "POST" -> "create_transaction";
                case "PUT", "PATCH" -> "update_transaction";
                case "DELETE" -> "delete_transaction";
                default -> "transaction_operation";
            };
        }
        
        return "unknown_operation";
    }

    /**
     * Gets the current user ID from the security context.
     */
    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("sub");
        }
        
        return null;
    }

    /**
     * Gets the client IP address, handling proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
