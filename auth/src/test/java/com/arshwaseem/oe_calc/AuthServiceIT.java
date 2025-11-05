package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.DTOs.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Tests for Auth Service
 * These tests verify the complete authentication flow including:
 * - User registration
 * - User login with JWT generation
 * - Token validation
 * - Token refresh
 * - Cookie handling
 * - Integration with User Service (mocked via WireMock)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static WireMockServer wireMockServer;

    private static final String TEST_USERNAME = "integrationTestUser";
    private static final String TEST_PASSWORD = "SecurePassword123!";
    private static final Long TEST_USER_ID = 1L;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("user.service.url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("jwt.secret", () -> "Aw7PTgR6oOahlwGLqgtWfV1TUKN61BwW");
        registry.add("jwt.expiration", () -> "3600000"); // 1 hour
        registry.add("jwt.refresh.expiration", () -> "604800000"); // 7 days
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
    }


    @Test
    @Order(1)
    @DisplayName("Integration: Should successfully register a new user")
    void testRegistration_Success() {

        stubUserServiceForRegistration(TEST_USERNAME, TEST_PASSWORD, TEST_USER_ID);

        RegisterRequest registerRequest = new RegisterRequest(TEST_USERNAME, TEST_PASSWORD);


        ResponseEntity<RegisterResponse> response = testRestTemplate.postForEntity(
                "/auth/register",
                registerRequest,
                RegisterResponse.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();


        wireMockServer.verify(postRequestedFor(urlEqualTo("/user/register"))
                .withRequestBody(matchingJsonPath("$.username", equalTo(TEST_USERNAME)))
        );
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Should fail registration when User Service is unavailable")
    void testRegistration_UserServiceUnavailable() {

        wireMockServer.stubFor(post(urlEqualTo("/user/register"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        RegisterRequest registerRequest = new RegisterRequest("newuser", "password123");


        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/auth/register",
                registerRequest,
                String.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Error");
    }



    @Test
    @Order(3)
    @DisplayName("Integration: Should successfully login with valid credentials")
    void testLogin_Success() {

        stubUserServiceForLogin(TEST_USERNAME, TEST_PASSWORD, TEST_USER_ID);

        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);


        ResponseEntity<AuthResponse> response = testRestTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                AuthResponse.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        AuthResponse authResponse = response.getBody();
        assertThat(authResponse.getAccessToken()).isNotBlank();
        assertThat(authResponse.getRefreshToken()).isNotBlank();
        assertThat(authResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(authResponse.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(authResponse.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(authResponse.getExpiresIn()).isGreaterThan(0);


        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();
        assertThat(cookies).hasSize(2);

        boolean hasAccessToken = cookies.stream().anyMatch(c -> c.startsWith("accessToken="));
        boolean hasRefreshToken = cookies.stream().anyMatch(c -> c.startsWith("refreshToken="));
        assertThat(hasAccessToken).isTrue();
        assertThat(hasRefreshToken).isTrue();


        assertThat(jwtService.validateToken(authResponse.getAccessToken())).isTrue();
        assertThat(jwtService.extractUsername(authResponse.getAccessToken())).isEqualTo(TEST_USERNAME);
        assertThat(jwtService.extractUserId(authResponse.getAccessToken())).isEqualTo(TEST_USER_ID);
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Should fail login with invalid password")
    void testLogin_InvalidPassword() {

        stubUserServiceForLogin(TEST_USERNAME, TEST_PASSWORD, TEST_USER_ID);

        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, "WrongPassword123!");


        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                String.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Error");
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Should fail login when user does not exist")
    void testLogin_UserNotFound() {

        wireMockServer.stubFor(get(urlPathEqualTo("/user/username"))
                .withQueryParam("username", equalTo("nonexistentuser"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        LoginRequest loginRequest = new LoginRequest("nonexistentuser", "password");


        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                String.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @Test
    @Order(6)
    @DisplayName("Integration: Should validate a valid token")
    void testValidateToken_Success() {

        stubUserServiceForLogin(TEST_USERNAME, TEST_PASSWORD, TEST_USER_ID);


        String token = performLoginAndGetToken();


        stubUserServiceForLogin(TEST_USERNAME, TEST_USERNAME, TEST_USER_ID);


        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "accessToken=" + token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<TokenValidationResponse> response = testRestTemplate.exchange(
                "/auth/validate",
                HttpMethod.POST,
                request,
                TokenValidationResponse.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isValid()).isTrue();
        assertThat(response.getBody().getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(response.getBody().getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getBody().getMessage()).contains("valid");
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Should reject invalid token")
    void testValidateToken_InvalidToken() {
        // Given: An invalid/tampered token
        String invalidToken = "invalid.token.here";


        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "accessToken=" + invalidToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<TokenValidationResponse> response = testRestTemplate.exchange(
                "/auth/validate",
                HttpMethod.POST,
                request,
                TokenValidationResponse.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isValid()).isFalse();
    }

    @Test
    @Order(8)
    @DisplayName("Integration: Should reject validation when token is missing")
    void testValidateToken_MissingToken() {

        ResponseEntity<TokenValidationResponse> response = testRestTemplate.postForEntity(
                "/auth/validate",
                null,
                TokenValidationResponse.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    @Order(9)
    @DisplayName("Integration: Should refresh access token with valid refresh token")
    void testRefreshToken_Success() {
        stubUserServiceForLogin(TEST_USERNAME, TEST_PASSWORD, TEST_USER_ID);


        AuthTokens tokens = performLoginAndGetTokens();


        stubUserServiceForLogin(TEST_USERNAME, TEST_PASSWORD, TEST_USER_ID);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "accessToken=" + tokens.accessToken() + "; refreshToken=" + tokens.refreshToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<AuthResponse> response = testRestTemplate.exchange(
                "/auth/refresh",
                HttpMethod.POST,
                request,
                AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        AuthResponse authResponse = response.getBody();
        assertThat(authResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(authResponse.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(authResponse.getUserId()).isEqualTo(TEST_USER_ID);

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();
        boolean hasAccessToken = cookies.stream().anyMatch(c -> c.startsWith("accessToken="));
        assertThat(hasAccessToken).isTrue();
    }

    @Test
    @Order(10)
    @DisplayName("Integration: Should fail refresh when refresh token is missing")
    void testRefreshToken_MissingToken() {
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/auth/refresh",
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Error");
    }

    @Test
    @Order(11)
    @DisplayName("Integration: Should fail refresh with invalid refresh token")
    void testRefreshToken_InvalidToken() {
        String invalidRefreshToken = "invalid.refresh.token";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "refreshToken=" + invalidRefreshToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = testRestTemplate.exchange(
                "/auth/refresh",
                HttpMethod.POST,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Error");
    }


    @Test
    @Order(12)
    @DisplayName("Integration: Should successfully logout and clear cookies")
    void testLogout_Success() {
        // When: POST /auth/logout
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/auth/logout",
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();

        boolean accessTokenCleared = cookies.stream()
                .anyMatch(c -> c.startsWith("accessToken=") && c.contains("Max-Age=0"));
        boolean refreshTokenCleared = cookies.stream()
                .anyMatch(c -> c.startsWith("refreshToken=") && c.contains("Max-Age=0"));

        assertThat(accessTokenCleared).isTrue();
        assertThat(refreshTokenCleared).isTrue();
    }


    @Test
    @Order(13)
    @DisplayName("Integration: Complete authentication flow - Register → Login → Validate → Refresh → Logout")
    void testCompleteAuthenticationFlow() {
        String username = "flowTestUser";
        String password = "FlowPassword123!";
        Long userId = 999L;

        stubUserServiceForRegistration(username, password, userId);
        RegisterRequest registerRequest = new RegisterRequest(username, password);

        ResponseEntity<RegisterResponse> registerResponse = testRestTemplate.postForEntity(
                "/auth/register",
                registerRequest,
                RegisterResponse.class
        );

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        stubUserServiceForLogin(username, password, userId);
        LoginRequest loginRequest = new LoginRequest(username, password);

        ResponseEntity<AuthResponse> loginResponse = testRestTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                AuthResponse.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String accessToken = loginResponse.getBody().getAccessToken();
        String refreshToken = loginResponse.getBody().getRefreshToken();

        stubUserServiceForLogin(username, password, userId);
        HttpHeaders validateHeaders = new HttpHeaders();
        validateHeaders.add("Cookie", "accessToken=" + accessToken);

        ResponseEntity<TokenValidationResponse> validateResponse = testRestTemplate.exchange(
                "/auth/validate",
                HttpMethod.POST,
                new HttpEntity<>(validateHeaders),
                TokenValidationResponse.class
        );

        assertThat(validateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validateResponse.getBody().isValid()).isTrue();

        stubUserServiceForLogin(username, password, userId);
        HttpHeaders refreshHeaders = new HttpHeaders();
        refreshHeaders.add("Cookie", "accessToken=" + accessToken + "; refreshToken=" + refreshToken);

        ResponseEntity<AuthResponse> refreshResponse = testRestTemplate.exchange(
                "/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(refreshHeaders),
                AuthResponse.class
        );

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);


        ResponseEntity<String> logoutResponse = testRestTemplate.postForEntity(
                "/auth/logout",
                null,
                String.class
        );

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    private void stubUserServiceForRegistration(String username, String encodedPassword, Long userId) {
        wireMockServer.stubFor(post(urlEqualTo("/user/register"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")));
    }

    private void stubUserServiceForLogin(String username, String password, Long userId) {
        wireMockServer.stubFor(get(urlPathEqualTo("/user/username"))
                .withQueryParam("username", equalTo(username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("""
                                {
                                    "id": %d,
                                    "username": "%s",
                                    "password": "%s"
                                }
                                """, userId, username, passwordEncoder.encode(password)))));
    }

    private String performLoginAndGetToken() {
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        ResponseEntity<AuthResponse> response = testRestTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                AuthResponse.class
        );
        return response.getBody().getAccessToken();
    }

    private AuthTokens performLoginAndGetTokens() {
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        ResponseEntity<AuthResponse> response = testRestTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                AuthResponse.class
        );
        return new AuthTokens(
                response.getBody().getAccessToken(),
                response.getBody().getRefreshToken()
        );
    }

    private record AuthTokens(String accessToken, String refreshToken) {}


    @Test
    @Order(14)
    @DisplayName("Integration: Verify cookie attributes are set correctly")
    void testCookieAttributes() {
        stubUserServiceForLogin(TEST_USERNAME, TEST_PASSWORD, TEST_USER_ID);

        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        ResponseEntity<AuthResponse> response = testRestTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                AuthResponse.class
        );

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();

        for (String cookie : cookies) {
            assertThat(cookie).contains("HttpOnly");

            assertThat(cookie).contains("Path=/");


            System.out.println("Cookie: " + cookie);
        }
    }

    @Test
    @Order(15)
    @DisplayName("Integration: Verify User Service timeout handling")
    void testUserServiceTimeout() {
        wireMockServer.stubFor(get(urlPathEqualTo("/user/username"))
                .withQueryParam("username", equalTo("timeoutuser"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(6000) // 6 seconds (> 5 second timeout)
                        .withBody("{}")));

        LoginRequest loginRequest = new LoginRequest("timeoutuser", "password");

        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Error");
    }
}