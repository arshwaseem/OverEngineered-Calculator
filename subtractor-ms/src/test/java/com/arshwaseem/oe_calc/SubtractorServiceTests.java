package com.arshwaseem.oe_calc;


import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SubtractorServiceTests {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final SubtractorService subtractorService = new SubtractorService(meterRegistry);

    @Test
    public void SubtractorServiceTest() {
        double numA = Math.random();
        double numB = Math.random();
        Assertions.assertEquals((numA-numB),subtractorService.Subtract(numA,numB));
    }
}
