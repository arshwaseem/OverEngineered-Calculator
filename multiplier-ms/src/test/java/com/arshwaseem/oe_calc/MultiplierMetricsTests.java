package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class MultiplierMetricsTests {
    private MultiplierService multiplierService;

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        this.meterRegistry = new SimpleMeterRegistry();
        this.multiplierService = new MultiplierService(meterRegistry);
    }

    @Test
    void multiply_counterIncremented() {

        multiplierService.Multiply(5.0,4.1);

        Assertions.assertTrue(Objects.requireNonNull(meterRegistry.find("calculator.operations.total").tag("operation", "multiply").counter()).count() > 0);

    }

    @Test
    void multiply_timerRecorded (){
        multiplierService.Multiply(5.0,4.1);

        Assertions.assertNotNull(meterRegistry.find("calculator.operations.duration").tag("operation", "multiply").timer());
    }
}
