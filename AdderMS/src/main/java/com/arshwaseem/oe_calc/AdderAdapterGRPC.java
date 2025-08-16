package com.arshwaseem.oe_calc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class AdderAdapterGRPC extends AdderServiceGrpc.AdderServiceImplBase {

    private static final Logger log = LogManager.getLogger(AdderAdapterGRPC.class);
    private final AdderService adderService;

    public AdderAdapterGRPC(AdderService adderService) {
        this.adderService = adderService;
    }

    @Override
    public void add(AddRequest request, StreamObserver<AddResponse> responseObserver) {

        try{
            double numA =  request.getNumA();
            double numB = request.getNumB();
            double result = adderService.Add(numA, numB);

            AddResponse response = AddResponse.newBuilder().setResult(result).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in AdderAdapterGRPC", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
