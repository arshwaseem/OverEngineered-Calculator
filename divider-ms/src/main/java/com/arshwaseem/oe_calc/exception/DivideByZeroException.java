package com.arshwaseem.oe_calc.exception;

public class DivideByZeroException extends ArithmeticException{
    public DivideByZeroException(String message){
        super(message);
    }
}
