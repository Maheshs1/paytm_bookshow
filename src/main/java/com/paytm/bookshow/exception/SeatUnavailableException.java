package com.paytm.bookshow.exception;

public class SeatUnavailableException extends RuntimeException{
    public SeatUnavailableException(String errorMessage) {
        super(errorMessage);
    }
}
