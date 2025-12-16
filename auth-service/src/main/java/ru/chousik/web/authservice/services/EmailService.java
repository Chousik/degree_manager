package ru.chousik.web.authservice.services;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}
