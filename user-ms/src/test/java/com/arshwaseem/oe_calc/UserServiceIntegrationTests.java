package com.arshwaseem.oe_calc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class UserServiceIntegrationTests {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    public static void init() {
        postgreSQLContainer.start();
    }

    @AfterAll
    public static void close() {
        postgreSQLContainer.close();
    }


    @Test
    void user_FullCrudFlow() {

        UserRequest createRequest = new UserRequest("test", "pass");

        ResponseEntity<?> createResponse = testRestTemplate.postForEntity("/user/register", createRequest, UserRequest.class);

        Assertions.assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        ResponseEntity<UserResponse> getResponse = testRestTemplate.getForEntity("/user/username?username={username}", UserResponse.class, "test");

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals("test", getResponse.getBody().getUsername());

        ResponseEntity<?> deleteResponse = testRestTemplate.exchange(
                "/user/delete?username={username}", HttpMethod.DELETE, null, Void.class, "test"
        );

        Assertions.assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<?> getResponse2 = testRestTemplate.getForEntity("/user/username?username={username}", UserResponse.class, "test");

        Assertions.assertEquals(HttpStatus.NOT_FOUND, getResponse2.getStatusCode());
    }

}