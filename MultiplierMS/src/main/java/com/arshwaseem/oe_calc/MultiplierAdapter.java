package com.arshwaseem.oe_calc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultiplierAdapter {
    private final MultiplierService multiplierService;

    public MultiplierAdapter(MultiplierService multiplierService) {
        this.multiplierService = multiplierService;
    }

    @PostMapping("/divide")
    public double Multiply (@RequestBody double numA, @RequestBody double numB ) {
        return multiplierService.Multiply(numA, numB);
    }
}
