package com.arshwaseem.oe_calc;

import org.springframework.stereotype.Service;

@Service
public class MultiplierService implements MultiplierUseCases{
    public double Multiply(double numA, double numB){
        return numA*numB;
    }
    public void saveHistory(double res){
        System.out.println("Saving History results\n");
    }
}
