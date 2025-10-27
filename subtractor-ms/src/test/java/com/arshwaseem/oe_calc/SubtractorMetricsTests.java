package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class SubtractorMetricsTests {

    private SubtractorService subtractorService;

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        this.meterRegistry = new SimpleMeterRegistry();
        this.subtractorService = new SubtractorService(meterRegistry);
    }

    @Test
    void multiply_counterIncremented() {

        subtractorService.Subtract(5.0,4.1);

        Assertions.assertTrue(Objects.requireNonNull(meterRegistry.find("calculator.operations.total").tag("operation", "subtract").counter()).count() > 0);

    }

    @Test
    void multiply_timerRecorded (){
        subtractorService.Subtract(5.0,4.1);

        Assertions.assertNotNull(meterRegistry.find("calculator.operations.duration").tag("operation", "subtract").timer());
    }

}
