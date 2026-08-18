package com.example.audit.api;

import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(x -> errors.put(x.getField(), x.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("error", "validation_failed", "fields", errors));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> generic(Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", "internal_error"));
    }
}
