package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class AdderServiceTests {

    private AdderService adderService;

    @BeforeEach
    void setUp() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        this.adderService = new AdderService(meterRegistry);
    }


    @Test
    void add_shouldReturnCorrectSum(){

        double numA = 5.0;
        double numB = 10.0;

        double result = adderService.Add(numA, numB);

        Assertions.assertEquals(15.0, result);
    }

    @Test
    void add_shouldReturnCorrectSumWithNegativeNumbers(){

        double numA = 5.0;
        double numB = -10.0;
        double result = adderService.Add(numA, numB);

        Assertions.assertEquals(-5.0, result);

    }

    @Test
    void add_shouldReturnCorrectSumWithZeroNumbers(){
        double numA = 5.0;
        double numB = 0.0;
        double result = adderService.Add(numA, numB);

        Assertions.assertEquals(5.0, result);
    }

    @Test
    void add_shouldHandleOverflow(){
        double numA = Double.MAX_VALUE;
        double numB = Double.MAX_VALUE;
        double result = adderService.Add(numA, numB);

        Assertions.assertEquals(Double.POSITIVE_INFINITY, result);
    }
}
