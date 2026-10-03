package com.paytm.bookshow.exception;

public class SeatLimitExceededException extends RuntimeException{
    public SeatLimitExceededException(String errorMessage) {
        super(errorMessage);
    }
}
