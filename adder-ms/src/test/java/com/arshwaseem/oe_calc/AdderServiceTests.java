package com.arshwaseem.oe_calc;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AdderServiceTests {

    private AdderService adderService = new AdderService();

    @Test
    void add() {
        double numA = Math.random();
        double numB = Math.random();

        Assertions.assertEquals((numA+numB),adderService.Add(numA,numB));
    }

}
