package com.verify.panverification.exception;

public class InvalidPanException extends RuntimeException{

    public InvalidPanException(
            String message
    )
    {
        super(message);
    }
}
