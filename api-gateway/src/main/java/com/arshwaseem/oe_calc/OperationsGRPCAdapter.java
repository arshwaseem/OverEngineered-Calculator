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

    @Override
    public Double Add(Double numA, Double numB) {
        log.debug("GPRC Add request being performed");
        log.debug("Adder Host is: {}",grpcConfiguration.getAddHost());
        log.debug("Adder Port is: {}",grpcConfiguration.getAddPort());
        try {
            OperationRequest addRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
            Double result = addStub.add(addRequest).getResult();
            log.debug("GPRC Add result is: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error performing grpc add: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public Double Subtract(Double numA, Double numB) {
        OperationRequest subtractRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
        return subtractStub.subtract(subtractRequest).getResult();
    }

    @Override
    public Double Multiply(Double numA, Double numB) {
        OperationRequest multiplyRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
        return multiplyStub.multiply(multiplyRequest).getResult();
    }

    @Override
    public Double Divide(Double numA, Double numB) {
        OperationRequest divideRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
        return divideStub.divide(divideRequest).getResult();
    }
}
