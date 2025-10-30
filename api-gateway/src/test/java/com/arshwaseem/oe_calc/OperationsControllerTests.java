package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OperationsControllerTests {

    @Mock
    private OperationsService operationsService;

    @InjectMocks
    private OperationsController operationsController;

    private OperationRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new OperationRequestDTO();
        validRequest.setNumA(10.0);
        validRequest.setNumB(5.0);
    }

    // ========== ADD OPERATION TESTS ==========

    @Test
    @DisplayName("Add: Should return sum when valid request")
    void testAdd_Success() {
        // Given
        double expectedResult = 15.0;
        when(operationsService.Add(10.0, 5.0)).thenReturn(expectedResult);

        // When
        ResponseEntity<?> response = operationsController.Add(validRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        verify(operationsService).Add(10.0, 5.0);
    }

    @Test
    @DisplayName("Add: Should handle negative numbers")
    void testAdd_NegativeNumbers() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(-10.0);
        request.setNumB(-5.0);
        when(operationsService.Add(-10.0, -5.0)).thenReturn(-15.0);

        // When
        ResponseEntity<?> response = operationsController.Add(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(-15.0, response.getBody());
    }

    @Test
    @DisplayName("Add: Should handle zero values")
    void testAdd_ZeroValues() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(0.0);
        request.setNumB(0.0);
        when(operationsService.Add(0.0, 0.0)).thenReturn(0.0);

        // When
        ResponseEntity<?> response = operationsController.Add(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0, response.getBody());
    }

    @Test
    @DisplayName("Add: Should handle large numbers")
    void testAdd_LargeNumbers() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(Double.MAX_VALUE / 2);
        request.setNumB(100.0);
        double expectedResult = (Double.MAX_VALUE / 2) + 100.0;
        when(operationsService.Add(anyDouble(), anyDouble())).thenReturn(expectedResult);

        // When
        ResponseEntity<?> response = operationsController.Add(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    @DisplayName("Add: Should return 500 when service throws exception")
    void testAdd_ServiceException() {
        // Given
        String errorMessage = "Service unavailable";
        when(operationsService.Add(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<?> response = operationsController.Add(validRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    // ========== SUBTRACT OPERATION TESTS ==========

    @Test
    @DisplayName("Subtract: Should return difference when valid request")
    void testSubtract_Success() {
        // Given
        double expectedResult = 5.0;
        when(operationsService.Subtract(10.0, 5.0)).thenReturn(expectedResult);

        // When
        ResponseEntity<?> response = operationsController.Subtract(validRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        verify(operationsService).Subtract(10.0, 5.0);
    }

    @Test
    @DisplayName("Subtract: Should handle negative result")
    void testSubtract_NegativeResult() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(5.0);
        request.setNumB(10.0);
        when(operationsService.Subtract(5.0, 10.0)).thenReturn(-5.0);

        // When
        ResponseEntity<?> response = operationsController.Subtract(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(-5.0, response.getBody());
    }

    @Test
    @DisplayName("Subtract: Should return 500 when service throws exception")
    void testSubtract_ServiceException() {
        // Given
        when(operationsService.Subtract(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("gRPC error"));

        // When
        ResponseEntity<?> response = operationsController.Subtract(validRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ========== MULTIPLY OPERATION TESTS ==========

    @Test
    @DisplayName("Multiply: Should return product when valid request")
    void testMultiply_Success() {
        // Given
        double expectedResult = 50.0;
        when(operationsService.Multiply(10.0, 5.0)).thenReturn(expectedResult);

        // When
        ResponseEntity<?> response = operationsController.Multiply(validRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        verify(operationsService).Multiply(10.0, 5.0);
    }

    @Test
    @DisplayName("Multiply: Should handle multiplication by zero")
    void testMultiply_ByZero() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(10.0);
        request.setNumB(0.0);
        when(operationsService.Multiply(10.0, 0.0)).thenReturn(0.0);

        // When
        ResponseEntity<?> response = operationsController.Multiply(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0, response.getBody());
    }

    @Test
    @DisplayName("Multiply: Should handle negative numbers")
    void testMultiply_NegativeNumbers() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(-10.0);
        request.setNumB(5.0);
        when(operationsService.Multiply(-10.0, 5.0)).thenReturn(-50.0);

        // When
        ResponseEntity<?> response = operationsController.Multiply(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(-50.0, response.getBody());
    }

    @Test
    @DisplayName("Multiply: Should handle decimal numbers")
    void testMultiply_Decimals() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(2.5);
        request.setNumB(4.2);
        when(operationsService.Multiply(2.5, 4.2)).thenReturn(10.5);

        // When
        ResponseEntity<?> response = operationsController.Multiply(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10.5, response.getBody());
    }

    @Test
    @DisplayName("Multiply: Should return 500 when service throws exception")
    void testMultiply_ServiceException() {
        // Given
        when(operationsService.Multiply(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("Network error"));

        // When
        ResponseEntity<?> response = operationsController.Multiply(validRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ========== DIVIDE OPERATION TESTS ==========

    @Test
    @DisplayName("Divide: Should return quotient when valid request")
    void testDivide_Success() {
        // Given
        double expectedResult = 2.0;
        when(operationsService.Divide(10.0, 5.0)).thenReturn(expectedResult);

        // When
        ResponseEntity<?> response = operationsController.Divide(validRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        verify(operationsService).Divide(10.0, 5.0);
    }

    @Test
    @DisplayName("Divide: Should handle division by zero error")
    void testDivide_ByZero() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(10.0);
        request.setNumB(0.0);
        when(operationsService.Divide(10.0, 0.0))
                .thenThrow(new ArithmeticException("Division by zero"));

        // When
        ResponseEntity<?> response = operationsController.Divide(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Division by zero"));
    }

    @Test
    @DisplayName("Divide: Should handle decimal results")
    void testDivide_DecimalResult() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(10.0);
        request.setNumB(3.0);
        when(operationsService.Divide(10.0, 3.0)).thenReturn(3.333333333333333);

        // When
        ResponseEntity<?> response = operationsController.Divide(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3.333333333333333, response.getBody());
    }

    @Test
    @DisplayName("Divide: Should handle negative numbers")
    void testDivide_NegativeNumbers() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(-10.0);
        request.setNumB(5.0);
        when(operationsService.Divide(-10.0, 5.0)).thenReturn(-2.0);

        // When
        ResponseEntity<?> response = operationsController.Divide(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(-2.0, response.getBody());
    }

    @Test
    @DisplayName("Divide: Should return 500 when service throws exception")
    void testDivide_ServiceException() {
        // Given
        when(operationsService.Divide(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("gRPC timeout"));

        // When
        ResponseEntity<?> response = operationsController.Divide(validRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("gRPC timeout"));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle very small decimal numbers")
    void testVerySmallDecimals() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(0.0001);
        request.setNumB(0.0002);
        when(operationsService.Add(0.0001, 0.0002)).thenReturn(0.0003);

        // When
        ResponseEntity<?> response = operationsController.Add(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0003, response.getBody());
    }

    @Test
    @DisplayName("Should handle infinity result")
    void testInfinityResult() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(Double.MAX_VALUE);
        request.setNumB(Double.MAX_VALUE);
        when(operationsService.Add(Double.MAX_VALUE, Double.MAX_VALUE))
                .thenReturn(Double.POSITIVE_INFINITY);

        // When
        ResponseEntity<?> response = operationsController.Add(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Double.POSITIVE_INFINITY, response.getBody());
    }

    @Test
    @DisplayName("Should handle NaN result")
    void testNaNResult() {
        // Given
        OperationRequestDTO request = new OperationRequestDTO();
        request.setNumA(0.0);
        request.setNumB(0.0);
        when(operationsService.Divide(0.0, 0.0)).thenReturn(Double.NaN);

        // When
        ResponseEntity<?> response = operationsController.Divide(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Double.isNaN((Double) response.getBody()));
    }
}
