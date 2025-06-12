package ru.chousik.web.authservice.exception;

public class PasswordReuseException extends RuntimeException {
  public PasswordReuseException(String message) {
    super(message);
  }
}
