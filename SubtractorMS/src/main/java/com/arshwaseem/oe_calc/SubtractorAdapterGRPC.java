package com.arshwaseem.oe_calc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubtractorAdapterGRPC extends SubtractorServiceGrpc.SubtractorServiceImplBase{

    private static final Logger log = LoggerFactory.getLogger(SubtractorAdapterGRPC.class);
    private final SubtractorService  subtractorService;

    public SubtractorAdapterGRPC(SubtractorService subtractorService){
        this.subtractorService = subtractorService;
    }

    @Override
    public void subtract (SubtractRequest request, StreamObserver<SubtractResponse> responseObserver) {
        try{
            double numA = request.getNumA();
            double numB = request.getNumB();

            double result = subtractorService.Subtract(numA, numB);

            SubtractResponse response = SubtractResponse.newBuilder().setResult(result).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e){
            log.error("Error in SubtractorAdapterGRPC", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
