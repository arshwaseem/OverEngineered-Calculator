package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MultiplierServiceTests {

    private MultiplierService multiplierService = new MultiplierService();

    @Test
    public void testMultiply() {
        double numA = Math.random();
        double numB = Math.random();
        Assertions.assertEquals((numA*numB),multiplierService.Multiply(numA,numB));
    }
}
