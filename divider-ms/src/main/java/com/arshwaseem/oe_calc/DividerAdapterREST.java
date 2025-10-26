package com.arshwaseem.oe_calc;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DividerAdapterREST {
    private final DividerService dividerService;
    public DividerAdapterREST(DividerService dividerService) {
        this.dividerService = dividerService;
    }

    @PostMapping("/divide")
    public double Divide(@RequestBody double numA, @RequestBody double numB){
        return dividerService.Divider(numA, numB);
    }
}
