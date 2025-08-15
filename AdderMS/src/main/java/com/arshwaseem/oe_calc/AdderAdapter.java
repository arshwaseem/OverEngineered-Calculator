package com.arshwaseem.oe_calc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdderAdapter {
    private final AdderService adderService;
    public AdderAdapter(AdderService adderService) {
        this.adderService = adderService;
    }

    @GetMapping("/add")
    public double Add(@RequestParam Double numA, @RequestParam Double numB) {
        return adderService.Add(numA, numB);
    }
}