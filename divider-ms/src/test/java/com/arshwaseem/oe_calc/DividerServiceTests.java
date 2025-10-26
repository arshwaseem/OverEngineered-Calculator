package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.exception.DivideException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DividerServiceTests {

    private DividerService dividerService;

    @BeforeEach
    public void setup() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        dividerService = new DividerService(registry);
    }

    @Test
    public void divider_shouldDivideCorrectly() {
        double numA = 12;
        double numB = 4;

        Assertions.assertEquals((numA/numB),dividerService.Divider(numA,numB));
    }

    @Test
    public void divider_shouldThrowDivideByZeroException() {

        double numA = 12;
        double numB = 0;

        Assertions.assertThrows(DivideException.class, () -> {
            dividerService.Divider(numA,numB);
        });
    }
}
