package com.arshwaseem.oe_calc;

import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.stereotype.Service;

@Service
public class AdderService implements AdderUseCases{

    public double Add(double numA, double numB) {
        return numA + numB;
    }

    public void saveHistory(double res) {
        System.out.println("Saving history results\n");
    }
}