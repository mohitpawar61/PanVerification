package com.verify.panverification.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserAlreadyExistsException extends RuntimeException{

    public UserAlreadyExistsException(
            String message
    )
    {
        super(message);
        log.warn("UserAlreadyExistsException thrown: {}",message);
    }
}
