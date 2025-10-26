package com.arshwaseem.oe_calc;
import com.arshwaseem.oe_calc.history.History;
import com.arshwaseem.oe_calc.history.HistoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@GrpcService
public class DividerAdapterGRPC extends OperationServiceGrpc.OperationServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(DividerAdapterGRPC.class);
    private final DividerService dividerService;
    private final HistoryService historyService;

    public DividerAdapterGRPC(DividerService dividerService, HistoryService historyService) {

        this.dividerService = dividerService;
        this.historyService = historyService;
    }

    @Override
    public void divide(OperationRequest request, StreamObserver<OperationResponse> responseObserver){
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

            OperationResponse divideResponse = OperationResponse.newBuilder().setResult(result).build();

            History CalcHistory = new History();
            CalcHistory.setNumA(numA);
            CalcHistory.setNumB(numB);
            CalcHistory.setServiceName("Divider");
            CalcHistory.setTimeStamp(Timestamp.valueOf(LocalDateTime.now()));
            CalcHistory.setResult(result);

            historyService.PublishHistory(CalcHistory);

            responseObserver.onNext(divideResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in DividerAdapterGRPC", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
