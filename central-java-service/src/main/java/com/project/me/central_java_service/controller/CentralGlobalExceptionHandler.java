package com.project.me.central_java_service.controller;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.exception.CentralErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class CentralGlobalExceptionHandler {
    @ExceptionHandler(BaseCoreServiceException.class)
    public ResponseEntity<CentralErrorResponse> handleException(BaseCoreServiceException ex) {
        log.warn(ex.getMessage());
        CentralErrorResponse authErrorResponse = new CentralErrorResponse(
                ex.getStatusCode().value(),
                ex.getReason(),
                ex.getMessage()
        );
        return new ResponseEntity<>(authErrorResponse, ex.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation failed");

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("details", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
