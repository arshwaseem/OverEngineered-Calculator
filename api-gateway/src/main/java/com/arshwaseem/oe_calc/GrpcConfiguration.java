package com.arshwaseem.oe_calc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class GrpcConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GrpcConfiguration.class);

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
}
