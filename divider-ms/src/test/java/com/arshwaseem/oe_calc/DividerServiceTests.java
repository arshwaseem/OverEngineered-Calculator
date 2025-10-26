package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DividerServiceTests {
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final DividerSevice divider = new DividerSevice(meterRegistry);

    @Test
    public void DividerTest() {
        double numA = 12;
        double numB = 4;

        Assertions.assertEquals((numA/numB),divider.Divider(numA,numB));
    }
}
