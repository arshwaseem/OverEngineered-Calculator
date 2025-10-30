package com.arshwaseem.oe_calc;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OperationsGRPCAdapterTests {

    @Mock
    private OperationServiceGrpc.OperationServiceBlockingStub addStub;

    @Mock
    private OperationServiceGrpc.OperationServiceBlockingStub subtractStub;

    @Mock
    private OperationServiceGrpc.OperationServiceBlockingStub multiplyStub;

    @Mock
    private OperationServiceGrpc.OperationServiceBlockingStub divideStub;

    @Mock
    private GrpcConfiguration grpcConfiguration;

    @Mock
    private GatewayCustomMetrics gatewayCustomMetrics;

    private OperationsGRPCAdapter operationsGRPCAdapter;

    @BeforeEach
    void setUp() {
        operationsGRPCAdapter = new OperationsGRPCAdapter(
                addStub, subtractStub, multiplyStub, divideStub,
                grpcConfiguration, gatewayCustomMetrics
        );

        // Setup metrics mock to execute the supplier
        when(gatewayCustomMetrics.recordGrpcCall(anyString(), anyString(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<Double> supplier = invocation.getArgument(2);
                    return supplier.get();
                });
    }

    // ========== ADD OPERATION TESTS ==========

    @Test
    @DisplayName("Add: Should call gRPC stub and return result")
    void testAdd_Success() {
        // Given
        double numA = 10.0;
        double numB = 5.0;
        double expectedResult = 15.0;

        OperationRequest request = OperationRequest.newBuilder()
                .setNumA(numA)
                .setNumB(numB)
                .build();

        OperationResponse response = OperationResponse.newBuilder()
                .setResult(expectedResult)
                .build();

        when(addStub.add(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Add(numA, numB);

        // Then
        assertEquals(expectedResult, result);
        verify(addStub).add(any(OperationRequest.class));
        verify(gatewayCustomMetrics).recordGrpcCall(eq("adder"), eq("add"), any());
    }

    @Test
    @DisplayName("Add: Should record metrics for successful call")
    void testAdd_RecordsMetrics() {
        // Given
        OperationResponse response = OperationResponse.newBuilder()
                .setResult(15.0)
                .build();
        when(addStub.add(any(OperationRequest.class))).thenReturn(response);

        // When
        operationsGRPCAdapter.Add(10.0, 5.0);

        // Then
        verify(gatewayCustomMetrics).recordGrpcCall(
                eq("adder"),
                eq("add"),
                any()
        );
    }

    @Test
    @DisplayName("Add: Should handle gRPC exception")
    void testAdd_GrpcException() {
        // Given
        when(addStub.add(any(OperationRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.UNAVAILABLE));

        // When & Then
        assertThrows(StatusRuntimeException.class, () -> {
            operationsGRPCAdapter.Add(10.0, 5.0);
        });
    }

    @Test
    @DisplayName("Add: Should handle negative numbers")
    void testAdd_NegativeNumbers() {
        // Given
        OperationResponse response = OperationResponse.newBuilder()
                .setResult(-15.0)
                .build();
        when(addStub.add(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Add(-10.0, -5.0);

        // Then
        assertEquals(-15.0, result);
    }

    // ========== SUBTRACT OPERATION TESTS ==========

    @Test
    @DisplayName("Subtract: Should call gRPC stub and return result")
    void testSubtract_Success() {
        // Given
        double numA = 10.0;
        double numB = 5.0;
        double expectedResult = 5.0;

        OperationResponse response = OperationResponse.newBuilder()
                .setResult(expectedResult)
                .build();

        when(subtractStub.subtract(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Subtract(numA, numB);

        // Then
        assertEquals(expectedResult, result);
        verify(subtractStub).subtract(any(OperationRequest.class));
        verify(gatewayCustomMetrics).recordGrpcCall(eq("subtractor"), eq("subtract"), any());
    }

    @Test
    @DisplayName("Subtract: Should handle gRPC deadline exceeded")
    void testSubtract_DeadlineExceeded() {
        // Given
        when(subtractStub.subtract(any(OperationRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.DEADLINE_EXCEEDED));

        // When & Then
        assertThrows(StatusRuntimeException.class, () -> {
            operationsGRPCAdapter.Subtract(10.0, 5.0);
        });
    }

    @Test
    @DisplayName("Subtract: Should handle negative result")
    void testSubtract_NegativeResult() {
        // Given
        OperationResponse response = OperationResponse.newBuilder()
                .setResult(-5.0)
                .build();
        when(subtractStub.subtract(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Subtract(5.0, 10.0);

        // Then
        assertEquals(-5.0, result);
    }

    // ========== MULTIPLY OPERATION TESTS ==========

    @Test
    @DisplayName("Multiply: Should call gRPC stub and return result")
    void testMultiply_Success() {
        // Given
        double numA = 10.0;
        double numB = 5.0;
        double expectedResult = 50.0;

        OperationResponse response = OperationResponse.newBuilder()
                .setResult(expectedResult)
                .build();

        when(multiplyStub.multiply(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Multiply(numA, numB);

        // Then
        assertEquals(expectedResult, result);
        verify(multiplyStub).multiply(any(OperationRequest.class));
        verify(gatewayCustomMetrics).recordGrpcCall(eq("multiplier"), eq("multiply"), any());
    }

    @Test
    @DisplayName("Multiply: Should handle zero multiplication")
    void testMultiply_Zero() {
        // Given
        OperationResponse response = OperationResponse.newBuilder()
                .setResult(0.0)
                .build();
        when(multiplyStub.multiply(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Multiply(10.0, 0.0);

        // Then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("Multiply: Should handle gRPC UNAVAILABLE error")
    void testMultiply_ServiceUnavailable() {
        // Given
        when(multiplyStub.multiply(any(OperationRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.UNAVAILABLE
                        .withDescription("Service unavailable")));

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            operationsGRPCAdapter.Multiply(10.0, 5.0);
        });

        assertTrue(exception.getMessage().contains("UNAVAILABLE"));
    }

    // ========== DIVIDE OPERATION TESTS ==========

    @Test
    @DisplayName("Divide: Should call gRPC stub and return result")
    void testDivide_Success() {
        // Given
        double numA = 10.0;
        double numB = 5.0;
        double expectedResult = 2.0;

        OperationResponse response = OperationResponse.newBuilder()
                .setResult(expectedResult)
                .build();

        when(divideStub.divide(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Divide(numA, numB);

        // Then
        assertEquals(expectedResult, result);
        verify(divideStub).divide(any(OperationRequest.class));
        verify(gatewayCustomMetrics).recordGrpcCall(eq("divider"), eq("divide"), any());
    }

    @Test
    @DisplayName("Divide: Should handle division by zero from service")
    void testDivide_ByZero() {
        // Given
        when(divideStub.divide(any(OperationRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.INVALID_ARGUMENT
                        .withDescription("Division by zero")));

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            operationsGRPCAdapter.Divide(10.0, 0.0);
        });

        assertTrue(exception.getMessage().contains("Division by zero"));
    }

    @Test
    @DisplayName("Divide: Should handle decimal results")
    void testDivide_DecimalResult() {
        // Given
        OperationResponse response = OperationResponse.newBuilder()
                .setResult(3.333333333333333)
                .build();
        when(divideStub.divide(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Divide(10.0, 3.0);

        // Then
        assertEquals(3.333333333333333, result, 0.000000000001);
    }

    // ========== CROSS-CUTTING CONCERN TESTS ==========

    @Test
    @DisplayName("Should handle very large numbers")
    void testLargeNumbers() {
        // Given
        OperationResponse response = OperationResponse.newBuilder()
                .setResult(Double.MAX_VALUE)
                .build();
        when(addStub.add(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Add(Double.MAX_VALUE / 2, Double.MAX_VALUE / 2);

        // Then
        assertEquals(Double.MAX_VALUE, result);
    }

    @Test
    @DisplayName("Should handle very small decimal numbers")
    void testSmallDecimals() {
        // Given
        OperationResponse response = OperationResponse.newBuilder()
                .setResult(0.0003)
                .build();
        when(addStub.add(any(OperationRequest.class))).thenReturn(response);

        // When
        Double result = operationsGRPCAdapter.Add(0.0001, 0.0002);

        // Then
        assertEquals(0.0003, result, 0.00001);
    }

    @Test
    @DisplayName("Should create correct OperationRequest for each operation")
    void testOperationRequestCreation() {
        // Given
        double numA = 7.5;
        double numB = 2.5;

        OperationResponse dummyResponse = OperationResponse.newBuilder()
                .setResult(10.0)
                .build();

        when(addStub.add(any())).thenReturn(dummyResponse);
        when(subtractStub.subtract(any())).thenReturn(dummyResponse);
        when(multiplyStub.multiply(any())).thenReturn(dummyResponse);
        when(divideStub.divide(any())).thenReturn(dummyResponse);

        // When
        operationsGRPCAdapter.Add(numA, numB);
        operationsGRPCAdapter.Subtract(numA, numB);
        operationsGRPCAdapter.Multiply(numA, numB);
        operationsGRPCAdapter.Divide(numA, numB);

        // Then - Verify requests were created (indirectly through stub calls)
        verify(addStub).add(any(OperationRequest.class));
        verify(subtractStub).subtract(any(OperationRequest.class));
        verify(multiplyStub).multiply(any(OperationRequest.class));
        verify(divideStub).divide(any(OperationRequest.class));
    }

    @Test
    @DisplayName("Should propagate all gRPC error statuses correctly")
    void testVariousGrpcErrors() {
        // Test INTERNAL error
        when(addStub.add(any(OperationRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.INTERNAL));
        assertThrows(StatusRuntimeException.class, () -> operationsGRPCAdapter.Add(1.0, 2.0));

        // Test UNKNOWN error
        when(subtractStub.subtract(any(OperationRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.UNKNOWN));
        assertThrows(StatusRuntimeException.class, () -> operationsGRPCAdapter.Subtract(1.0, 2.0));

        // Test RESOURCE_EXHAUSTED error
        when(multiplyStub.multiply(any(OperationRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.RESOURCE_EXHAUSTED));
        assertThrows(StatusRuntimeException.class, () -> operationsGRPCAdapter.Multiply(1.0, 2.0));

        // Test CANCELLED error
        when(divideStub.divide(any(OperationRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.CANCELLED));
        assertThrows(StatusRuntimeException.class, () -> operationsGRPCAdapter.Divide(1.0, 2.0));
    }
}
