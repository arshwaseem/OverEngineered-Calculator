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
public class AdderAdapterGRPC extends AdderServiceGrpc.AdderServiceImplBase {

    private static final Logger log = LogManager.getLogger(AdderAdapterGRPC.class);
    private final AdderService adderService;
    private final HistoryService historyService;

    public AdderAdapterGRPC(AdderService adderService, HistoryService historyService) {
        this.adderService = adderService;
        this.historyService = historyService;
    }

    @Override
    public void add(AddRequest request, StreamObserver<AddResponse> responseObserver) {

        try{
            double numA =  request.getNumA();
            double numB = request.getNumB();
            double result = adderService.Add(numA, numB);

            History CalcHistory = new History();
            CalcHistory.setServiceName("Adder");
            CalcHistory.setNumA(numA);
            CalcHistory.setNumB(numB);
            CalcHistory.setResult(result);
            CalcHistory.setTimeStamp(Timestamp.valueOf(LocalDateTime.now()));

            AddResponse response = AddResponse.newBuilder().setResult(result).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in AdderAdapterGRPC", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
