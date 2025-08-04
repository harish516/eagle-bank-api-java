package com.eaglebank.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoggingUtilsTest {

    @Test
    void maskEmail_shouldMaskNormalEmail() {
        String result = LoggingUtils.maskEmail("john.doe@example.com");
        assertEquals("jo***@example.com", result);
    }

    @Test
    void maskEmail_shouldMaskShortEmail() {
        String result = LoggingUtils.maskEmail("a@example.com");
        assertEquals("a***@example.com", result);
    }

    @Test
    void maskEmail_shouldHandleNullEmail() {
        String result = LoggingUtils.maskEmail(null);
        assertEquals("[NULL_EMAIL]", result);
    }

    @Test
    void maskEmail_shouldHandleInvalidEmail() {
        String result = LoggingUtils.maskEmail("invalid-email");
        assertEquals("[INVALID_EMAIL]", result);
    }

    @Test
    void hashEmail_shouldGenerateConsistentHash() {
        String email = "test@example.com";
        String hash1 = LoggingUtils.hashEmail(email);
        String hash2 = LoggingUtils.hashEmail(email);
        
        assertEquals(hash1, hash2);
        assertEquals(8, hash1.length());
        assertNotEquals(email, hash1);
    }

    @Test
    void maskUserId_shouldMaskLongUserId() {
        String result = LoggingUtils.maskUserId("usr-12345678901234567890");
        assertEquals("usr-***7890", result);
    }

    @Test
    void maskUserId_shouldMaskShortUserId() {
        String result = LoggingUtils.maskUserId("usr-123");
        assertEquals("usr***", result);
    }

    @Test
    void maskPhoneNumber_shouldMaskPhone() {
        String result = LoggingUtils.maskPhoneNumber("+44 1234 567890");
        assertEquals("+4***90", result);
    }

    @Test
    void safeEmailId_shouldCombineMaskAndHash() {
        String result = LoggingUtils.safeEmailId("test@example.com");
        assertTrue(result.contains("te***@example.com"));
        assertTrue(result.contains("hash:"));
        assertTrue(result.split("hash:")[1].trim().replace(")", "").length() == 8);
    }
}
