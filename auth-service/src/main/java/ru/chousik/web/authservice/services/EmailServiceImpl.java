package ru.chousik.web.authservice.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class EmailServiceImpl implements EmailService {
    final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    String fromEmail;
    @Value("${app.frontend-base-url:http://localhost:5173}")
    String frontendBaseUrl;

    @Override
    public void sendVerificationEmail(String to, String token) {
        if (!StringUtils.hasText(to)) {
            log.warn("Skip sending verification email: recipient is empty");
            return;
        }

        String verificationLink = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl + "?token=" + token
                : frontendBaseUrl + "/?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.hasText(fromEmail)) {
            message.setFrom(fromEmail);
        }
        message.setTo(to);
        message.setSubject("Подтверждение регистрации Fixly");
        message.setText("Перейдите по ссылке, чтобы подтвердить почту: " + verificationLink);

        try {
            mailSender.send(message);
            log.info("Verification email queued for {}", to);
        } catch (MailException ex) {
            log.error("Failed to send verification email to {}: {}", to, ex.getMessage());
        }
    }
}
