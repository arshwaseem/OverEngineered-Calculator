package com.arshwaseem.oe_calc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

@Configuration
@Data
public class GrpcConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GrpcConfiguration.class);

    @Autowired
    private Tracer tracer;

    @Value("${grpc.add.host:add-service}")
    private String addHost;
    @Value("${grpc.add.port:50051}")
    private int addPort;
    @Value("${grpc.subtract.host:subtract-service}")
    private String subtractHost;
    @Value("${grpc.subtract.port:50051}")
    private int subtractPort;
    @Value("${grpc.multiply.host:multiply-service}")
    private String multiplyHost;
    @Value("${grpc.multiply.port:50051}")
    private int multiplyPort;
    @Value("${grpc.divide.host:divide-service}")
    private String divideHost;
    @Value("${grpc.divide.port:50051}")
    private int dividePort;

    private ManagedChannel createOptimizedChannel(String host, int port){

        log.info("Creating gRPC stub to host: {} port: {}", host, port);

        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .idleTimeout(5, TimeUnit.MINUTES)
                .maxInboundMessageSize(4*1024 * 1024)
                .directExecutor()
                .build();

    }

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel addChannel(){
        return createOptimizedChannel(addHost, addPort);
    }

    @Bean
    public ManagedChannel subtractChannel(){
        return createOptimizedChannel(subtractHost, subtractPort);
    }

    @Bean
    public ManagedChannel multiplyChannel(){
        return createOptimizedChannel(multiplyHost, multiplyPort);
    }

    @Bean
    public ManagedChannel divideChannel(){
        return createOptimizedChannel(divideHost, dividePort);
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub addStub(ManagedChannel addChannel){
        return OperationServiceGrpc.newBlockingStub(addChannel)
                .withInterceptors(new TracingClientInterceptor(tracer));
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub subtractStub(ManagedChannel subtractChannel){
        return OperationServiceGrpc.newBlockingStub(subtractChannel)
                .withInterceptors(new TracingClientInterceptor(tracer));
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub divideStub(ManagedChannel divideChannel){
        return OperationServiceGrpc.newBlockingStub(divideChannel)
                .withInterceptors(new TracingClientInterceptor(tracer));
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub multiplyStub(ManagedChannel multiplyChannel){
        return OperationServiceGrpc.newBlockingStub(multiplyChannel)
                .withInterceptors(new TracingClientInterceptor(tracer));
    }

    private static class TracingClientInterceptor implements io.grpc.ClientInterceptor {
        private final Tracer tracer;

        public TracingClientInterceptor(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        public <ReqT, RespT> io.grpc.ClientCall<ReqT, RespT> interceptCall(
                io.grpc.MethodDescriptor<ReqT, RespT> method,
                io.grpc.CallOptions callOptions,
                io.grpc.Channel next) {

            // Skip tracing if tracer is not available or no active span
            if (tracer == null || tracer.currentSpan() == null) {
                return next.newCall(method, callOptions);
            }

            try {
                var currentSpan = tracer.currentSpan();
                var currentTraceContext = tracer.currentTraceContext();

                // Safely check if we have OTel context
                if (!(currentTraceContext instanceof OtelCurrentTraceContext)) {
                    return next.newCall(method, callOptions);
                }

                var otelContext = ((OtelCurrentTraceContext) currentTraceContext).context();
                if (otelContext == null) {
                    return next.newCall(method, callOptions);
                }

                // Wrap the call to propagate context
                return new io.grpc.ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                        next.newCall(method, callOptions)) {

                    @Override
                    public void start(Listener<RespT> responseListener, io.grpc.Metadata headers) {
                        try {
                            // Inject trace context into gRPC metadata
                            var span = Span.fromContext((Context) otelContext);
                            var spanContext = span.getSpanContext();

                            if (spanContext.isValid()) {
                                var traceId = spanContext.getTraceId();
                                var spanId = spanContext.getSpanId();

                                // Add W3C trace context headers
                                headers.put(
                                        io.grpc.Metadata.Key.of("traceparent", io.grpc.Metadata.ASCII_STRING_MARSHALLER),
                                        String.format("00-%s-%s-01", traceId, spanId)
                                );
                            }
                        } catch (Exception e) {
                            // Log but don't fail the call if tracing fails
                            // In production, you'd use proper logging
                            System.err.println("Failed to inject trace context: " + e.getMessage());
                        }

                        super.start(responseListener, headers);
                    }
                };

            } catch (Exception e) {
                // If anything goes wrong with tracing, just proceed without it
                return next.newCall(method, callOptions);
            }
        }
    }
}
