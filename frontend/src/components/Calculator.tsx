import { useState, useCallback, memo } from 'react';
import apiService from '@/services/api';
import type { OperationResponse } from '@/types';
import { Card, CardContent } from '@/components/ui/card';
import { motion } from 'motion/react';
import { Button } from "@/components/ui/button.tsx";

type Operation = '+' | '-' | '*' | '/';

// Memoized button component to prevent unnecessary re-renders
const CalculatorButton = memo(({
    children,
    onClick,
    variant = "outline",
    className = "",
    disabled
}: {
    children: React.ReactNode;
    onClick: () => void;
    variant?: "outline" | "default" | "secondary" | "destructive";
    className?: string;
    disabled: boolean;
}) => {
    const MotionButton = motion.create(Button);

    return (
        <MotionButton
            onClick={onClick}
            variant={variant}
            className={`h-16 text-lg ${className}`}
            whileTap={{ scale: 0.80 }}
            transition={{ type: "spring", stiffness: 400, damping: 17 }}
            disabled={disabled}
        >
            {children}
        </MotionButton>
    );
});

CalculatorButton.displayName = 'CalculatorButton';

export default function Calculator() {
    const [display, setDisplay] = useState<string>('0');
    const [firstOperand, setFirstOperand] = useState<number | null>(null);
    const [operation, setOperation] = useState<Operation | null>(null);
    const [waitingForSecondOperand, setWaitingForSecondOperand] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(false);

    const isErrorState = useCallback((value: string): boolean => {
        return value.includes('Error') || value === 'undefined' || value === 'NaN';
    }, []);

    const inputDigit = useCallback((digit: number) => {
        setDisplay(prevDisplay => {
            if (waitingForSecondOperand || isErrorState(prevDisplay)) {
                setWaitingForSecondOperand(false);
                return String(digit);
            }

            if (prevDisplay === '0') {
                return String(digit);
            }

            return prevDisplay + String(digit);
        });
    }, [waitingForSecondOperand, isErrorState]);

    const inputDecimal = useCallback(() => {
        setDisplay(prevDisplay => {
            if (waitingForSecondOperand) {
                setWaitingForSecondOperand(false);
                return '0.';
            }

            if (!prevDisplay.includes('.')) {
                return prevDisplay + '.';
            }

            return prevDisplay;
        });
    }, [waitingForSecondOperand]);

    const clear = useCallback(() => {
        setDisplay('0');
        setFirstOperand(null);
        setOperation(null);
        setWaitingForSecondOperand(false);
    }, []);

    const handleOperator = useCallback(async (nextOperation: Operation) => {
        const inputValue = parseFloat(display);

        if (firstOperand === null) {
            setFirstOperand(inputValue);
            setOperation(nextOperation);
            setWaitingForSecondOperand(true);
            return;
        }

        if (operation && !waitingForSecondOperand) {
            try {
                setLoading(true);

                let result: OperationResponse;

                switch (operation) {
                    case '+':
                        result = await apiService.add(firstOperand, inputValue);
                        break;
                    case '-':
                        result = await apiService.subtract(firstOperand, inputValue);
                        break;
                    case '*':
                        result = await apiService.multiply(firstOperand, inputValue);
                        break;
                    case '/':
                        if (inputValue === 0) {
                            setDisplay('Error');
                            setLoading(false);
                            return;
                        }
                        result = await apiService.divide(firstOperand, inputValue);
                        break;
                    default:
                        return;
                }

                const calculatedResult = result.result;
                setDisplay(String(calculatedResult));
                setFirstOperand(calculatedResult);
                setOperation(nextOperation);
                setWaitingForSecondOperand(true);

            } catch (error) {
                console.error('Calculation error:', error);
                setDisplay('Error');
                setFirstOperand(null);
                setOperation(null);
            } finally {
                setLoading(false);
            }
        } else {
            setOperation(nextOperation);
            setWaitingForSecondOperand(true);
        }
    }, [display, firstOperand, operation, waitingForSecondOperand]);

    const handleEquals = useCallback(async () => {
        const inputValue = parseFloat(display);

        if (firstOperand === null || operation === null) {
            return;
        }

        try {
            setLoading(true);

            let result: OperationResponse;

            switch (operation) {
                case '+':
                    result = await apiService.add(firstOperand, inputValue);
                    break;
                case '-':
                    result = await apiService.subtract(firstOperand, inputValue);
                    break;
                case '*':
                    result = await apiService.multiply(firstOperand, inputValue);
                    break;
                case '/':
                    if (inputValue === 0) {
                        setDisplay('Error: Div by 0');
                        setLoading(false);
                        return;
                    }
                    result = await apiService.divide(firstOperand, inputValue);
                    break;
                default:
                    return;
            }

            setDisplay(String(result.result));
            setFirstOperand(result.result);
            setOperation(null);
            setWaitingForSecondOperand(true);

        } catch (error) {
            console.error('Calculation error:', error);
            setDisplay('Error');
            setFirstOperand(null);
            setOperation(null);
        } finally {
            setLoading(false);
        }
    }, [display, firstOperand, operation]);

    // Create memoized handlers for each digit
    const handleDigit0 = useCallback(() => inputDigit(0), [inputDigit]);
    const handleDigit1 = useCallback(() => inputDigit(1), [inputDigit]);
    const handleDigit2 = useCallback(() => inputDigit(2), [inputDigit]);
    const handleDigit3 = useCallback(() => inputDigit(3), [inputDigit]);
    const handleDigit4 = useCallback(() => inputDigit(4), [inputDigit]);
    const handleDigit5 = useCallback(() => inputDigit(5), [inputDigit]);
    const handleDigit6 = useCallback(() => inputDigit(6), [inputDigit]);
    const handleDigit7 = useCallback(() => inputDigit(7), [inputDigit]);
    const handleDigit8 = useCallback(() => inputDigit(8), [inputDigit]);
    const handleDigit9 = useCallback(() => inputDigit(9), [inputDigit]);

    // Create memoized handlers for operations
    const handleAdd = useCallback(() => handleOperator('+'), [handleOperator]);
    const handleSubtract = useCallback(() => handleOperator('-'), [handleOperator]);
    const handleMultiply = useCallback(() => handleOperator('*'), [handleOperator]);
    const handleDivide = useCallback(() => handleOperator('/'), [handleOperator]);

    return (
        <Card className="w-full max-w-md mx-auto border border-gray-900 border-2">
            <CardContent className="p-6">
                <div
                    className={`col-span-4 bg-gray-200 p-4 rounded-lg mb-4 text-right text-4xl font-semibold tracking-wide ${loading ? 'opacity-50' : ''}`}>
                    <motion.div
                        key={display}
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.2 }}
                        className="text-slate-900 text-4xl break-all"
                    >
                        {display}
                    </motion.div>
                </div>

                <div className="grid grid-cols-4 gap-3">
                    <CalculatorButton
                        onClick={clear}
                        variant="outline"
                        className="col-span-2 h-14 bg-red-600 hover:bg-red-800 font-bold"
                        disabled={loading}
                    >
                        Clear
                    </CalculatorButton>

                    <CalculatorButton
                        onClick={handleDivide}
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        disabled={loading}
                    >
                        ÷
                    </CalculatorButton>

                    <CalculatorButton
                        onClick={handleMultiply}
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        disabled={loading}
                    >
                        ×
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit7}
                        disabled={loading}
                    >
                        7
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit8}
                        disabled={loading}
                    >
                        8
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit9}
                        disabled={loading}
                    >
                        9
                    </CalculatorButton>

                    <CalculatorButton
                        onClick={handleSubtract}
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        disabled={loading}
                    >
                        −
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit4}
                        disabled={loading}
                    >
                        4
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit5}
                        disabled={loading}
                    >
                        5
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit6}
                        disabled={loading}
                    >
                        6
                    </CalculatorButton>

                    <CalculatorButton
                        onClick={handleAdd}
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        disabled={loading}
                    >
                        +
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit1}
                        disabled={loading}
                    >
                        1
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit2}
                        disabled={loading}
                    >
                        2
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={handleDigit3}
                        disabled={loading}
                    >
                        3
                    </CalculatorButton>

                    <CalculatorButton
                        onClick={handleEquals}
                        className="row-span-2 bg-blue-600 hover:bg-blue-700 active:bg-blue-900 text-white h-full text-2xl font-bold"
                        disabled={loading}
                    >
                        =
                    </CalculatorButton>

                    <CalculatorButton
                        onClick={handleDigit0}
                        variant="outline"
                        className="col-span-2 h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        disabled={loading}
                    >
                        0
                    </CalculatorButton>

                    <CalculatorButton
                        variant="outline"
                        className="h-14 text-xl bg-slate-100 font-bold hover:bg-slate-400 active:bg-slate-500"
                        onClick={inputDecimal}
                        disabled={loading}
                    >
                        .
                    </CalculatorButton>
                </div>

                {loading && (
                    <p className="text-center text-sm text-gray-500 mt-4">
                        Calculating...
                    </p>
                )}
            </CardContent>
        </Card>
    );
}