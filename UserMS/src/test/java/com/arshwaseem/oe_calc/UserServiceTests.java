package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public class UserServiceTests {
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    private UserService userService;

    public UserServiceTests(UserService userService) {
        this.userService = userService;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @BeforeAll
    static void setup() {
        postgres.start();
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.execute("INSERT INTO USERS('UserName', 'Password', 'LastResult') VALUES ('test', 'test', 0)");
    }

    @AfterAll
    static void teardown() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.execute("DELETE FROM USERS");
        postgres.stop();
    }

    @Test
    void getById(){
        User res = userService.GetByID(Long.valueOf(1)).get();
        Assertions.assertNotNull(res);
        Assertions.assertEquals(res.getID(), Long.valueOf(1));
    }

    @Test
    void getByUsername(){
        User res = userService.GetByName("test").get();
        Assertions.assertNotNull(res);
        Assertions.assertEquals(res.getUserName(), "test");
    }
}