package com.verify.panverification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    public ResponseEntity<String> handleNotFound(
            ResourceNotFoundException ex
    ){
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());

    }

    @ExceptionHandler(
            InvalidPanException.class
    )
    public ResponseEntity<String> handleInvalidPan(
            RuntimeException ex
    ){
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

    @ExceptionHandler(
            UserAlreadyExistsException.class
    )
    public ResponseEntity<String> handleUserExists(
            UserAlreadyExistsException ex
    )
    {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }
}
