package ru.chousik.web.taskservice.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {
    public record ApiError(String message, Instant timestamp) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError body = new ApiError(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
