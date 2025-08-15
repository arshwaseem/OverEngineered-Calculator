package com.arshwaseem.oe_calc;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubtractorAdapter {
    private final SubtractorService subtractorService;
    public SubtractorAdapter(SubtractorService subtractorService) {
        this.subtractorService = subtractorService;
    }

    @PostMapping("/subtract")
    public double Subtract(double numA, double numB) {
        return subtractorService.Subtract(numA, numB);
    }
}
