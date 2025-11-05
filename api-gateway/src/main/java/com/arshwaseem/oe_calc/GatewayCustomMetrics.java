package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class GatewayCustomMetrics {

    private final MeterRegistry meterRegistry;

    public GatewayCustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public <T> T recordGrpcCall(String service, String method, Supplier<T> call){
        return Timer.builder("calculator.grpc.client.calls.duration")
                .tag("service", service)
                .tag("method", method)
                .register(meterRegistry)
                .record(() -> {
                    try{
                        Counter.builder("calculator.grpc.client.calls.total")
                                .tag("service", service)
                                .tag("method", method)
                                .register(meterRegistry)
                                .increment();

                        return call.get();
                    } catch(Exception e){
                        log.error("Error during grpc call: {}", e.getMessage());
                        Counter.builder("calculator.grpc.client.calls.errors")
                                .tag("service", service)
                                .tag("method", method)
                                .tag("error", e.getClass().getSimpleName())
                                .register(meterRegistry)
                                .increment();

                        throw e;
                    }
                });
    }
}
