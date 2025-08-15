package com.arshwaseem.oe_calc;

import org.springframework.web.bind.annotation.*;

@RestController
public class AdderAdapter {
    private final AdderService adderService;
    public AdderAdapter(AdderService adderService) {
        this.adderService = adderService;
    }

    @PostMapping("/add")
    public double Add(@RequestBody double numA, @RequestBody double numB) {
        return adderService.Add(numA, numB);
    }
}