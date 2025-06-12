package ru.chousik.web.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SelfRoleModificationException extends RuntimeException {
    public SelfRoleModificationException() {
        super("Запрещено модифицировать свои роли.");
    }
}
