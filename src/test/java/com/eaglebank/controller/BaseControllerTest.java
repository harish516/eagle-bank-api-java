package com.eaglebank.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    private TestableBaseController controller;

    @BeforeEach
    void setUp() {
        controller = new TestableBaseController();
    }

    // Test class to expose protected methods for testing
    private static class TestableBaseController extends BaseController {
        public String testGetAuthenticatedEmail(Authentication authentication) {
            return getAuthenticatedEmail(authentication);
        }

        public String testGetAuthenticatedUserId(Authentication authentication) {
            return getAuthenticatedUserId(authentication);
        }

        public String testGetAuthenticatedUsername(Authentication authentication) {
            return getAuthenticatedUsername(authentication);
        }

        public boolean testIsAuthenticated(Authentication authentication) {
            return isAuthenticated(authentication);
        }

        public boolean testHasRole(Authentication authentication, String role) {
            return hasRole(authentication, role);
        }
    }

    @Test
    void shouldExtractEmailFromJwtEmailClaim() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");

        // When
        String result = controller.testGetAuthenticatedEmail(authentication);

        // Then
        assertThat(result).isEqualTo("test@example.com");
    }

    @Test
    void shouldFallbackToPreferredUsernameWhenEmailNotPresent() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("email")).thenReturn(null);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser@example.com");

        // When
        String result = controller.testGetAuthenticatedEmail(authentication);

        // Then
        assertThat(result).isEqualTo("testuser@example.com");
    }

    @Test
    void shouldFallbackToPrincipalNameWhenJwtClaimsNotAvailable() {
        // Given
        when(authentication.getPrincipal()).thenReturn("not-a-jwt");
        when(authentication.getName()).thenReturn("user@example.com");

        // When
        String result = controller.testGetAuthenticatedEmail(authentication);

        // Then
        assertThat(result).isEqualTo("user@example.com");
    }

    @Test
    void shouldReturnNullWhenNoValidEmailFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("email")).thenReturn(null);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser"); // no @ symbol
        when(authentication.getName()).thenReturn("testuser"); // no @ symbol

        // When
        String result = controller.testGetAuthenticatedEmail(authentication);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenAuthenticationIsNull() {
        // When
        String result = controller.testGetAuthenticatedEmail(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldExtractUserIdFromSubjectClaim() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("usr-123456");

        // When
        String result = controller.testGetAuthenticatedUserId(authentication);

        // Then
        assertThat(result).isEqualTo("usr-123456");
    }

    @Test
    void shouldFallbackToUserIdClaim() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn(null);
        when(jwt.getClaimAsString("user_id")).thenReturn("usr-789");

        // When
        String result = controller.testGetAuthenticatedUserId(authentication);

        // Then
        assertThat(result).isEqualTo("usr-789");
    }

    @Test
    void shouldExtractUsernameFromPreferredUsernameClaim() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");

        // When
        String result = controller.testGetAuthenticatedUsername(authentication);

        // Then
        assertThat(result).isEqualTo("testuser");
    }

    @Test
    void shouldFallbackToAuthenticationNameForUsername() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("preferred_username")).thenReturn(null);
        when(jwt.getClaimAsString("username")).thenReturn(null);
        when(authentication.getName()).thenReturn("fallback-user");

        // When
        String result = controller.testGetAuthenticatedUsername(authentication);

        // Then
        assertThat(result).isEqualTo("fallback-user");
    }

    @Test
    void shouldReturnTrueWhenAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);

        // When
        boolean result = controller.testIsAuthenticated(authentication);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        boolean result = controller.testIsAuthenticated(authentication);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAuthenticationIsNull() {
        // When
        boolean result = controller.testIsAuthenticated(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnTrueWhenUserHasRole() {
        // Given
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // When
        boolean hasUserRole = controller.testHasRole(authentication, "USER");
        boolean hasAdminRole = controller.testHasRole(authentication, "ADMIN");

        // Then
        assertThat(hasUserRole).isTrue();
        assertThat(hasAdminRole).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnFalseWhenUserDoesNotHaveRole() {
        // Given
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // When
        boolean hasAdminRole = controller.testHasRole(authentication, "ADMIN");

        // Then
        assertThat(hasAdminRole).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleRoleWithoutRolePrefix() {
        // Given
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("USER")
        );
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // When
        boolean hasUserRole = controller.testHasRole(authentication, "USER");

        // Then
        assertThat(hasUserRole).isTrue();
    }

    @Test
    void shouldIgnoreEmptyEmailClaim() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("email")).thenReturn("   "); // blank email
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser@example.com");

        // When
        String result = controller.testGetAuthenticatedEmail(authentication);

        // Then
        assertThat(result).isEqualTo("testuser@example.com");
    }

    @Test
    void shouldIgnorePreferredUsernameWithoutAtSymbol() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("email")).thenReturn(null);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser"); // no @ symbol
        when(authentication.getName()).thenReturn("fallback@example.com");

        // When
        String result = controller.testGetAuthenticatedEmail(authentication);

        // Then
        assertThat(result).isEqualTo("fallback@example.com");
    }
}
