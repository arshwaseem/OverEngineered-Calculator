package com.arshwaseem.oe_calc;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Tests for API Gateway
 * Tests the complete flow including:
 * - JWT authentication filter
 * - REST endpoint routing
 * - gRPC communication with operation services
 * - Auth service integration (mocked with WireMock)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiGatewayIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static WireMockServer authServiceMock;
    private static Server addGrpcServer;
    private static Server subtractGrpcServer;
    private static Server multiplyGrpcServer;
    private static Server divideGrpcServer;

    private static final int AUTH_SERVICE_PORT = 8888;
    private static final int ADD_GRPC_PORT = 50061;
    private static final int SUBTRACT_GRPC_PORT = 50062;
    private static final int MULTIPLY_GRPC_PORT = 50063;
    private static final int DIVIDE_GRPC_PORT = 50064;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("auth.service.url", () -> "http://localhost:" + AUTH_SERVICE_PORT);
        registry.add("grpc.add.host", () -> "localhost");
        registry.add("grpc.add.port", () -> ADD_GRPC_PORT);
        registry.add("grpc.subtract.host", () -> "localhost");
        registry.add("grpc.subtract.port", () -> SUBTRACT_GRPC_PORT);
        registry.add("grpc.multiply.host", () -> "localhost");
        registry.add("grpc.multiply.port", () -> MULTIPLY_GRPC_PORT);
        registry.add("grpc.divide.host", () -> "localhost");
        registry.add("grpc.divide.port", () -> DIVIDE_GRPC_PORT);
    }

    @BeforeAll
    static void setupMocks() throws IOException {
        // Setup WireMock for Auth Service
        authServiceMock = new WireMockServer(wireMockConfig().port(AUTH_SERVICE_PORT));
        authServiceMock.start();
        WireMock.configureFor("localhost", AUTH_SERVICE_PORT);

        // Setup gRPC mock servers
        addGrpcServer = ServerBuilder.forPort(ADD_GRPC_PORT)
                .addService(new MockAddService())
                .build()
                .start();

        subtractGrpcServer = ServerBuilder.forPort(SUBTRACT_GRPC_PORT)
                .addService(new MockSubtractService())
                .build()
                .start();

        multiplyGrpcServer = ServerBuilder.forPort(MULTIPLY_GRPC_PORT)
                .addService(new MockMultiplyService())
                .build()
                .start();

        divideGrpcServer = ServerBuilder.forPort(DIVIDE_GRPC_PORT)
                .addService(new MockDivideService())
                .build()
                .start();
    }

    @AfterAll
    static void tearDownMocks() {
        if (authServiceMock != null) {
            authServiceMock.stop();
        }
        if (addGrpcServer != null) {
            addGrpcServer.shutdown();
        }
        if (subtractGrpcServer != null) {
            subtractGrpcServer.shutdown();
        }
        if (multiplyGrpcServer != null) {
            multiplyGrpcServer.shutdown();
        }
        if (divideGrpcServer != null) {
            divideGrpcServer.shutdown();
        }
    }

    @BeforeEach
    void setupTest() {
        authServiceMock.resetAll();
    }

    // ========== AUTHENTICATION TESTS ==========

    @Test
    @Order(1)
    @DisplayName("Integration: Should allow request to calculation endpoint with valid token")
    void testAuthenticatedRequest_ValidToken() {
        // Given
        stubAuthServiceForValidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(10.0);
        request.setNumB(5.0);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "accessToken=" + VALID_TOKEN);
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Double> response = restTemplate.exchange(
                "/api/op/add",
                HttpMethod.POST,
                entity,
                Double.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(15.0);
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Should reject request without token")
    void testUnauthenticatedRequest_NoToken() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(10.0);
        request.setNumB(5.0);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/op/add",
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Unauthorized");
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Should reject request with invalid token")
    void testAuthenticatedRequest_InvalidToken() {
        // Given
        stubAuthServiceForInvalidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(10.0);
        request.setNumB(5.0);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "accessToken=" + INVALID_TOKEN);
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/op/add",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Should accept Bearer token in Authorization header")
    void testAuthenticatedRequest_BearerToken() {
        // Given
        stubAuthServiceForValidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(7.0);
        request.setNumB(3.0);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + VALID_TOKEN);
        // Need at least one cookie for the auth service client
        headers.add("Cookie", "dummy=value");
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Double> response = restTemplate.exchange(
                "/api/op/subtract",
                HttpMethod.POST,
                entity,
                Double.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(4.0);
    }

    // ========== OPERATION ENDPOINT TESTS ==========

    @Test
    @Order(5)
    @DisplayName("Integration: Add endpoint should return correct sum")
    void testAddEndpoint() {
        // Given
        stubAuthServiceForValidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(100.5);
        request.setNumB(50.3);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Double> response = restTemplate.exchange(
                "/api/op/add",
                HttpMethod.POST,
                entity,
                Double.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(150.8);
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Subtract endpoint should return correct difference")
    void testSubtractEndpoint() {
        // Given
        stubAuthServiceForValidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(100.0);
        request.setNumB(30.0);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Double> response = restTemplate.exchange(
                "/api/op/subtract",
                HttpMethod.POST,
                entity,
                Double.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(70.0);
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Multiply endpoint should return correct product")
    void testMultiplyEndpoint() {
        // Given
        stubAuthServiceForValidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(12.0);
        request.setNumB(8.0);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Double> response = restTemplate.exchange(
                "/api/op/multiply",
                HttpMethod.POST,
                entity,
                Double.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(96.0);
    }

    @Test
    @Order(8)
    @DisplayName("Integration: Divide endpoint should return correct quotient")
    void testDivideEndpoint() {
        // Given
        stubAuthServiceForValidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(100.0);
        request.setNumB(4.0);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Double> response = restTemplate.exchange(
                "/api/op/divide",
                HttpMethod.POST,
                entity,
                Double.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(25.0);
    }

    @Test
    @Order(9)
    @DisplayName("Integration: Should handle negative numbers correctly")
    void testNegativeNumbers() {
        // Given
        stubAuthServiceForValidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(-10.0);
        request.setNumB(-5.0);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Double> response = restTemplate.exchange(
                "/api/op/add",
                HttpMethod.POST,
                entity,
                Double.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(-15.0);
    }

    @Test
    @Order(10)
    @DisplayName("Integration: Should handle decimal numbers correctly")
    void testDecimalNumbers() {
        // Given
        stubAuthServiceForValidToken();
        
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(10.5);
        request.setNumB(2.3);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<OperationRequestDTO> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Double> response = restTemplate.exchange(
                "/api/op/multiply",
                HttpMethod.POST,
                entity,
                Double.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isCloseTo(24.15, org.assertj.core.data.Offset.offset(0.01));
    }

    // ========== HELPER METHODS ==========

    private void stubAuthServiceForValidToken() {
        authServiceMock.stubFor(post(urlEqualTo("/auth/validate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("""
                                {
                                    "valid": true,
                                    "userId": %d,
                                    "username": "%s",
                                    "message": "Token is valid"
                                }
                                """, TEST_USER_ID, TEST_USERNAME))));
    }

    private void stubAuthServiceForInvalidToken() {
        authServiceMock.stubFor(post(urlEqualTo("/auth/validate"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "valid": false,
                                    "message": "Invalid token"
                                }
                                """)));
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "accessToken=" + VALID_TOKEN);
        return headers;
    }

    // ========== MOCK gRPC SERVICES ==========

    static class MockAddService extends OperationServiceGrpc.OperationServiceImplBase {
        @Override
        public void add(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
            double result = request.getNumA() + request.getNumB();
            OperationResponse response = OperationResponse.newBuilder()
                    .setResult(result)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    static class MockSubtractService extends OperationServiceGrpc.OperationServiceImplBase {
        @Override
        public void subtract(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
            double result = request.getNumA() - request.getNumB();
            OperationResponse response = OperationResponse.newBuilder()
                    .setResult(result)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    static class MockMultiplyService extends OperationServiceGrpc.OperationServiceImplBase {
        @Override
        public void multiply(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
            double result = request.getNumA() * request.getNumB();
            OperationResponse response = OperationResponse.newBuilder()
                    .setResult(result)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    static class MockDivideService extends OperationServiceGrpc.OperationServiceImplBase {
        @Override
        public void divide(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
            double result = request.getNumA() / request.getNumB();
            OperationResponse response = OperationResponse.newBuilder()
                    .setResult(result)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
