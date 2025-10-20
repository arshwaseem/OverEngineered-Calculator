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
public class SubtractorAdapterGRPC extends OperationServiceGrpc.OperationServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(SubtractorAdapterGRPC.class);
    private final SubtractorService  subtractorService;
    private final HistoryService historyService;

    public SubtractorAdapterGRPC(SubtractorService subtractorService, HistoryService historyService){

        this.subtractorService = subtractorService;
        this.historyService = historyService;
    }

    @Override
    public void subtract (OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
        try{
            double numA = request.getNumA();
            double numB = request.getNumB();

            double result = subtractorService.Subtract(numA, numB);

            OperationResponse response = OperationResponse.newBuilder().setResult(result).build();

            History CalcHistory = new History();
            CalcHistory.setNumA(numA);
            CalcHistory.setNumB(numB);
            CalcHistory.setResult(result);
            CalcHistory.setServiceName("Subtractor");
            CalcHistory.setTimeStamp(Timestamp.valueOf(LocalDateTime.now()));

            historyService.PublishHistory(CalcHistory);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e){
            log.error("Error in SubtractorAdapterGRPC", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
