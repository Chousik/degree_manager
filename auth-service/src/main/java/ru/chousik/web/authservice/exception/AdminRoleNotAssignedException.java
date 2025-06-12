package ru.chousik.web.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AdminRoleNotAssignedException extends RuntimeException {
    public AdminRoleNotAssignedException(String username) {
        super("У пользователя %s отсутствует роль админа.".formatted(username));
    }
}
