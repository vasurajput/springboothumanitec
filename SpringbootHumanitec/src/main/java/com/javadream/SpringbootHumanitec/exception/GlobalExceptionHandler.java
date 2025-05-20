package com.javadream.SpringbootHumanitec.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomApiException ex) {
        log.error("CustomApiException at {}: {}", ex.getLocation(), ex.getMessage(), ex);

        Map<String, Object> body = Map.of(
                "timestamp", ex.getTimestamp(),
                "status", ex.getStatusCode(),
                "error", ex.getMessage(),
                "location", ex.getLocation()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}