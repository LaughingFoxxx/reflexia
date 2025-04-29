package com.project.me.authjavaservice.controller;

import com.project.me.authjavaservice.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AuthGlobalExceptionHandler {

    @ExceptionHandler(BaseAuthException.class)
    public ResponseEntity<AuthErrorResponse> handleException(BaseAuthException ex) {
        log.warn(ex.getMessage());
        AuthErrorResponse authErrorResponse = new AuthErrorResponse(
                ex.getStatusCode().value(),
                ex.getReason(),
                ex.getMessage()
        );
        return new ResponseEntity<>(authErrorResponse, ex.getStatusCode());
    }
}