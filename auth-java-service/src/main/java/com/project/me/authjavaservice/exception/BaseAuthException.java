package com.project.me.authjavaservice.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

// Исключение: истёкший токен
@Getter
@Setter
public class BaseAuthException extends ResponseStatusException {
    public BaseAuthException(HttpStatusCode status, String reason) {
        super(status, reason);
    }
}
