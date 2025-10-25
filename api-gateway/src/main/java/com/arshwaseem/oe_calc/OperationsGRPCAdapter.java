package com.arshwaseem.oe_calc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OperationsGRPCAdapter implements OperationsClientPort{

    private final OperationServiceGrpc.OperationServiceBlockingStub addStub;
    private final OperationServiceGrpc.OperationServiceBlockingStub subtractStub;
    private final OperationServiceGrpc.OperationServiceBlockingStub multiplyStub;
    private final OperationServiceGrpc.OperationServiceBlockingStub divideStub;
    private final GrpcConfiguration grpcConfiguration;
    private final GatewayCustomMetrics gatewayCustomMetrics;

    @Override
    public Double Add(Double numA, Double numB) {

        return gatewayCustomMetrics.recordGrpcCall(
                "adder","add",
                () -> {
                    try {
                        OperationRequest addRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
                        Double result = addStub.add(addRequest).getResult();
                        return result;
                    } catch (Exception e) {
                        log.error("Error performing grpc add: {}", e.getMessage());
                        throw e;
                    }
                }
        );
    }

    @Override
    public Double Subtract(Double numA, Double numB) {
        return gatewayCustomMetrics.recordGrpcCall(
                "subtractor", "subtract",
                () -> {
                    try {
                        OperationRequest subtractRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
                        Double result = subtractStub.subtract(subtractRequest).getResult();
                        return result;
                    } catch (Exception e) {
                        log.error("Error performing grpc subtract: {}", e.getMessage());
                        throw e;
                    }
                }
        );
    }

    @Override
    public Double Multiply(Double numA, Double numB) {
        return gatewayCustomMetrics.recordGrpcCall(
                "multiplier", "multiply",
                () -> {
                    try {
                        OperationRequest multiplyRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
                        Double result = multiplyStub.multiply(multiplyRequest).getResult();
                        return result;
                    } catch (Exception e) {
                        log.error("Error performing grpc multiply: {}", e.getMessage());
                        throw e;
                    }
                }
        );
    }

    @Override
    public Double Divide(Double numA, Double numB) {
        return gatewayCustomMetrics.recordGrpcCall(
                "divider", "divide",
                () -> {
                    try {
                        OperationRequest divideRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
                        Double result = divideStub.divide(divideRequest).getResult();
                        return result;
                    } catch (Exception e) {
                        log.error("Error performing grpc divide: {}", e.getMessage());
                        throw e;
                    }
                }
        );
    }
}
