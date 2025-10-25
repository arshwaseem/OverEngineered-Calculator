package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MultiplierService implements MultiplierUseCases{
    private static final Logger log = LoggerFactory.getLogger(MultiplierService.class);

    private final MeterRegistry meterRegistry;

    public MultiplierService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public double Multiply(double numA, double numB){

        return Timer.builder("calculator.operations.duration")
                .description("Duration of Calculator Operations")
                .tag("operation", "multiply")
                .register(meterRegistry)
                .record(() -> {
                    try{
                        Counter.builder("calculator.operations.total")
                                .description("Total Calculator Operations")
                                .tag("operation", "multiply")
                                .register(meterRegistry)
                                .increment();
                        return numA*numB;
                    } catch(ArithmeticException ex){
                        log.error("Arithmetic Exception : {}",ex.getMessage());
                        Counter.builder("calculator.operations.errors")
                                .description("Calculator Operations Errors")
                                .tag("operation", "multiply")
                                .tag("error", ex.getClass().getSimpleName())
                                .register(meterRegistry)
                                .increment();
                    }
                    return 0;
                });
    }
}
