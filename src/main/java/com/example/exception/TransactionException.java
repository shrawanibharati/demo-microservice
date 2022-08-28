package com.example.exception;


public class TransactionException extends Exception {


    public TransactionException(String errorMessage) {
        super(errorMessage);
    }

    public TransactionException(String errorMessage, Throwable t) {
        super(errorMessage, t);
    }
}
