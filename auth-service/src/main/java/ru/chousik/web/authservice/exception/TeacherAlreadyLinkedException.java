package ru.chousik.web.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TeacherAlreadyLinkedException extends RuntimeException {
    public TeacherAlreadyLinkedException(String teacherData) {
        super("%s уже зарегистрирован в системе".formatted(teacherData));
    }
}
