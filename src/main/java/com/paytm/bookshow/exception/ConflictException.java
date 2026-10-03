package com.paytm.bookshow.exception;

public class ConflictException extends RuntimeException{
    public ConflictException(String errorMessage) {
        super(errorMessage);
    }
}
