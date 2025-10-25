package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.stereotype.Service;

@Service
public class AdderService implements AdderUseCases{

    private static final Logger log = LoggerFactory.getLogger(AdderService.class);
    private final MeterRegistry meterRegistry;

    private final Counter operationCounter;
    private final Timer operationTimer;

    public AdderService (MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.operationCounter = Counter.builder("calculatior.operations.total")
                .description("Total number of Addition Operations")
                .tag("operation", "add0")
                .register( meterRegistry);

        this.operationTimer = Timer.builder("calculator.operations.duration")
                .description("Duration of Addition Operations")
                .tag("operation", "add")
                .register( meterRegistry);
    }


    public double Add(double numA, double numB){

        return Timer.builder("calculator.operations.duration")
                .description("Duration of Addition Operations")
                .tag("operation", "add")
                .register(meterRegistry)
                .record(()-> {
                    try {
                        Counter.builder("calculator.operations.total")
                                .description("Total number of Addition Operations")
                                .tag("operation", "add")
                                .register( meterRegistry)
                                .increment();

                        return numA + numB;
                    } catch (Exception e) {
                        log.error("Add operations failed: {}", e.getMessage());
                        Counter.builder("calculator.operations.errors")
                                .description("Number of operation errors")
                                .tag("operation", "add")
                                .tag("error", e.getClass().getSimpleName())
                                .register( meterRegistry)
                                .increment();
                        return 0;
                    }
                });
    }

}