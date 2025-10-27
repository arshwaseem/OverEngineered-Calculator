package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubtractorService implements SubtractorUseCases {

    private static final Logger log = LoggerFactory.getLogger(SubtractorService.class);

    private final MeterRegistry meterRegistry;

    public SubtractorService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public double Subtract(double numA, double numB) {

        return Timer.builder("calculator.operations.duration")
                .description("Duration of Calculator Operations")
                .tag("operation", "subtract")
                .register(meterRegistry)
                .record(() -> {
                    try{
                        Counter.builder("calculator.operations.total")
                                .description("Total Calculator Operations")
                                .tag("operation", "subtract")
                                .register( meterRegistry)
                                .increment();
                        if(numB < 0){
                            return numA + numB;
                        }
                        return numA - numB;
                    } catch(Exception e){
                        log.error("Subtraction Failed: {}", e.getMessage());
                        Counter.builder("calculator.operations.errors")
                                .description("Calculator Operations Errors")
                                .tag("operation", "subtract")
                                .tag("error", e.getClass().getSimpleName())
                                .register( meterRegistry)
                                .increment();
                        return 0;
                    }
                });
    }

    public void saveHistory(double res) {
        System.out.println("Saving History results\n");
    }
}
