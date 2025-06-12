package ru.chousik.web.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectOldPasswordException extends RuntimeException {
    public IncorrectOldPasswordException() {
        super("Введен неверный пароль.");
    }
}
