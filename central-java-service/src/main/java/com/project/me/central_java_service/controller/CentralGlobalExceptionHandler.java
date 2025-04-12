package com.project.me.central_java_service.controller;

import com.project.me.central_java_service.exception.BaseCoreServiceException;
import com.project.me.central_java_service.exception.CentralErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
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
}
