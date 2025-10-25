package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.stereotype.Service;

@Service
public class AdderService implements AdderUseCases{

    private static final Logger log = LoggerFactory.getLogger(AdderService.class);

    public double Add(double numA, double numB){
        try{
            return numA + numB;
        } catch(Exception e){
            log.error("Error in MultiplierAdapterGRPC", e);
        }
        return 0;
    }

    public void saveHistory(double res) {
        System.out.println("Saving history results\n");
    }
}