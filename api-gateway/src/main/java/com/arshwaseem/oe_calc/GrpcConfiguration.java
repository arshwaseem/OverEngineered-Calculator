package com.arshwaseem.oe_calc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

@Configuration
@Data
public class GrpcConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GrpcConfiguration.class);

    @Autowired
    private OpenTelemetry openTelemetry;

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

    @Bean
    public GrpcTelemetry grpcTelemetry(){
        return GrpcTelemetry.create(openTelemetry);
    }

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
                .intercept(grpcTelemetry().newClientInterceptor())
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
        return OperationServiceGrpc.newBlockingStub(addChannel);
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub subtractStub(ManagedChannel subtractChannel){
        return OperationServiceGrpc.newBlockingStub(subtractChannel);
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub divideStub(ManagedChannel divideChannel){
        return OperationServiceGrpc.newBlockingStub(divideChannel);
    }

    @Bean
    public OperationServiceGrpc.OperationServiceBlockingStub multiplyStub(ManagedChannel multiplyChannel){
        return OperationServiceGrpc.newBlockingStub(multiplyChannel);
    }
}
