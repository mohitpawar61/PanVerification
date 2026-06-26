package com.verify.panverification.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidPanException extends RuntimeException{

    public InvalidPanException(
            String message
    )
    {
        super(message);
        log.warn("InvalidPanException thrown: {}",message);
    }
}
