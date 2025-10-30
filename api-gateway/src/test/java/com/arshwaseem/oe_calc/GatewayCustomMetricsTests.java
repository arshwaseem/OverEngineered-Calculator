package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class GatewayCustomMetricsTests {

    private MeterRegistry meterRegistry;
    private GatewayCustomMetrics gatewayCustomMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gatewayCustomMetrics = new GatewayCustomMetrics(meterRegistry);
    }

    @Test
    @DisplayName("Should record successful gRPC call with timer and counter")
    void testRecordGrpcCall_Success() {
        // Given
        String service = "adder";
        String operation = "add";
        Supplier<Double> supplier = () -> 15.0;

        // When
        Double result = gatewayCustomMetrics.recordGrpcCall(service, operation, supplier);

        // Then
        assertEquals(15.0, result);

        // Verify counter was incremented
        Counter counter = meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("service", service)
                .tag("method", operation)
                .counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());

        // Verify timer was recorded
        Timer timer = meterRegistry.find("calculator.grpc.client.calls.duration")
                .tag("service", service)
                .tag("method", operation)
                .timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
    }

    @Test
    @DisplayName("Should record failed gRPC call with error status")
    void testRecordGrpcCall_Failure() {
        // Given
        String service = "divider";
        String operation = "divide";
        Supplier<Double> supplier = () -> {
            throw new RuntimeException("Division by zero");
        };

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            gatewayCustomMetrics.recordGrpcCall(service, operation, supplier);
        });

        // Verify error counter was incremented
        Counter counter = meterRegistry.find("calculator.grpc.client.calls.errors")
                .tag("service", service)
                .tag("method", operation)
                .counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());

        // Verify timer was still recorded even on error
        Timer timer = meterRegistry.find("calculator.grpc.client.calls.duration")
                .tag("service", service)
                .tag("method", operation)
                .timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
    }

    @Test
    @DisplayName("Should record multiple calls to same service")
    void testMultipleCalls_SameService() {
        // Given
        String service = "adder";
        String operation = "add";

        // When
        gatewayCustomMetrics.recordGrpcCall(service, operation, () -> 10.0);
        gatewayCustomMetrics.recordGrpcCall(service, operation, () -> 20.0);
        gatewayCustomMetrics.recordGrpcCall(service, operation, () -> 30.0);

        // Then
        Counter counter = meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("service", service)
                .tag("method", operation)
                .counter();
        assertNotNull(counter);
        assertEquals(3.0, counter.count());

        Timer timer = meterRegistry.find("calculator.grpc.client.calls.duration")
                .tag("service", service)
                .tag("method", operation)
                .timer();
        assertNotNull(timer);
        assertEquals(3, timer.count());
    }

    @Test
    @DisplayName("Should record metrics for different services independently")
    void testMultipleServices() {
        // When
        gatewayCustomMetrics.recordGrpcCall("adder", "add", () -> 15.0);
        gatewayCustomMetrics.recordGrpcCall("subtractor", "subtract", () -> 5.0);
        gatewayCustomMetrics.recordGrpcCall("multiplier", "multiply", () -> 50.0);
        gatewayCustomMetrics.recordGrpcCall("divider", "divide", () -> 2.0);

        // Then - Verify each service has its own metrics
        assertNotNull(meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("service", "adder").counter());
        assertNotNull(meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("service", "subtractor").counter());
        assertNotNull(meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("service", "multiplier").counter());
        assertNotNull(meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("service", "divider").counter());
    }

    @Test
    @DisplayName("Should track success and error counts separately")
    void testSuccessAndErrorCountsSeparate() {
        // Given
        String service = "adder";
        String operation = "add";

        // When
        gatewayCustomMetrics.recordGrpcCall(service, operation, () -> 10.0);
        gatewayCustomMetrics.recordGrpcCall(service, operation, () -> 20.0);
        
        try {
            gatewayCustomMetrics.recordGrpcCall(service, operation, () -> {
                throw new RuntimeException("Error");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        // Then
        Counter successCounter = meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("service", service)
                .tag("method", operation)
                .counter();
        assertEquals(3.0, successCounter.count());

        Counter errorCounter = meterRegistry.find("calculator.grpc.client.calls.errors")
                .tag("service", service)
                .tag("method", operation)
                .counter();
        assertEquals(1.0, errorCounter.count());
    }

    @Test
    @DisplayName("Should measure execution time accurately")
    void testTimerMeasuresExecutionTime() throws InterruptedException {
        // Given
        String service = "test-service";
        String operation = "test-operation";
        Supplier<Double> slowSupplier = () -> {
            try {
                Thread.sleep(100); // Simulate slow operation
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 42.0;
        };

        // When
        gatewayCustomMetrics.recordGrpcCall(service, operation, slowSupplier);

        // Then
        Timer timer = meterRegistry.find("calculator.grpc.client.calls.duration")
                .tag("service", service)
                .tag("method", operation)
                .timer();
        
        assertNotNull(timer);
        assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) >= 100);
    }

    @Test
    @DisplayName("Should return supplier result on success")
    void testReturnValue() {
        // Given
        Double expectedResult = 123.456;
        Supplier<Double> supplier = () -> expectedResult;

        // When
        Double result = gatewayCustomMetrics.recordGrpcCall("service", "op", supplier);

        // Then
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Should propagate exception from supplier")
    void testExceptionPropagation() {
        // Given
        RuntimeException expectedException = new RuntimeException("Test error");
        Supplier<Double> supplier = () -> {
            throw expectedException;
        };

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            gatewayCustomMetrics.recordGrpcCall("service", "op", supplier);
        });

        assertEquals(expectedException, thrown);
    }

    @Test
    @DisplayName("Should record metrics for all four operation types")
    void testAllOperationTypes() {
        // When
        gatewayCustomMetrics.recordGrpcCall("adder", "add", () -> 15.0);
        gatewayCustomMetrics.recordGrpcCall("subtractor", "subtract", () -> 5.0);
        gatewayCustomMetrics.recordGrpcCall("multiplier", "multiply", () -> 50.0);
        gatewayCustomMetrics.recordGrpcCall("divider", "divide", () -> 2.0);

        // Then
        assertEquals(1.0, meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("method", "add")
                .tag("service", "adder")
                .counter().count());
        assertEquals(1.0, meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("method", "subtract")
                .tag("service", "subtractor")
                .counter().count());
        assertEquals(1.0, meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("method", "multiply")
                .tag("service", "multiplier")
                .counter().count());
        assertEquals(1.0, meterRegistry.find("calculator.grpc.client.calls.total")
                .tag("method", "divide")
                .tag("service", "divider")
                .counter().count());
    }
}
