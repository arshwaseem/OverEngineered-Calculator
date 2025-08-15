package com.arshwaseem.oe_calc;

import org.springframework.stereotype.Service;

@Service
public class SubtractorService implements SubtractorUseCases {

    public double Subtract(double numA, double numB) {
        return numA - numB;
    }

    public void saveHistory(double res) {
        System.out.println("Saving History results\n");
    }
}
