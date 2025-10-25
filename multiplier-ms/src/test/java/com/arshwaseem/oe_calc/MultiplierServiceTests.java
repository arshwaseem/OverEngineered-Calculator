package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
public class MultiplierServiceTests {

    private MultiplierService multiplierService = new MultiplierService();

    @Test
    public void testMultiply() {
        double numA = Math.random();
        double numB = Math.random();
        Assertions.assertEquals((numA*numB),multiplierService.Multiply(numA,numB));
    }
}
