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
import ru.chousik.web.authservice.exception.AdminRoleAlreadyAssignedException;
import ru.chousik.web.authservice.exception.AdminRoleNotAssignedException;
import ru.chousik.web.authservice.exception.EmailExistsException;
import ru.chousik.web.authservice.exception.ExpiredVerificationTokenException;
import ru.chousik.web.authservice.exception.InvalidVerificationTokenException;
import ru.chousik.web.authservice.exception.IncorrectOldPasswordException;
import ru.chousik.web.authservice.exception.PasswordReuseException;
import ru.chousik.web.authservice.exception.UserNotFoundException;
import ru.chousik.web.authservice.exception.UsernameExistsException;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.EmailVerificationTokenRepository;
import ru.chousik.web.authservice.repository.UserProfileRepository;
import ru.chousik.web.authservice.repository.UserRepository;
import ru.chousik.web.authservice.dto.ChangePasswordDTO;
import ru.chousik.web.authservice.dto.AdminChangePasswordDTO;

import java.time.OffsetDateTime;
import java.util.List;
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

    @Test
    void ensureOAuthUser_createsUserWhenMissing() {
        when(userRepository.getUserEntitiesByUsername("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed");

        accountService.ensureOAuthUser("user@example.com", "User");

        verify(userRepository).saveAndFlush(any(UserEntity.class));
        verify(authoritiesRepository).save(any(AuthoritiesEntity.class));
        verify(userProfileRepository).save(any(UserProfileEntity.class));
        verify(emailVerificationTokenRepository).deleteByUsername("user@example.com");
    }

    @Test
    void ensureOAuthUser_enablesExistingUser() {
        UserEntity user = new UserEntity();
        user.setUsername("user@example.com");
        user.setEnabled(false);
        when(userRepository.getUserEntitiesByUsername("user@example.com")).thenReturn(Optional.of(user));

        accountService.ensureOAuthUser("user@example.com", null);

        assertThat(user.getEnabled()).isTrue();
        verify(userRepository).saveAndFlush(user);
        verify(emailVerificationTokenRepository).deleteByUsername("user@example.com");
    }

    @Test
    void changeOwnPassword_throwsWhenOldPasswordInvalid() {
        UserEntity user = new UserEntity();
        user.setUsername("alice");
        user.setPassword("hashed");
        when(userRepository.getUserEntitiesByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(false);

        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("old");
        dto.setNewPassword("newpassword");

        assertThatThrownBy(() -> accountService.changeOwnPassword("alice", dto))
                .isInstanceOf(IncorrectOldPasswordException.class);
    }

    @Test
    void changeOwnPassword_updatesPassword() {
        UserEntity user = new UserEntity();
        user.setUsername("alice");
        user.setPassword("hashed");
        when(userRepository.getUserEntitiesByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(true);
        when(userRepository.getPasswordByUsername("alice")).thenReturn("hashed");
        when(passwordEncoder.matches("newpass", "hashed")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("new-hash");

        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("old");
        dto.setNewPassword("newpass");
        dto.setOtp("123456");

        accountService.changeOwnPassword("alice", dto);

        verify(twoFactorService).requireValidCodeForAction(user, "123456");
        verify(userRepository).updatePasswordByUsername("new-hash", "alice");
    }

    @Test
    void changeUserPassword_throwsWhenUserMissing() {
        when(userRepository.existsByUsername("missing")).thenReturn(false);

        AdminChangePasswordDTO dto = new AdminChangePasswordDTO();
        dto.setPassword("newpass");

        assertThatThrownBy(() -> accountService.changeUserPassword("missing", dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void changeUserPassword_throwsWhenPasswordReused() {
        when(userRepository.existsByUsername("user")).thenReturn(true);
        when(userRepository.getPasswordByUsername("user")).thenReturn("hash");
        when(passwordEncoder.matches("newpass", "hash")).thenReturn(true);

        AdminChangePasswordDTO dto = new AdminChangePasswordDTO();
        dto.setPassword("newpass");

        assertThatThrownBy(() -> accountService.changeUserPassword("user", dto))
                .isInstanceOf(PasswordReuseException.class);
    }

    @Test
    void addAdminRole_throwsWhenAlreadyAssigned() {
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        when(userRepository.getUserEntitiesByUsername("admin")).thenReturn(Optional.of(user));
        when(authoritiesRepository.getAuthoritiesEntityByUser(user))
                .thenReturn(List.of(new AuthoritiesEntity(user, "ROLE_ADMIN")));

        assertThatThrownBy(() -> accountService.addAdminRole("admin"))
                .isInstanceOf(AdminRoleAlreadyAssignedException.class);
    }

    @Test
    void removeAdminRole_throwsWhenMissing() {
        UserEntity user = new UserEntity();
        user.setUsername("user");
        when(userRepository.getUserEntitiesByUsername("user")).thenReturn(Optional.of(user));
        when(authoritiesRepository.getAuthoritiesEntityByUser(user))
                .thenReturn(List.of(new AuthoritiesEntity(user, "ROLE_USER")));

        assertThatThrownBy(() -> accountService.removeAdminRole("user"))
                .isInstanceOf(AdminRoleNotAssignedException.class);
    }

    @Test
    void getUsers_mapsAuthorities() {
        UserEntity user = new UserEntity();
        user.setUsername("bob");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(authoritiesRepository.getAuthoritiesEntityByUser(user))
                .thenReturn(List.of(new AuthoritiesEntity(user, "ROLE_USER")));

        var result = accountService.getUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("bob");
        assertThat(result.get(0).getRoles()).contains("ROLE_USER");
    }
}
