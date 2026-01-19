package ru.chousik.web.authservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import ru.chousik.web.authservice.dto.TwoFactorCodeRequest;
import ru.chousik.web.authservice.dto.TwoFactorSetupResponse;
import ru.chousik.web.authservice.dto.TwoFactorStatusResponse;
import ru.chousik.web.authservice.services.TwoFactorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoFactorControllerTest {

    @Mock
    private TwoFactorService twoFactorService;

    @InjectMocks
    private TwoFactorController twoFactorController;

    @Test
    void status_usesPreferredUsername() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "alice")
                .build();
        when(twoFactorService.getStatus("alice")).thenReturn(new TwoFactorStatusResponse(true));

        TwoFactorStatusResponse response = twoFactorController.status(jwt);

        assertThat(response.enabled()).isTrue();
        verify(twoFactorService).getStatus("alice");
    }

    @Test
    void setup_usesSubjectFallback() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("bob")
                .build();
        when(twoFactorService.initiateSetup("bob"))
                .thenReturn(new TwoFactorSetupResponse(false, "secret", "otp://"));

        TwoFactorSetupResponse response = twoFactorController.setup(jwt);

        assertThat(response.secret()).isEqualTo("secret");
        verify(twoFactorService).initiateSetup("bob");
    }

    @Test
    void enable_delegatesWithCode() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("user_name", "carol")
                .build();

        TwoFactorStatusResponse response = twoFactorController.enable(jwt, new TwoFactorCodeRequest("123456"));

        assertThat(response.enabled()).isTrue();
        verify(twoFactorService).enable("carol", "123456");
    }
}
