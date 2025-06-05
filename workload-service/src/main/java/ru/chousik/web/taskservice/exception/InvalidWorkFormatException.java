package ru.chousik.web.taskservice.exception;

public class InvalidWorkFormatException extends RuntimeException {
    public InvalidWorkFormatException(String message) {
        super(message);
    }
}
