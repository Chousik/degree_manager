package ru.chousik.web.authservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.chousik.web.authservice.dto.RegisterUserDTO;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.EmailVerificationToken;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.entity.UserProfileEntity;
import ru.chousik.web.authservice.exception.EmailExistsException;
import ru.chousik.web.authservice.exception.ExpiredVerificationTokenException;
import ru.chousik.web.authservice.exception.InvalidVerificationTokenException;
import ru.chousik.web.authservice.exception.UsernameExistsException;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.EmailVerificationTokenRepository;
import ru.chousik.web.authservice.repository.UserProfileRepository;
import ru.chousik.web.authservice.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AuthoritiesRepository authoritiesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private TwoFactorService twoFactorService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void register_throwsWhenUsernameExists() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("alice");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> accountService.register(dto))
                .isInstanceOf(UsernameExistsException.class);
    }

    @Test
    void register_throwsWhenEmailExists() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userProfileRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> accountService.register(dto))
                .isInstanceOf(EmailExistsException.class);
    }

    @Test
    void register_createsUserAndSendsEmail() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("bob");
        dto.setPassword("password123");
        dto.setEmail("bob@example.com");
        dto.setName("Bob");
        dto.setSurname("Stone");

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userProfileRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        UserEntity savedUser = new UserEntity();
        savedUser.setUsername("bob");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(authoritiesRepository.save(any(AuthoritiesEntity.class))).thenReturn(new AuthoritiesEntity());
        when(userProfileRepository.save(any(UserProfileEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        accountService.register(dto);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendVerificationEmail("bob@example.com", tokenCaptor.getValue().getToken());
    }

    @Test
    void verifyEmail_throwsForExpiredToken() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("token");
        token.setExpiresAt(OffsetDateTime.now().minusHours(1));
        token.setUsed(false);

        when(emailVerificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> accountService.verifyEmail("token"))
                .isInstanceOf(ExpiredVerificationTokenException.class);
    }

    @Test
    void verifyEmail_updatesUserAndProfile() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("token");
        token.setExpiresAt(OffsetDateTime.now().plusHours(2));
        token.setUsed(false);
        token.setUsername("alice");

        UserEntity user = new UserEntity();
        user.setUsername("alice");
        user.setEnabled(false);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setUsername("alice");
        profile.setStatus("PENDING_VERIFICATION");

        when(emailVerificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));
        when(userRepository.getUserEntitiesByUsername("alice")).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUsername("alice")).thenReturn(Optional.of(profile));

        accountService.verifyEmail("token");

        assertThat(token.getUsed()).isTrue();
        verify(userRepository).save(user);
        verify(userProfileRepository).save(profile);
        verify(emailVerificationTokenRepository).save(token);
    }

    @Test
    void verifyEmail_throwsWhenTokenInvalid() {
        when(emailVerificationTokenRepository.findByToken("token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.verifyEmail("token"))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }
}
