package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubtractorService implements SubtractorUseCases {

    private static final Logger log = LoggerFactory.getLogger(SubtractorService.class);

    public double Subtract(double numA, double numB) {
        try{
            return numA - numB;
        } catch (Exception e) {
            log.error("Error:",e);
        }
        return 0;
    }

    public void saveHistory(double res) {
        System.out.println("Saving History results\n");
    }
}
