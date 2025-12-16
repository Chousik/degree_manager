package ru.chousik.web.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String email) {
        super("Пользователь с email '%s' уже существует в системе".formatted(email));
    }
}
