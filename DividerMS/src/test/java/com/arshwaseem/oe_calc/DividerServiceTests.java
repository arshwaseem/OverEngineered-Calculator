package com.arshwaseem.oe_calc;

import com.google.apps.card.v1.Divider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DividerServiceTests {
    private DividerSevice divider = new DividerSevice();

    @Test
    public void DividerTest() {
        double numA = 12;
        double numB = 4;

        Assertions.assertEquals((numA+numB),divider.Divider(numA,numB));
    }
}
