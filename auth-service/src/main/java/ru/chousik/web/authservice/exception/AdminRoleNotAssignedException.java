package ru.chousik.web.authservice.exception;

public class AdminRoleNotAssignedException extends RuntimeException {
    public AdminRoleNotAssignedException(String message) {
        super(message);
    }
}
