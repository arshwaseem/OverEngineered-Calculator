package com.arshwaseem.oe_calc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationsGRPCAdapter implements OperationsClientPort{

    private final OperationServiceGrpc.OperationServiceBlockingStub addStub;
    private final OperationServiceGrpc.OperationServiceBlockingStub subtractStub;
    private final OperationServiceGrpc.OperationServiceBlockingStub multiplyStub;
    private final OperationServiceGrpc.OperationServiceBlockingStub divideStub;

    @Override
    public Double Add(Double numA, Double numB) {
        OperationRequest addRequest = OperationRequest.newBuilder().setNumA(numA).setNumB(numB).build();
        return addStub.add(addRequest).getResult();
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
