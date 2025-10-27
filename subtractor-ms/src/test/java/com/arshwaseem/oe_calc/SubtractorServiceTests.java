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
    public void subtractor_ShouldSubtractCorrectly() {
        double numA = 5.0;
        double numB = 2.0;
        Assertions.assertEquals(3.0,subtractorService.Subtract(numA,numB));
    }

    @Test
    public void subtractor_ShouldHandleNegativeNumbers(){
        double numA = -5.0;
        double numB = -2.0;

        Assertions.assertEquals(-7.0,subtractorService.Subtract(numA,numB));
    }

    @Test
    public void subtractor_ShouldHandleZeroNumbers(){
        double numA = 5;
        double numB = 0;

        Assertions.assertEquals(5,subtractorService.Subtract(numA,numB));
    }

    @Test
    public void subtractor_ShouldHandleZeroAsFirstNumber(){
        double numA = 0;
        double numB = 5;

        Assertions.assertEquals(-5,subtractorService.Subtract(numA,numB));
    }
}