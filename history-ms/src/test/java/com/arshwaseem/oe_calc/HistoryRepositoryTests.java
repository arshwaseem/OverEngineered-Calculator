package com.arshwaseem.oe_calc;

import org.junit.After;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@Testcontainers
public class HistoryRepositoryTests {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>();

    @DynamicPropertySource
    static void postgreSQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    HistoryJPARepository historyJPARepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void init() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("INSERT INTO history(servicename, numa, numb, result) VALUES ('adder',2,2,4)");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM history");
    }

    @AfterAll
    static void tearDownAll() {
        postgreSQLContainer.stop();
    }

    @Test
    void history_ShouldPersist(){

        History toSave = new History();

        toSave.setServiceName("test");

        History saved = historyJPARepository.save(toSave);

        Assertions.assertNotNull(saved);
        Assertions.assertEquals("test",  saved.getServiceName());

    }

    @Test
    void history_ShouldFindById(){
        Optional<History> received = historyJPARepository.findById(1L);
        Assertions.assertTrue(received.isPresent());
        Assertions.assertEquals("adder", received.get().getServiceName());
    }

    @Test
    void history_ShouldFindAllByServiceName(){
        List<History> received = historyJPARepository.findAllByServiceName("adder");

        Assertions.assertFalse(received.isEmpty());
        Assertions.assertEquals("adder", received.get(0).getServiceName());
    }
}