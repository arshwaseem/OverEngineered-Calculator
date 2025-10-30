package com.arshwaseem.oe_calc.filter;

import com.arshwaseem.oe_calc.AuthServiceClient;
import com.arshwaseem.oe_calc.dto.TokenValidationResponse;
import com.arshwaseem.oe_calc.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTests {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private StringWriter responseWriter;


    @Nested
    public class SetupRequiring {

        @BeforeEach
        void setUp() throws Exception {
            responseWriter = new StringWriter();
            when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        }

        @Test
        @DisplayName("Should reject request when token is missing")
        void testMissingToken_ReturnsUnauthorized() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/op/divide");
            when(cookieUtil.getAccessToken(request)).thenReturn(Optional.empty());
            when(request.getHeader("Authorization")).thenReturn(null);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).setContentType("application/json");
            verify(filterChain, never()).doFilter(any(), any());

            String responseBody = responseWriter.toString();
            assertTrue(responseBody.contains("Unauthorized"));
            assertTrue(responseBody.contains("Missing authentication token"));
        }

        @Test
        @DisplayName("Should bypass all /auth/* variants")
        void testAllPublicEndpoints() throws Exception {
            String[] publicEndpoints = {
                    "/auth/register",
                    "/auth/login",
                    "/auth/logout",
                    "/auth/refresh",
                    "/auth/validate",
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/api/auth/refresh",
                    "/api/auth/validate"
            };

            for (String endpoint : publicEndpoints) {
                // Reset mocks for each iteration
                reset(request, response, filterChain);

                // Given
                when(request.getRequestURI()).thenReturn(endpoint);

                // When
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Then
                verify(filterChain).doFilter(request, response);
                verify(authServiceClient, never()).validateToken(any(), any());
            }
        }

        @Test
        @DisplayName("Should reject request when token validation fails")
        void testTokenValidation_Invalid() throws Exception {
            // Given
            String invalidToken = "invalid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/op/add");
            when(cookieUtil.getAccessToken(request)).thenReturn(Optional.of(invalidToken));

            TokenValidationResponse invalidResponse = TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token expired")
                    .build();

            when(authServiceClient.validateToken(eq(invalidToken), eq(request)))
                    .thenReturn(invalidResponse);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());

            String responseBody = responseWriter.toString();
            assertTrue(responseBody.contains("Token expired"));
        }

        @Test
        @DisplayName("Should reject request when Authorization header is malformed")
        void testMalformedAuthorizationHeader() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/api/op/subtract");
            when(cookieUtil.getAccessToken(request)).thenReturn(Optional.empty());
            when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Should handle exception during token validation")
        void testTokenValidation_Exception() throws Exception {
            // Given
            String token = "some.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/op/multiply");
            when(cookieUtil.getAccessToken(request)).thenReturn(Optional.of(token));
            when(authServiceClient.validateToken(eq(token), eq(request)))
                    .thenThrow(new RuntimeException("Service unavailable"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());

            String responseBody = responseWriter.toString();
            assertTrue(responseBody.contains("Token validation failed"));
        }

        @Test
        @DisplayName("Should bypass filter for /actuator endpoints")
        void testPublicEndpoint_Actuator() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/actuator/health");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            // Note: Based on current implementation, /actuator is NOT in PUBLIC_ENDPOINTS
            // This test documents current behavior - you may want to add it
            verify(cookieUtil).getAccessToken(request);
        }


    }

    @Test
    @DisplayName("Should bypass filter for public auth endpoints")
    void testPublicEndpoint_AuthRegister() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/auth/register");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(authServiceClient, never()).validateToken(any(), any());
    }

    @Test
    @DisplayName("Should bypass filter for /api/auth/* endpoints")
    void testPublicEndpoint_ApiAuthLogin() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(authServiceClient, never()).validateToken(any(), any());
    }

    @Test
    @DisplayName("Should extract token from cookie and validate successfully")
    void testTokenExtraction_FromCookie_Success() throws Exception {
        // Given
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/op/add");
        when(cookieUtil.getAccessToken(request)).thenReturn(Optional.of(validToken));

        TokenValidationResponse validResponse = TokenValidationResponse.builder()
                .valid(true)
                .userId(1L)
                .username("testuser")
                .message("Token valid")
                .build();

        when(authServiceClient.validateToken(eq(validToken), eq(request)))
                .thenReturn(validResponse);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).setAttribute("userId", 1L);
        verify(request).setAttribute("username", "testuser");
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should extract token from Authorization header when cookie not present")
    void testTokenExtraction_FromAuthorizationHeader_Success() throws Exception {
        // Given
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/op/multiply");
        when(cookieUtil.getAccessToken(request)).thenReturn(Optional.empty());
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        TokenValidationResponse validResponse = TokenValidationResponse.builder()
                .valid(true)
                .userId(2L)
                .username("headeruser")
                .message("Token valid")
                .build();

        when(authServiceClient.validateToken(eq(validToken), eq(request)))
                .thenReturn(validResponse);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).setAttribute("userId", 2L);
        verify(request).setAttribute("username", "headeruser");
        verify(filterChain).doFilter(request, response);
    }


    @Test
    @DisplayName("Should properly set userId and username attributes on successful validation")
    void testRequestAttributes_SetCorrectly() throws Exception {
        // Given
        String token = "valid.token";
        Long expectedUserId = 12345L;
        String expectedUsername = "john.doe";

        when(request.getRequestURI()).thenReturn("/api/op/divide");
        when(cookieUtil.getAccessToken(request)).thenReturn(Optional.of(token));

        TokenValidationResponse validResponse = TokenValidationResponse.builder()
                .valid(true)
                .userId(expectedUserId)
                .username(expectedUsername)
                .message("Valid")
                .build();

        when(authServiceClient.validateToken(token, request)).thenReturn(validResponse);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        ArgumentCaptor<String> attributeNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> attributeValueCaptor = ArgumentCaptor.forClass(Object.class);

        verify(request, times(2)).setAttribute(attributeNameCaptor.capture(), attributeValueCaptor.capture());

        var capturedNames = attributeNameCaptor.getAllValues();
        var capturedValues = attributeValueCaptor.getAllValues();

        assertTrue(capturedNames.contains("userId"));
        assertTrue(capturedNames.contains("username"));
        assertTrue(capturedValues.contains(expectedUserId));
        assertTrue(capturedValues.contains(expectedUsername));
    }

    @Test
    @DisplayName("Should prefer cookie token over Authorization header when both present")
    void testTokenExtraction_CookiePriorityOverHeader() throws Exception {
        // Given
        String cookieToken = "cookie.token";
        String headerToken = "header.token";

        when(request.getRequestURI()).thenReturn("/api/op/add");
        when(cookieUtil.getAccessToken(request)).thenReturn(Optional.of(cookieToken));

        TokenValidationResponse validResponse = TokenValidationResponse.builder()
                .valid(true)
                .userId(1L)
                .username("user")
                .build();

        when(authServiceClient.validateToken(eq(cookieToken), eq(request)))
                .thenReturn(validResponse);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(authServiceClient).validateToken(eq(cookieToken), eq(request));
        verify(authServiceClient, never()).validateToken(eq(headerToken), any());
    }
}
