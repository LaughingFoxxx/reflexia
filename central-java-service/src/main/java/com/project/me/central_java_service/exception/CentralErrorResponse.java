package com.project.me.central_java_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CentralErrorResponse {
    private int status;
    private String error;
    private String message;
}

