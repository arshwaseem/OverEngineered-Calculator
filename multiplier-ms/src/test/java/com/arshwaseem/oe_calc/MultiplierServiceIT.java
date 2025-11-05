package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.history.History;
import com.arshwaseem.oe_calc.history.HistoryService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest
public class MultiplierServiceIT {
    private final String queueName = "history-queue";

    @Autowired
    private MessageConverter messageConverter;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.grpc.server.inprocess.name", () -> "test");
        registry.add("spring.grpc.server.port", () -> "-1");
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    }

    @Autowired
    HistoryService historyService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Container
    static RabbitMQContainer rabbitMQContainer;

    private static ManagedChannel channel;
    private static OperationServiceGrpc.OperationServiceBlockingStub blockingStub;

    @BeforeAll
    static void init() {
        channel = InProcessChannelBuilder.forName("test").directExecutor().usePlaintext().build();
        blockingStub = OperationServiceGrpc.newBlockingStub(channel);
        rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management").withExposedPorts(5672, 15672).withQueue("history-queue");
        rabbitMQContainer.start();
    }

    @AfterAll
    static void shutdown() {
        channel.shutdown();
        rabbitMQContainer.stop();
    }

    @BeforeEach
    void purgeQueue() {
        rabbitTemplate.execute(channel1 -> {
            channel1.queuePurge(queueName);
            return null;
        });
    }

    @Test
    void multiply_ShouldReturnCorrectSumGrpc() {

        OperationRequest operationRequest = OperationRequest.newBuilder().setNumA(9.0).setNumB(3.0).build();

        OperationResponse operationResponse = blockingStub.multiply(operationRequest);

        Assertions.assertEquals(27.0, operationResponse.getResult());
    }

    @Test
    void multiply_ShouldPublishMessageToQueue() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<History> historyMessage = new AtomicReference<>();

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
        container.setQueueNames(queueName);
        container.setMessageListener(
                (message) -> {
                    historyMessage.set((History) messageConverter.fromMessage(message));
                    countDownLatch.countDown();
                }
        );
        container.start();

        boolean received = countDownLatch.await(10, TimeUnit.SECONDS);
        Assertions.assertTrue(received);
        Assertions.assertEquals(27.0, historyMessage.get().getResult());
        Assertions.assertEquals(9.0, historyMessage.get().getNumA());

    }
}
