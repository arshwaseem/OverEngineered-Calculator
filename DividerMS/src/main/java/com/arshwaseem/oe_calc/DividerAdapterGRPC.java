package com.arshwaseem.oe_calc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DividerAdapterGRPC extends DividerServiceGrpc.DividerServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(DividerAdapterGRPC.class);
    private final DividerSevice dividerService;

    public DividerAdapterGRPC(DividerSevice dividerService) {
        this.dividerService = dividerService;
    }

    @Override
    public void divide(DivideRequest request, StreamObserver<DivideResponse> responseObserver){
        try{
            double numA = request.getNumA();
            double numB = request.getNumB();

            if(numB == 0){
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Cannot Divide By Zero")
                                .asRuntimeException()
                );
                return;
            }

            double result = dividerService.Divider(numA, numB);

            DivideResponse divideResponse = DivideResponse.newBuilder().setResult(result).build();

            responseObserver.onNext(divideResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in DividerAdapterGRPC", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
