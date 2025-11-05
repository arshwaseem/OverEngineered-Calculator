package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdderAdapterREST {

    private final AdderService adderService;

    private static final Logger log = LoggerFactory.getLogger(AdderAdapterREST.class);

    public AdderAdapterREST(AdderService adderService) {
        this.adderService = adderService;
    }

    @PostMapping("/add")
    public double Add(@RequestBody AddRequestDTO addRequestDTO) {
        try{
            return adderService.Add(addRequestDTO.getNumA(), addRequestDTO.getNumB());
        } catch(Exception e){
            log.error("Error performing add: {}", e.getMessage());
            return Double.NaN;
        }
    }
}