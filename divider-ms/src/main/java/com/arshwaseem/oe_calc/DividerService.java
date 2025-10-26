package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.exception.DivideException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DividerService implements DividerUseCases {
    private static final Logger log = LoggerFactory.getLogger(DividerService.class);

    private final MeterRegistry meterRegistry;

    public DividerService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public double Divider(double numA, double numB) throws DivideException {

        return Timer.builder("calculator.operations.duration")
                .description("Duration of Calculator Operations")
                .tag("operation","divide")
                .register(meterRegistry)
                .record(()-> {
                    try{
                        Counter.builder("calculator.operations.total")
                                .description("Total Calculator Operations")
                                .tag("operation","divide")
                                .register(meterRegistry)
                                .increment();
                        if(numB == 0){
                            throw new DivideException("Attempted to divide by zero, division failed");
                        }
                        return numA/numB;
                    } catch (Exception e){
                        log.error("An error occured while dividing {}", e.getMessage());
                        Counter.builder("calculator.operations.errors")
                                .description("Calculator Operations Errors")
                                .tag("operation","divide")
                                .tag("error",e.getClass().getSimpleName())
                                .register(meterRegistry)
                                .increment();
                        throw e;
                    }
                });
    }
}
