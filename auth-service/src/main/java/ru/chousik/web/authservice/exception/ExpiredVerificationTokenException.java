package ru.chousik.web.authservice.exception;

public class ExpiredVerificationTokenException extends RuntimeException {
    public ExpiredVerificationTokenException() {
        super("Срок действия токена подтверждения истёк.");
    }
}
