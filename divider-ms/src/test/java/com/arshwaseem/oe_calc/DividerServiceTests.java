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
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
public class DividerServiceTests {
    private SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private DividerSevice divider = new DividerSevice(meterRegistry);

    @Test
    public void DividerTest() {
        double numA = 12;
        double numB = 4;

        Assertions.assertEquals((numA/numB),divider.Divider(numA,numB));
    }
}
