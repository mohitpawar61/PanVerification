package com.verify.panverification.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(
            String message
    )
    {
        super(message);
    }
}
