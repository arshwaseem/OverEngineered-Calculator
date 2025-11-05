package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.history.History;
import com.arshwaseem.oe_calc.history.HistoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.grpc.server.service.GrpcService;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@GrpcService
public class AdderAdapterGRPC extends OperationServiceGrpc.OperationServiceImplBase {

    private static final Logger log = LogManager.getLogger(AdderAdapterGRPC.class);
    private final AdderService adderService;
    private final HistoryService historyService;

    public AdderAdapterGRPC(AdderService adderService, HistoryService historyService) {
        this.adderService = adderService;
        this.historyService = historyService;
    }

    @Override
    public void add(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {

        try{
            log.debug("Adding after receiving grpc request");
            double numA =  request.getNumA();
            double numB = request.getNumB();
            double result = adderService.Add(numA, numB);

            History calcHistory = new History();
            calcHistory.setServiceName("Adder");
            calcHistory.setNumA(numA);
            calcHistory.setNumB(numB);
            calcHistory.setResult(result);
            calcHistory.setTimeStamp(Timestamp.valueOf(LocalDateTime.now()));

            historyService.PublishHistory(calcHistory);

            OperationResponse response = OperationResponse.newBuilder().setResult(result).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in AdderAdapterGRPC: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
