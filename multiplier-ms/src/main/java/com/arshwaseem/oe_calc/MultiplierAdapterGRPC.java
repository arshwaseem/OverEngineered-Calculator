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
public class MultiplierAdapterGRPC extends OperationServiceGrpc.OperationServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(MultiplierAdapterGRPC.class);
    private final MultiplierService multiplierService;
    private final HistoryService historyService;

    public MultiplierAdapterGRPC(MultiplierService multiplierService, HistoryService historyService) {

        this.multiplierService = multiplierService;
        this.historyService = historyService;

    }

    @Override
    public void multiply (OperationRequest request, StreamObserver<OperationResponse> responseObserver) {

        try{
            double numA = request.getNumA();
            double numB = request.getNumB();
            double result = multiplierService.Multiply(numA, numB);

            OperationResponse response = OperationResponse.newBuilder().setResult(result).build();

            History CalcHistory = new History();
            CalcHistory.setNumA(numA);
            CalcHistory.setNumB(numB);
            CalcHistory.setResult(result);
            CalcHistory.setServiceName("Multiplier");
            CalcHistory.setTimeStamp(Timestamp.valueOf(LocalDateTime.now()));

            historyService.PublishHistory(CalcHistory);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in MultiplierAdapterGRPC: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
