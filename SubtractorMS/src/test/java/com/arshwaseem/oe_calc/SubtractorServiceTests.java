package com.arshwaseem.oe_calc;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubtractorServiceTests {

    private SubtractorService subtractorService = new SubtractorService();

    @Test
    public void SubtractorServiceTest() {
        double numA = Math.random();
        double numB = Math.random();
        Assertions.assertEquals((numA-numB),subtractorService.Subtract(numA,numB));
    }
}
