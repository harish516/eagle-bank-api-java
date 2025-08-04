package com.eaglebank.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility class for secure logging operations.
 * Provides methods to safely log sensitive information without exposing PII.
 */
public class LoggingUtils {
    
    private static final String MASKED_EMAIL_PATTERN = "%s***@%s";
    private static final int EMAIL_PREFIX_LENGTH = 2;
    
    /**
     * Masks an email address for safe logging.
     * Shows first 2 characters of local part and full domain.
     * 
     * @param email the email to mask
     * @return masked email (e.g., "jo***@example.com") or "[INVALID_EMAIL]" if null/invalid
     */
    public static String maskEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "[NULL_EMAIL]";
        }
        
        email = email.trim();
        int atIndex = email.indexOf('@');
        
        if (atIndex <= 0 || atIndex >= email.length() - 1) {
            return "[INVALID_EMAIL]";
        }
        
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        
        if (localPart.length() <= EMAIL_PREFIX_LENGTH) {
            return String.format("%s***@%s", localPart.charAt(0), domain);
        }
        
        return String.format(MASKED_EMAIL_PATTERN, 
                localPart.substring(0, EMAIL_PREFIX_LENGTH), domain);
    }
    
    /**
     * Creates a hash of the email for consistent tracking without exposing PII.
     * Useful for correlation and debugging without revealing actual email.
     * 
     * @param email the email to hash
     * @return SHA-256 hash of the email (first 8 characters) or "[HASH_ERROR]" if hashing fails
     */
    public static String hashEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "[NULL_EMAIL_HASH]";
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.trim().toLowerCase().getBytes());
            // Return first 8 characters of hex for brevity while maintaining uniqueness
            return HexFormat.of().formatHex(hash).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            return "[HASH_ERROR]";
        }
    }
    
    /**
     * Masks a user ID for logging (keeps prefix and suffix, masks middle).
     * 
     * @param userId the user ID to mask
     * @return masked user ID or "[NULL_USER_ID]" if null
     */
    public static String maskUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "[NULL_USER_ID]";
        }
        
        userId = userId.trim();
        if (userId.length() <= 8) {
            return userId.substring(0, 3) + "***";
        }
        
        return userId.substring(0, 4) + "***" + userId.substring(userId.length() - 4);
    }
    
    /**
     * Masks a phone number for safe logging.
     * 
     * @param phoneNumber the phone number to mask
     * @return masked phone number or "[NULL_PHONE]" if null
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "[NULL_PHONE]";
        }
        
        phoneNumber = phoneNumber.trim();
        if (phoneNumber.length() <= 4) {
            return "***" + phoneNumber.substring(phoneNumber.length() - 1);
        }
        
        return phoneNumber.substring(0, 2) + "***" + phoneNumber.substring(phoneNumber.length() - 2);
    }
    
    /**
     * Creates a safe logging identifier combining masked email and hash.
     * 
     * @param email the email address
     * @return safe logging identifier
     */
    public static String safeEmailId(String email) {
        return String.format("%s (hash:%s)", maskEmail(email), hashEmail(email));
    }
}
