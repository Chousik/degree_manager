package ru.chousik.web.authservice.services;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.secret.SecretGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.chousik.web.authservice.dto.TwoFactorSetupResponse;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.exception.InvalidTwoFactorCodeException;
import ru.chousik.web.authservice.exception.MissingTwoFactorCodeException;
import ru.chousik.web.authservice.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecretGenerator secretGenerator;

    @Mock
    private CodeVerifier codeVerifier;

    @InjectMocks
    private TwoFactorService twoFactorService;

    @Test
    void initiateSetup_generatesSecretAndReturnsUrl() {
        UserEntity user = new UserEntity();
        user.setUsername("alice");
        when(userRepository.getUserEntitiesByUsername("alice")).thenReturn(Optional.of(user));
        when(secretGenerator.generate()).thenReturn("secret123");
        ReflectionTestUtils.setField(twoFactorService, "issuer", "Fixly");

        TwoFactorSetupResponse response = twoFactorService.initiateSetup("alice");

        assertThat(response.enabled()).isFalse();
        assertThat(response.secret()).isEqualTo("secret123");
        assertThat(response.otpauthUrl()).contains("secret123");
        assertThat(response.otpauthUrl()).contains("Fixly");
        verify(userRepository).save(user);
    }

    @Test
    void enable_throwsWhenCodeInvalid() {
        UserEntity user = new UserEntity();
        user.setUsername("bob");
        user.setTwoFactorSecret("secret");
        when(userRepository.getUserEntitiesByUsername("bob")).thenReturn(Optional.of(user));
        when(codeVerifier.isValidCode("secret", "000000")).thenReturn(false);

        assertThatThrownBy(() -> twoFactorService.enable("bob", "000000"))
                .isInstanceOf(InvalidTwoFactorCodeException.class);
    }

    @Test
    void requireCodeForLogin_requiresCodeWhenEnabled() {
        UserEntity user = new UserEntity();
        user.setTwoFactorEnabled(true);

        assertThatThrownBy(() -> twoFactorService.requireCodeForLogin(user, null))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("OTP_REQUIRED");
    }

    @Test
    void requireValidCodeForAction_skipsWhenDisabled() {
        UserEntity user = new UserEntity();
        user.setTwoFactorEnabled(false);

        twoFactorService.requireValidCodeForAction(user, null);

        verify(codeVerifier, never()).isValidCode(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void requireValidCodeForAction_requiresCodeWhenEnabled() {
        UserEntity user = new UserEntity();
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret("secret");

        assertThatThrownBy(() -> twoFactorService.requireValidCodeForAction(user, " "))
                .isInstanceOf(MissingTwoFactorCodeException.class);
    }
}
