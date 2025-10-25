package com.arshwaseem.oe_calc;


import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
public class SubtractorServiceTests {

    private SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private SubtractorService subtractorService = new SubtractorService(meterRegistry);

    @Test
    public void SubtractorServiceTest() {
        double numA = Math.random();
        double numB = Math.random();
        Assertions.assertEquals((numA-numB),subtractorService.Subtract(numA,numB));
    }
}
