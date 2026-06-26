package com.verify.panverification.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    public ResponseEntity<String> handleNotFound(
            ResourceNotFoundException ex
    ){
        log.error("Resource not found: {}", ex.getMessage());
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
        log.warn("Invalid PAN: {}", ex.getMessage());
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
        log.warn("User already exists: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }
}
