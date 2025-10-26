package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class DividerMetricsTests {

    private DividerService dividerService;

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        this.meterRegistry = new SimpleMeterRegistry();
        this.dividerService = new DividerService(meterRegistry);
    }

    @Test
    void divide_counterIncremented() {

        dividerService.Divider(5.0,4.1);

        Assertions.assertTrue(Objects.requireNonNull(meterRegistry.find("calculator.operations.total").tag("operation", "divide").counter()).count() > 0);

    }

    @Test
    void divide_timerRecorded (){
        dividerService.Divider(5.0,4.1);

        Assertions.assertNotNull(meterRegistry.find("calculator.operations.duration").tag("operation", "divide").timer());
    }

}
