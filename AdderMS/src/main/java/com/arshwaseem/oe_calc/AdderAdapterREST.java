package com.arshwaseem.oe_calc;

import org.springframework.web.bind.annotation.*;

@RestController
public class AdderAdapterREST {
    private final AdderService adderService;
    public AdderAdapterREST(AdderService adderService) {
        this.adderService = adderService;
    }

    @PostMapping("/add")
    public double Add(@RequestBody AddRequestDTO addRequestDTO) {
        return adderService.Add(addRequestDTO.getNumA(), addRequestDTO.getNumB());
    }
}