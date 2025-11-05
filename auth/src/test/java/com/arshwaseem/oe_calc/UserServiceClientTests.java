package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.DTOs.UserDTO;
import com.arshwaseem.oe_calc.configuration.UserServiceProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class UserServiceClientTests {

    private static WireMockServer wireMockServer;
    private UserServiceClient userServiceClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        System.out.println("✅ WireMock started on port: " + wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();

        // Create client manually - no Spring context needed!
        UserServiceProperties properties = new UserServiceProperties();
        properties.setUrl("http://localhost:" + wireMockServer.port());

        WebClient.Builder webClientBuilder = WebClient.builder();
        userServiceClient = new UserServiceClient(webClientBuilder, properties);
    }

    @Test
    void getUserByUsername_shouldReturnUser() {
        // Given
        stubFor(get(urlPathEqualTo("/user/username"))
                .withQueryParam("username", equalTo("arshwaseem"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                    "id": 1,
                                    "username": "arshwaseem",
                                    "password": "123456"
                                }
                                """)));

        // When
        UserDTO userDto = userServiceClient.getUserByUsername("arshwaseem");

        // Then
        assertThat(userDto).isNotNull();
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getUsername()).isEqualTo("arshwaseem");
        assertThat(userDto.getPassword()).isEqualTo("123456");

        verify(getRequestedFor(urlPathEqualTo("/user/username"))
                .withQueryParam("username", equalTo("arshwaseem")));
    }

    @Test
    void getUserByUsername_whenNotFound_shouldReturnNull() {
        stubFor(get(urlPathEqualTo("/user/username"))
                .withQueryParam("username", equalTo("notfound"))
                .willReturn(aResponse().withStatus(404)));

        UserDTO userDto = userServiceClient.getUserByUsername("notfound");

        assertThat(userDto).isNull();
    }

    @Test
    void getUserByUsername_whenTimeout_shouldReturnNull() {
        stubFor(get(urlPathEqualTo("/user/username"))
                .withQueryParam("username", equalTo("slow"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(6000)));  // Exceeds 5 second timeout

        UserDTO userDto = userServiceClient.getUserByUsername("slow");

        assertThat(userDto).isNull();
    }

    @Test
    void createUser_shouldReturnCreatedUser() {
        stubFor(post(urlEqualTo("/user/register"))
                .willReturn(aResponse()
                        .withStatus(201)));

        UserDTO newUser = new UserDTO(null, "newuser", "password123");
        ResponseEntity<?> createdUser = userServiceClient.createUser(newUser);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}