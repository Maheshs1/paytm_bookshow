package com.paytm.bookshow.exception;

public class InvalidSeatException extends RuntimeException{
    public InvalidSeatException(String errorMessage) {
        super(errorMessage);
    }
}
