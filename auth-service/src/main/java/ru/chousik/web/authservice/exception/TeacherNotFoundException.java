package ru.chousik.web.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TeacherNotFoundException extends RuntimeException {
    public TeacherNotFoundException(String teacherData) {
        super("Учитель %s не зарегистрирован в системе.".formatted(teacherData));
    }
}
