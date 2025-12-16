package ru.chousik.web.authservice.exception;

public class InvalidVerificationTokenException extends RuntimeException {
    public InvalidVerificationTokenException() {
        super("Неверный токен подтверждения email.");
    }
}
