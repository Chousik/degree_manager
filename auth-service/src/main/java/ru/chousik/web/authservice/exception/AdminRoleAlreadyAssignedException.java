package ru.chousik.web.authservice.exception;

public class AdminRoleAlreadyAssignedException extends RuntimeException {
    public AdminRoleAlreadyAssignedException(String message) {
        super(message);
    }
}
