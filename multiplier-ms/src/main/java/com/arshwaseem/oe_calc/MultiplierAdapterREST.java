package com.arshwaseem.oe_calc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultiplierAdapterREST {
    private static final Logger log = LoggerFactory.getLogger(MultiplierAdapterREST.class);
    private final MultiplierService multiplierService;

    public MultiplierAdapterREST(MultiplierService multiplierService) {
        this.multiplierService = multiplierService;
    }

    @PostMapping("/divide")
    public double Multiply (@RequestBody double numA, @RequestBody double numB ) {
        try{
            return multiplierService.Multiply(numA, numB);
        } catch (Exception e) {
            log.error("error while trying to divide: {}",e.getMessage());
            return Double.NaN;
        }
    }
}
