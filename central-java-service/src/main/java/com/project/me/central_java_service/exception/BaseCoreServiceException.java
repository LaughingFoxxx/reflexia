package com.project.me.central_java_service.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class BaseCoreServiceException extends ResponseStatusException {
    public BaseCoreServiceException(HttpStatusCode code, String message) {
        super(code, message);
    }
}
