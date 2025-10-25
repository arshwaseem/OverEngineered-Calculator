package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MultiplierService implements MultiplierUseCases{
    private static final Logger log = LoggerFactory.getLogger(MultiplierService.class);

    public double Multiply(double numA, double numB){
        try{
            return numA*numB;
        } catch(ArithmeticException ex){
            log.error("Arithmetic Exception",ex);
        }
        return 0;
    }
    public void saveHistory(double res){
        System.out.println("Saving History results\n");
    }
}
