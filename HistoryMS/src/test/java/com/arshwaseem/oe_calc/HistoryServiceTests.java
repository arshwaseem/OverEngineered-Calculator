package com.arshwaseem.oe_calc;

import org.hibernate.AssertionFailure;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class HistoryServiceTests {
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.flyway.enabled", ()-> "false");
    }

    @Autowired
    private HistoryService historyService;

    @BeforeEach
    public void init(@Autowired JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS history (id SERIAL PRIMARY KEY, serviceName VARCHAR(50) NOT NULL, numA DOUBLE PRECISION NOT NULL, numB DOUBLE PRECISION NOT NULL, result DOUBLE PRECISION NOT NULL, userId BIGINT NOT NULL ,timeStamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("INSERT INTO history (serviceName, numA, numB, result, userId, timeStamp) VALUES ('test', 1, 1, 2, 1, CURRENT_TIMESTAMP)");
    }

    @AfterEach
    public void close(@Autowired @NotNull JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DROP TABLE history");
    }

    @AfterAll
    public static void cleanUp() {
        postgres.stop();
    }

    @Test
    public void GetById(){
        History res = historyService.GetHistoryByID(Long.valueOf(1));
        Assertions.assertNotNull(res);
        Assertions.assertEquals("test", res.getServiceName());
    }

    @Test
    public void AddHistory(){
        History res = new History();
        res.setServiceName("test");
        res.setNumA(10);
        res.setNumB(20);
        res.setResult(30);
        res.setTimeStamp(Timestamp.valueOf(LocalDateTime.now()));

        try {
            historyService.AddUpdateHistory(res);
        } catch (Exception e) {
            throw new AssertionFailure(e.getMessage());
        }
    }

    @Test
    public void DeleteHistory(){
        try {
            historyService.DeleteHistory(Long.valueOf(1));
        } catch (Exception e) {
            throw new AssertionFailure(e.getMessage());
        }
    }

    @Test
    public void GetAllHistory(){
        List<History> res = historyService.GetAllHistory();
        Assertions.assertNotNull(res);
        Assertions.assertNotEquals(0, res.size());
    }

    @Test
    public void GetAllByServiceName(){
        List<History> res = historyService.GetAllByServiceName("test");
        Assertions.assertNotNull(res);
        Assertions.assertNotEquals(0, res.size());
        Assertions.assertEquals("test", res.get(0).getServiceName());
    }
}