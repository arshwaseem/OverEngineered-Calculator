package com.arshwaseem.oe_calc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplierAdapterGRPC extends MultiplierServiceGrpc.MultiplierServiceImplBase{

    private static final Logger log = LoggerFactory.getLogger(MultiplierAdapterGRPC.class);
    private final MultiplierService multiplierService;

    public MultiplierAdapterGRPC(MultiplierService multiplierService) {
        this.multiplierService = multiplierService;
    }

    @Override
    public void multiply (MultiplyRequest request, StreamObserver<MultiplyResponse> responseObserver) {

        try{
            double numA = request.getNumA();
            double numB = request.getNumB();
            double result = multiplierService.Multiply(numA, numB);

            MultiplyResponse response = MultiplyResponse.newBuilder().setResult(result).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in MultiplierAdapterGRPC", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
