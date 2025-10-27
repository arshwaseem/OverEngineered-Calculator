package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MultiplierServiceTests {

    private final SimpleMeterRegistry meterRegistry =  new SimpleMeterRegistry();
    private final MultiplierService multiplierService = new MultiplierService(meterRegistry);

    @Test
    public void multiply_ShouldMultiplyCorrectly() {
        double numA = 5.0;
        double numB = 6.0;
        Assertions.assertEquals(30.0,multiplierService.Multiply(numA,numB));
    }

    @Test void multiply_ShouldMultiplyCorrectly_WithZero() {
        double numA = 10.0;
        double numB = 0.0;
        Assertions.assertEquals(0.0,multiplierService.Multiply(numA,numB));
    }

    @Test
    void multiply_ShouldHandleOverflow() {
        double numA = Double.MAX_VALUE;
        double numB = Double.MAX_VALUE;

        Assertions.assertEquals(Double.POSITIVE_INFINITY,multiplierService.Multiply(numA,numB));
    }

    @Test
    void multiply_ShouldHandleNegativeNumbers() {
        double numA = -15.0;
        double numB = 2.0;

        Assertions.assertEquals(-30.0,multiplierService.Multiply(numA,numB));
    }
}
