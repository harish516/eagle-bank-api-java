package com.eaglebank.filter;

import com.eaglebank.service.AuditService;
import com.eaglebank.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Filter for applying rate limiting to API requests.
 * Integrates with audit logging to track rate limit violations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // Skip rate limiting for certain paths
        if (shouldSkipRateLimit(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = getClientKey(request);
        RateLimitService.RateLimitType rateLimitType = getRateLimitType(requestURI, request.getMethod());
        
        if (!rateLimitService.isAllowed(clientKey, rateLimitType)) {
            handleRateLimitExceeded(request, response, clientKey, rateLimitType);
            return;
        }
        
        // Add rate limit headers to response
        addRateLimitHeaders(response, clientKey, rateLimitType);
        
        filterChain.doFilter(request, response);
    }

    /**
     * Determines if rate limiting should be skipped for the given URI.
     */
    private boolean shouldSkipRateLimit(String requestURI) {
        return requestURI.startsWith("/actuator/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/api-docs/") ||
               requestURI.startsWith("/v3/api-docs/") ||
               requestURI.equals("/h2-console") ||
               requestURI.startsWith("/h2-console/");
    }

    /**
     * Gets the client key for rate limiting (user ID or IP address).
     */
    private String getClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof Jwt jwt) {
            // Use user ID from JWT if available
            String userId = jwt.getClaimAsString("sub");
            if (userId != null) {
                return "user:" + userId;
            }
        }
        
        // Fall back to IP address
        String ipAddress = getClientIpAddress(request);
        return "ip:" + ipAddress;
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

    /**
     * Determines the appropriate rate limit type based on the request.
     */
    private RateLimitService.RateLimitType getRateLimitType(String requestURI, String method) {
        // Strict rate limiting for sensitive operations
        if ((requestURI.startsWith("/api/v1/users") && ("POST".equals(method) || "DELETE".equals(method))) ||
            (requestURI.contains("/accounts/") && "DELETE".equals(method)) ||
            requestURI.contains("/transactions") && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return RateLimitService.RateLimitType.STRICT;
        }
        
        // Relaxed rate limiting for read operations
        if ("GET".equals(method)) {
            return RateLimitService.RateLimitType.RELAXED;
        }
        
        // Default rate limiting for other operations
        return RateLimitService.RateLimitType.DEFAULT;
    }

    /**
     * Handles rate limit exceeded scenario.
     */
    private void handleRateLimitExceeded(HttpServletRequest request, HttpServletResponse response, 
                                       String clientKey, RateLimitService.RateLimitType rateLimitType) 
            throws IOException {
        
        String userId = extractUserIdFromKey(clientKey);
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Log the rate limit violation
        auditService.logRateLimitExceeded(userId, ipAddress, userAgent, 
                                        request.getRequestURI(), rateLimitType.name());
        
        // Set response status and headers
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60"); // Suggest retry after 60 seconds
        
        // Create error response
        Map<String, Object> errorResponse = Map.of(
                "error", "Too Many Requests",
                "message", "Rate limit exceeded. Please try again later.",
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "path", request.getRequestURI(),
                "rateLimitType", rateLimitType.name()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Adds rate limit information to response headers.
     */
    private void addRateLimitHeaders(HttpServletResponse response, String clientKey, 
                                   RateLimitService.RateLimitType rateLimitType) {
        try {
            long availableTokens = rateLimitService.getAvailableTokens(clientKey, rateLimitType);
            response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
            response.setHeader("X-RateLimit-Type", rateLimitType.name());
        } catch (Exception e) {
            log.debug("Failed to add rate limit headers", e);
        }
    }

    /**
     * Extracts user ID from client key.
     */
    private String extractUserIdFromKey(String clientKey) {
        if (clientKey.startsWith("user:")) {
            return clientKey.substring(5);
        }
        return null; // IP-based rate limiting
    }
}
