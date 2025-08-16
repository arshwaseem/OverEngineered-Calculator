package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DividerSevice implements DividerUseCases {
    private static final Logger log = LoggerFactory.getLogger(DividerSevice.class);

    public double Divider(double numA, double numB) {
        try{
            return numA / numB;
        } catch (Exception e) {
            log.error("Error in DividerAdapterGRPC", e);
        }
        return 0;
    }

    public void saveHistory(double res) {
        System.out.println("Saving history results\n");
    }
}
