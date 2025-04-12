package com.project.me.authjavaservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthErrorResponse {
    private int status;
    private String error;
    private String message;
}
