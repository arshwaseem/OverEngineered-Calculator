package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@Testcontainers
@SpringBootTest
public class HistoryServiceIT {

    @Container
    private static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management").withQueue("history-queue");

    @Container
    private static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private HistoryService historyService;

    @BeforeAll
    static void init() {
        postgres.start();
        rabbitMQContainer.start();
    }

    @AfterAll
    static void destroy() {
        postgres.stop();
        rabbitMQContainer.stop();
    }
    
    @Test
    void history_ShouldConsumeRabbitMQMessage() throws InterruptedException {

        History history = new History();
        history.setServiceName("test");
        history.setResult(5.0);
        history.setNumA(3.0);
        history.setNumB(2.0);
        history.setTimeStamp(Timestamp.from(Instant.now()));

        rabbitTemplate.convertAndSend("","history-queue", history);

        Thread.sleep(5000);

        List<History> historyList = historyService.GetAllHistory();

        Assertions.assertFalse(historyList.isEmpty());
        Assertions.assertEquals(1, historyList.get(0).getId());
    }


}
