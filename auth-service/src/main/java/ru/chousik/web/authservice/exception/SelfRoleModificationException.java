package ru.chousik.web.authservice.exception;
//#tOdO сделать мб
public class SelfRoleModificationException extends RuntimeException {
    public SelfRoleModificationException(String message) {
        super(message);
    }
}
