package com.arshwaseem.oe_calc;

import org.springframework.stereotype.Service;

@Service
public class DividerSevice implements DividerUseCases {
    public double Divider(double numA, double numB) {
        return numA / numB;
    }

    public void saveHistory(double res) {
        System.out.println("Saving history results\n");
    }
}
