package ru.chousik.web.authservice.exception;

public class SelfRoleModificationException extends RuntimeException {
    public SelfRoleModificationException(String message) {
        super(message);
    }
}
