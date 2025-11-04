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

    public OperationServiceGrpc.OperationServiceBlockingStub newStub(String host, int port){
        log.info("Creating gRPC stub to host: {} port: {}", host, port);
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .intercept(new TracingClientInterceptor(tracer))
                .build();

        return OperationServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub addStub(){
        return newStub(addHost,addPort);
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub subtractStub(){
        return newStub(subtractHost,subtractPort);
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub divideStub(){
        return newStub(divideHost,dividePort);
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub multiplyStub(){
        return newStub(multiplyHost,multiplyPort);
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

            // Create a new span for this gRPC call
            var currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                // Get the OpenTelemetry context
                var otelContext = ((OtelCurrentTraceContext) tracer.currentTraceContext())
                        .context();

                // Wrap the call to propagate context
                return new io.grpc.ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                        next.newCall(method, callOptions)) {

                    @Override
                    public void start(Listener<RespT> responseListener, io.grpc.Metadata headers) {
                        // Inject trace context into gRPC metadata
                        if (otelContext != null) {
                            var span = Span.fromContext((Context) otelContext);
                            var traceId = span.getSpanContext().getTraceId();
                            var spanId = span.getSpanContext().getSpanId();

                            // Add W3C trace context headers
                            headers.put(
                                    io.grpc.Metadata.Key.of("traceparent", io.grpc.Metadata.ASCII_STRING_MARSHALLER),
                                    String.format("00-%s-%s-01", traceId, spanId)
                            );
                        }

                        super.start(responseListener, headers);
                    }
                };
            }

            return next.newCall(method, callOptions);
        }
    }
}
