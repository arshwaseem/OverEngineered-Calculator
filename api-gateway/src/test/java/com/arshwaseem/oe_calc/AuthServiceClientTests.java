package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.dto.TokenValidationResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientTests {

    @Mock
    private WebClient authServiceWebclient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthServiceClient authServiceClient;

    private static final String VALID_TOKEN = "valid.jwt.token";

    @Test
    @DisplayName("Should successfully validate token with cookies")
    void testValidateToken_Success() {
        // Given
        Cookie[] cookies = {
                new Cookie("accessToken", VALID_TOKEN),
                new Cookie("refreshToken", "refresh.token")
        };
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        TokenValidationResponse expectedResponse = TokenValidationResponse.builder()
                .valid(true)
                .userId(1L)
                .username("testuser")
                .message("Token is valid")
                .build();

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(expectedResponse));

        // When
        TokenValidationResponse result = authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        verify(requestBodyUriSpec).uri("/auth/validate");
    }

    @Test
    @DisplayName("Should include cookie header in request")
    void testValidateToken_IncludesCookieHeader() {
        // Given
        Cookie[] cookies = {
                new Cookie("accessToken", "token1"),
                new Cookie("refreshToken", "token2"),
                new Cookie("sessionId", "session123")
        };
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .build();

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(response));

        // When
        authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestBodySpec).header(eq("Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertTrue(cookieHeader.contains("accessToken=token1"));
        assertTrue(cookieHeader.contains("refreshToken=token2"));
        assertTrue(cookieHeader.contains("sessionId=session123"));
    }

    @Test
    @DisplayName("Should return invalid response when cookies are empty")
    void testValidateToken_EmptyCookies() {
        // Given
        Cookie[] cookies = {};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        // When
        TokenValidationResponse result = authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Failed to validate"));
    }

    @Test
    @DisplayName("Should handle WebClient timeout")
    void testValidateToken_Timeout() {
        // Given
        Cookie[] cookies = {new Cookie("accessToken", VALID_TOKEN)};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Timeout")));

        // When
        TokenValidationResponse result = authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Failed to validate"));
        assertTrue(result.getMessage().contains("Timeout"));
    }

    @Test
    @DisplayName("Should handle 401 Unauthorized response")
    void testValidateToken_Unauthorized() {
        // Given
        Cookie[] cookies = {new Cookie("accessToken", "invalid.token")};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.error(WebClientResponseException.create(
                        401, "Unauthorized", null, null, null
                )));

        // When
        TokenValidationResponse result = authServiceClient.validateToken("invalid.token", httpServletRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Failed to validate"));
    }

    @Test
    @DisplayName("Should handle 500 Internal Server Error")
    void testValidateToken_ServerError() {
        // Given
        Cookie[] cookies = {new Cookie("accessToken", VALID_TOKEN)};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.error(WebClientResponseException.create(
                        500, "Internal Server Error", null, null, null
                )));

        // When
        TokenValidationResponse result = authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Failed to validate"));
    }

    @Test
    @DisplayName("Should handle network connection error")
    void testValidateToken_NetworkError() {
        // Given
        Cookie[] cookies = {new Cookie("accessToken", VALID_TOKEN)};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));

        // When
        TokenValidationResponse result = authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Connection refused"));
    }

    @Test
    @DisplayName("Should handle multiple cookies correctly")
    void testValidateToken_MultipleCookies() {
        // Given
        Cookie[] cookies = {
                new Cookie("accessToken", "token1"),
                new Cookie("refreshToken", "token2"),
                new Cookie("deviceId", "device123"),
                new Cookie("sessionId", "session456")
        };
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .build();

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(response));

        // When
        authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestBodySpec).header(eq("Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        String[] cookiePairs = cookieHeader.split(";");
        assertEquals(4, cookiePairs.length);
    }

    @Test
    @DisplayName("Should handle empty token")
    void testValidateToken_EmptyToken() {
        // Given
        Cookie[] cookies = {new Cookie("accessToken", "")};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(false)
                .message("Empty token")
                .build();

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(response));

        // When
        TokenValidationResponse result = authServiceClient.validateToken("", httpServletRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should verify URI is called correctly")
    void testValidateToken_VerifyUriCall() {
        // Given
        Cookie[] cookies = {new Cookie("accessToken", VALID_TOKEN)};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .build();

        when(authServiceWebclient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(response));

        // When
        authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        verify(requestBodyUriSpec).uri("/auth/validate");
    }
}