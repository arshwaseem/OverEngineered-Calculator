package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/op")
public class OperationsController {

    private static final Logger log = LoggerFactory.getLogger(OperationsController.class);
    private final OperationsService operationsService;

    public OperationsController(OperationsService operationsService){
        this.operationsService = operationsService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> Add(@RequestBody OperationRequestDTO operationRequestDTO) {
        try{
            log.debug("Recieved numA: " + operationRequestDTO.getNumA());
            log.debug("Recieved numB: " + operationRequestDTO.getNumB());
            Double result = operationsService.Add(operationRequestDTO.getNumA(), operationRequestDTO.getNumB());
            log.debug("Result: " + result);
            return ResponseEntity.ok().body(result);
        } catch (Exception e){
            log.error("Error during add : {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/subtract")
    public ResponseEntity<?> Subtract(@RequestBody OperationRequestDTO operationRequestDTO) {
        try{
            Double result = operationsService.Subtract(operationRequestDTO.getNumA(), operationRequestDTO.getNumB());
            return ResponseEntity.ok().body(result);
        } catch (Exception e){
            log.error("Error during subtract: {}",e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/multiply")
    public ResponseEntity<?> Multiply(@RequestBody OperationRequestDTO operationRequestDTO) {
        try{
            Double result = operationsService.Multiply(operationRequestDTO.getNumA(), operationRequestDTO.getNumB());
            return ResponseEntity.ok().body(result);
        } catch (Exception e){
            log.error("Error during multiply: {}",e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/divide")
    public ResponseEntity<?> Divide(@RequestBody OperationRequestDTO operationRequestDTO) {
        try{
            Double result = operationsService.Divide(operationRequestDTO.getNumA(), operationRequestDTO.getNumB());
            return ResponseEntity.ok().body(result);
        }
        catch (Exception e){
            log.error("Error during divide: {}",e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
