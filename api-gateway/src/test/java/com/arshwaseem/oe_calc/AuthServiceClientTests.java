package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.configuration.AuthServiceProperties;
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

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientTests {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private AuthServiceProperties authServiceProperties;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthServiceClient authServiceClient;

    private static final String AUTH_SERVICE_URL = "http://localhost:8080";
    private static final String VALID_TOKEN = "valid.jwt.token";

    @BeforeEach()
    void setUp(TestInfo testInfo) {
        if (testInfo.getDisplayName().contains("Should return invalid response when cookies are null")) {
            return;
        }
        when(authServiceProperties.getUrl()).thenReturn(AUTH_SERVICE_URL);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

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

        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(expectedResponse));

        // When
        TokenValidationResponse result = authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        verify(webClientBuilder).build();
        verify(requestBodyUriSpec).uri(AUTH_SERVICE_URL + "/auth/validate");
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
    @DisplayName("Should return invalid response when cookies are null")
    void testValidateToken_NullCookies() {
        // Given
        when(httpServletRequest.getCookies()).thenReturn(null);

        TokenValidationResponse res = authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        Assertions.assertFalse(res.isValid());
    }

    @Test
    @DisplayName("Should handle WebClient timeout")
    void testValidateToken_Timeout() {
        // Given
        Cookie[] cookies = {new Cookie("accessToken", VALID_TOKEN)};
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        
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
    @DisplayName("Should use correct auth service URL from properties")
    void testValidateToken_UsesCorrectUrl() {
        // Given
        String customUrl = "http://custom-auth-service:9090";
        when(authServiceProperties.getUrl()).thenReturn(customUrl);
        authServiceClient = new AuthServiceClient(webClientBuilder, authServiceProperties);

        Cookie[] cookies = {new Cookie("accessToken", VALID_TOKEN)};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .build();
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(response));

        // When
        authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then
        verify(requestBodyUriSpec).uri(customUrl + "/auth/validate");
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
        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(Mono.just(response));

        // When
        TokenValidationResponse result = authServiceClient.validateToken("", httpServletRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should respect 5 second timeout")
    void testValidateToken_TimeoutConfiguration() {
        // Given
        Cookie[] cookies = {new Cookie("accessToken", VALID_TOKEN)};
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        // Create a delayed response that exceeds timeout
        Mono<TokenValidationResponse> delayedResponse = Mono.just(
                TokenValidationResponse.builder().valid(true).build()
        ).delayElement(Duration.ofSeconds(6));

        when(responseSpec.bodyToMono(TokenValidationResponse.class))
                .thenReturn(delayedResponse);

        // When
        TokenValidationResponse result = authServiceClient.validateToken(VALID_TOKEN, httpServletRequest);

        // Then - Should timeout and return error response
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Failed to validate"));
    }
}
