package com.arshwaseem.oe_calc;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

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
