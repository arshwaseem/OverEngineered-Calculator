package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OperationsService {
    private static final Logger log = LoggerFactory.getLogger(OperationsService.class);
    private final OperationsClientPort operationsClientPort;

    public OperationsService(OperationsClientPort operationsClientPort) {
        this.operationsClientPort = operationsClientPort;
    }

    public Double Add(Double numA, Double numB){
        try{
            log.debug("Performing Addition");
            return operationsClientPort.Add(numA,numB);
        } catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public Double Subtract(Double numA, Double numB){
        try{
            return operationsClientPort.Subtract(numA,numB);
        } catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public Double Multiply(Double numA, Double numB){
        try{
            return operationsClientPort.Multiply(numA,numB);
        } catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public Double Divide(Double numA, Double numB){
        try{
            return operationsClientPort.Divide(numA,numB);
        } catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
