package com.eaglebank.controller;

import com.eaglebank.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Base controller class providing common functionality for all controllers.
 * Contains shared utility methods for authentication and authorization.
 */
@Slf4j
public abstract class BaseController {

    /**
     * Extract the email from the JWT token to identify the authenticated user.
     * This method handles various JWT token formats and provides fallback mechanisms.
     * 
     * @param authentication The authentication object containing the JWT
     * @return The email from the JWT token, or null if not found
     */
    protected String getAuthenticatedEmail(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            log.debug("Authentication or principal is null");
            return null;
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Primary: Get the email claim from the JWT token
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.trim().isEmpty()) {
                log.debug("Found email in JWT: {}", LoggingUtils.maskEmail(email));
                return email;
            }
            
            // Fallback: if no email claim, try preferred_username if it looks like an email
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null && username.contains("@")) {
                log.debug("Using preferred_username as email: {}", LoggingUtils.maskEmail(username));
                return username;
            }
            
            log.debug("No valid email found in JWT claims");
        }
        
        // Final fallback to the principal name if it looks like an email
        String principalName = authentication.getName();
        if (principalName != null && principalName.contains("@")) {
            log.debug("Using principal name as email: {}", LoggingUtils.maskEmail(principalName));
            return principalName;
        }
        
        log.debug("No valid email found in authentication object");
        return null;
    }
    
    /**
     * Extract the user ID from the JWT token.
     * This is useful when the JWT contains a user ID claim.
     * 
     * @param authentication The authentication object containing the JWT
     * @return The user ID from the JWT token, or null if not found
     */
    protected String getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Try common user ID claim names
            String userId = jwt.getClaimAsString("sub"); // Subject claim
            if (userId != null && !userId.trim().isEmpty()) {
                return userId;
            }
            
            userId = jwt.getClaimAsString("user_id");
            if (userId != null && !userId.trim().isEmpty()) {
                return userId;
            }
            
            userId = jwt.getClaimAsString("id");
            if (userId != null && !userId.trim().isEmpty()) {
                return userId;
            }
        }
        
        return null;
    }
    
    /**
     * Extract the username from the JWT token.
     * This is useful for logging and audit purposes.
     * 
     * @param authentication The authentication object containing the JWT
     * @return The username from the JWT token, or null if not found
     */
    protected String getAuthenticatedUsername(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null && !username.trim().isEmpty()) {
                return username;
            }
            
            username = jwt.getClaimAsString("username");
            if (username != null && !username.trim().isEmpty()) {
                return username;
            }
        }
        
        return authentication.getName();
    }
    
    /**
     * Check if the current user is authenticated (has a valid JWT token).
     * 
     * @param authentication The authentication object
     * @return true if the user is authenticated, false otherwise
     */
    protected boolean isAuthenticated(Authentication authentication) {
        return authentication != null && 
               authentication.isAuthenticated() && 
               authentication.getPrincipal() != null;
    }
    
    /**
     * Check if the authenticated user has a specific role.
     * 
     * @param authentication The authentication object
     * @param role The role to check for
     * @return true if the user has the role, false otherwise
     */
    protected boolean hasRole(Authentication authentication, String role) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role) ||
                                     authority.getAuthority().equals(role));
    }
}
