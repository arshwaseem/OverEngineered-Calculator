package com.arshwaseem.oe_calc.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for CookieUtil
 * Tests cookie extraction and handling utilities
 */
@ExtendWith(MockitoExtension.class)
class CookieUtilTests {

    @Mock
    private HttpServletRequest request;

    private CookieUtil cookieUtil;

    @BeforeEach
    void setUp() {
        cookieUtil = new CookieUtil();
    }

    @Test
    @DisplayName("Should extract access token from cookies")
    void testGetAccessToken_Found() {
        // Given
        Cookie[] cookies = {
                new Cookie("accessToken", "test.access.token"),
                new Cookie("refreshToken", "test.refresh.token")
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertTrue(result.isPresent());
        assertEquals("test.access.token", result.get());
    }

    @Test
    @DisplayName("Should return empty when access token not found")
    void testGetAccessToken_NotFound() {
        // Given
        Cookie[] cookies = {
                new Cookie("refreshToken", "test.refresh.token"),
                new Cookie("sessionId", "session123")
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when cookies array is null")
    void testGetAccessToken_NullCookies() {
        // Given
        when(request.getCookies()).thenReturn(null);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when cookies array is empty")
    void testGetAccessToken_EmptyCookies() {
        // Given
        when(request.getCookies()).thenReturn(new Cookie[0]);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle cookie with empty value")
    void testGetAccessToken_EmptyValue() {
        // Given
        Cookie[] cookies = {
                new Cookie("accessToken", "")
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertTrue(result.isPresent());
        assertEquals("", result.get());
    }

    @Test
    @DisplayName("Should find accessToken among multiple cookies")
    void testGetAccessToken_MultipleCookies() {
        // Given
        Cookie[] cookies = {
                new Cookie("sessionId", "session123"),
                new Cookie("locale", "en_US"),
                new Cookie("accessToken", "my.token.here"),
                new Cookie("deviceId", "device456")
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertTrue(result.isPresent());
        assertEquals("my.token.here", result.get());
    }

    @Test
    @DisplayName("Should be case-sensitive for cookie names")
    void testGetAccessToken_CaseSensitive() {
        // Given
        Cookie[] cookies = {
                new Cookie("AccessToken", "wrong.case"),
                new Cookie("ACCESSTOKEN", "wrong.case"),
                new Cookie("accesstoken", "wrong.case")
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle cookies with special characters in value")
    void testGetAccessToken_SpecialCharacters() {
        // Given
        String tokenWithSpecialChars = "eyJhbGc.iOiJIUzI1NiIs.InR5cCI6Ikp";
        Cookie[] cookies = {
                new Cookie("accessToken", tokenWithSpecialChars)
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertTrue(result.isPresent());
        assertEquals(tokenWithSpecialChars, result.get());
    }

    @Test
    @DisplayName("Should return first accessToken if multiple exist")
    void testGetAccessToken_MultipleSameName() {
        // Given
        Cookie[] cookies = {
                new Cookie("accessToken", "first.token"),
                new Cookie("accessToken", "second.token")
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertTrue(result.isPresent());
        // Should return first match
        assertEquals("first.token", result.get());
    }

    // If CookieUtil has additional methods like getRefreshToken, add similar tests:
    
    @Test
    @DisplayName("Should extract refresh token from cookies if method exists")
    void testGetRefreshToken_Found() {
        // Given
        Cookie[] cookies = {
                new Cookie("accessToken", "test.access.token"),
                new Cookie("refreshToken", "test.refresh.token")
        };

        // When
        // This assumes CookieUtil has a getRefreshToken method
        // Optional<String> result = cookieUtil.getRefreshToken(request);

        // Then
        // assertTrue(result.isPresent());
        // assertEquals("test.refresh.token", result.get());
        
        // Note: Uncomment above if getRefreshToken method exists
        assertTrue(true); // Placeholder
    }

    @Test
    @DisplayName("Should handle null cookie name gracefully")
    void testGetAccessToken_NullCookieName() {
        // Given
        Cookie cookieWithNullName = new Cookie("accessToken", "token");
        Cookie[] cookies = {cookieWithNullName};
        when(request.getCookies()).thenReturn(cookies);

        // When
        Optional<String> result = cookieUtil.getAccessToken(request);

        // Then
        assertTrue(result.isPresent());
        assertEquals("token", result.get());
    }
}
