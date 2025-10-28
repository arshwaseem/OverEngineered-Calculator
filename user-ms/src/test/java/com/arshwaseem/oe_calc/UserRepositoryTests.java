package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

@DataJpaTest
public class UserRepositoryTests {
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest").withDatabaseName("test").withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("INSERT INTO users(username,password) VALUES('test','test')");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Autowired
    UserJPARepository userJPARepository;

    @Test
    void user_ShouldPersistUser(){
        User userToSave = new User();
        userToSave.setUsername("pass1");
        userToSave.setPassword("pass1");
        User userSaved = userJPARepository.save(userToSave);

        Assertions.assertNotNull(userSaved);
        Assertions.assertEquals(userSaved.getUsername(), userToSave.getUsername());
        Assertions.assertEquals(userSaved.getPassword(), userToSave.getPassword());
    }

    @Test
    void user_ShouldGetByName(){

        Optional<User> receivedUser = userJPARepository.findByusername("test");

        Assertions.assertTrue(receivedUser.isPresent());
        Assertions.assertEquals("test", receivedUser.get().getUsername());
        Assertions.assertEquals("test", receivedUser.get().getPassword());
    }

    @Test
    void user_ShouldDeleteByUsername(){
        userJPARepository.deleteByusername("test");

        Optional<User> receivedUser = userJPARepository.findByusername("test");

        Assertions.assertFalse(receivedUser.isPresent());
    }
}
