package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationsServiceTests {

    @Mock
    private OperationsClientPort operationsClientPort;

    @InjectMocks
    private OperationsService operationsService;

    @Test
    @DisplayName("Add: Should delegate to client port and return result")
    void testAdd_Success() {
        // Given
        double numA = 10.0;
        double numB = 5.0;
        double expectedResult = 15.0;
        when(operationsClientPort.Add(numA, numB)).thenReturn(expectedResult);

        // When
        Double result = operationsService.Add(numA, numB);

        // Then
        assertEquals(expectedResult, result);
        verify(operationsClientPort).Add(numA, numB);
    }

    @Test
    @DisplayName("Add: Should propagate exception from client port")
    void testAdd_PropagatesException() {
        // Given
        when(operationsClientPort.Add(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("gRPC connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            operationsService.Add(10.0, 5.0);
        });
    }

    @Test
    @DisplayName("Subtract: Should delegate to client port and return result")
    void testSubtract_Success() {
        // Given
        double numA = 10.0;
        double numB = 5.0;
        double expectedResult = 5.0;
        when(operationsClientPort.Subtract(numA, numB)).thenReturn(expectedResult);

        // When
        Double result = operationsService.Subtract(numA, numB);

        // Then
        assertEquals(expectedResult, result);
        verify(operationsClientPort).Subtract(numA, numB);
    }

    @Test
    @DisplayName("Subtract: Should handle negative results")
    void testSubtract_NegativeResult() {
        // Given
        when(operationsClientPort.Subtract(5.0, 10.0)).thenReturn(-5.0);

        // When
        Double result = operationsService.Subtract(5.0, 10.0);

        // Then
        assertEquals(-5.0, result);
    }

    @Test
    @DisplayName("Multiply: Should delegate to client port and return result")
    void testMultiply_Success() {
        // Given
        double numA = 10.0;
        double numB = 5.0;
        double expectedResult = 50.0;
        when(operationsClientPort.Multiply(numA, numB)).thenReturn(expectedResult);

        // When
        Double result = operationsService.Multiply(numA, numB);

        // Then
        assertEquals(expectedResult, result);
        verify(operationsClientPort).Multiply(numA, numB);
    }

    @Test
    @DisplayName("Multiply: Should handle zero result")
    void testMultiply_Zero() {
        // Given
        when(operationsClientPort.Multiply(10.0, 0.0)).thenReturn(0.0);

        // When
        Double result = operationsService.Multiply(10.0, 0.0);

        // Then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("Divide: Should delegate to client port and return result")
    void testDivide_Success() {
        // Given
        double numA = 10.0;
        double numB = 5.0;
        double expectedResult = 2.0;
        when(operationsClientPort.Divide(numA, numB)).thenReturn(expectedResult);

        // When
        Double result = operationsService.Divide(numA, numB);

        // Then
        assertEquals(expectedResult, result);
        verify(operationsClientPort).Divide(numA, numB);
    }

    @Test
    @DisplayName("Should call client port exactly once per operation")
    void testSingleDelegationPerOperation() {
        // Given
        when(operationsClientPort.Add(anyDouble(), anyDouble())).thenReturn(15.0);
        when(operationsClientPort.Subtract(anyDouble(), anyDouble())).thenReturn(5.0);
        when(operationsClientPort.Multiply(anyDouble(), anyDouble())).thenReturn(50.0);
        when(operationsClientPort.Divide(anyDouble(), anyDouble())).thenReturn(2.0);

        // When
        operationsService.Add(10.0, 5.0);
        operationsService.Subtract(10.0, 5.0);
        operationsService.Multiply(10.0, 5.0);
        operationsService.Divide(10.0, 5.0);

        // Then
        verify(operationsClientPort, times(1)).Add(anyDouble(), anyDouble());
        verify(operationsClientPort, times(1)).Subtract(anyDouble(), anyDouble());
        verify(operationsClientPort, times(1)).Multiply(anyDouble(), anyDouble());
        verify(operationsClientPort, times(1)).Divide(anyDouble(), anyDouble());
    }
}
