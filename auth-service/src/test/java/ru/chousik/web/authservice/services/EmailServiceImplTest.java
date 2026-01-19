package ru.chousik.web.authservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendVerificationEmail_skipsWhenRecipientBlank() {
        emailService.sendVerificationEmail("   ", "token");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendVerificationEmail_buildsVerificationLinkAndSetsFrom() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@fixly.ru");
        ReflectionTestUtils.setField(emailService, "verificationLinkBase", "https://fixly.test/");

        emailService.sendVerificationEmail("user@example.com", "abc123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("noreply@fixly.ru");
        assertThat(message.getTo()).containsExactly("user@example.com");
        assertThat(message.getText()).contains("https://fixly.test/api/users/verify-email?token=abc123");
    }
}
