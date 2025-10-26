package com.arshwaseem.oe_calc;


import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AdderServiceTests {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private final AdderService adderService = new AdderService(meterRegistry);

    @Test
    void add() {
        double numA = Math.random();
        double numB = Math.random();

        Assertions.assertEquals((numA+numB),adderService.Add(numA,numB));
    }

}
