package com.arshwaseem.oe_calc;

import org.junit.AfterClass;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@SpringBootTest
public class UserServiceTests {
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled",() -> "false");
    }


    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setup(@Autowired DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users(\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    userName VARCHAR(50) NOT NULL UNIQUE,\n" +
                "    password VARCHAR(255) NOT NULL,\n" +
                "    lastResult DOUBLE PRECISION\n" +
                ")");
        jdbcTemplate.execute("INSERT INTO users(userName, password, lastResult) VALUES ('test', 'test', 0)");
    }

    @AfterEach
    void teardown(@Autowired DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DELETE FROM users");
    }

    @AfterClass
    public static void cleanUp() {
        postgres.stop();
    }

    @Test
    void getById(){
        User res = userService.GetByID(Long.valueOf(1)).get();
        Assertions.assertNotNull(res);
        Assertions.assertEquals(res.getId(), Long.valueOf(1));
    }

    @Test
    void getByUsername(){
        User res = userService.GetByName("test").get();
        Assertions.assertNotNull(res);
        Assertions.assertEquals(res.getUserName(), "test");
    }
}