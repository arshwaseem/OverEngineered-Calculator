package com.arshwaseem.oe_calc;


import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class AdderMetricesTests {

    private AdderService adderService;

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        this.meterRegistry = new SimpleMeterRegistry();
        this.adderService = new AdderService(meterRegistry);
    }

    @Test
    void add_counterIncremented() {

        adderService.Add(5.0,4.1);

        Assertions.assertTrue(Objects.requireNonNull(meterRegistry.find("calculator.operations.total").tag("operation", "add").counter()).count() > 0);

    }

    @Test
    void add_timerRecorded (){
        adderService.Add(5.0,4.1);

        Assertions.assertNotNull(meterRegistry.find("calculator.operations.duration").tag("operation", "add").timer());
    }
    
}
