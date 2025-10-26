package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MultiplierServiceTests {

    private final SimpleMeterRegistry meterRegistry =  new SimpleMeterRegistry();
    private final MultiplierService multiplierService = new MultiplierService(meterRegistry);

    @Test
    public void testMultiply() {
        double numA = Math.random();
        double numB = Math.random();
        Assertions.assertEquals((numA*numB),multiplierService.Multiply(numA,numB));
    }
}
