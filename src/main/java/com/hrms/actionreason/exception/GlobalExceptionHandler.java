package com.hrms.actionreason.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceException.class)
    public ResponseEntity<?> handleResource(ResourceException ex) {

        return ResponseEntity.badRequest().body(ex.getMessage());

    }

}