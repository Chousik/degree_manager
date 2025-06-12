package ru.chousik.web.authservice.controller;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.chousik.web.authservice.exception.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler({
            UserNotFoundException.class,
            UsernameExistsException.class,
            TeacherNotFoundException.class,
            TeacherAlreadyLinkedException.class,
            IncorrectOldPasswordException.class,
            PasswordReuseException.class,
            WeakPasswordException.class,
            AdminRoleAlreadyAssignedException.class,
            AdminRoleNotAssignedException.class,
            SelfRoleModificationException.class,
            JwtExpiredException.class
    })
    public ResponseEntity<String> handleValidationError(RuntimeException ex) {
        HttpStatus status = Optional.ofNullable(
                        AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class))
                .map(ResponseStatus::value)
                .orElse(HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(status).body(ex.getMessage());
    }
}
