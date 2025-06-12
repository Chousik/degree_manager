package ru.chousik.web.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AdminRoleAlreadyAssignedException extends RuntimeException {
    public AdminRoleAlreadyAssignedException(String username) {
        super("У пользователя %s уже присутствует роль админа.".formatted(username));
    }
}
