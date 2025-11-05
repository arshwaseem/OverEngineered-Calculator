package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubtractorAdapterREST {
    private static final Logger log = LoggerFactory.getLogger(SubtractorAdapterREST.class);
    private final SubtractorService subtractorService;
    public SubtractorAdapterREST(SubtractorService subtractorService) {
        this.subtractorService = subtractorService;
    }

    @PostMapping("/subtract")
    public double Subtract(double numA, double numB) {
        try{
            return subtractorService.Subtract(numA, numB);
        } catch(Exception e){
            log.error("error while trying to subtract: {}",e.getMessage());
            return Double.NaN;
        }
    }
}
