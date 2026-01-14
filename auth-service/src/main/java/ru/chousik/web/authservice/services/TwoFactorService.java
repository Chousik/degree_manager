package ru.chousik.web.authservice.services;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.chousik.web.authservice.dto.TwoFactorSetupResponse;
import ru.chousik.web.authservice.dto.TwoFactorStatusResponse;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.exception.InvalidTwoFactorCodeException;
import ru.chousik.web.authservice.exception.MissingTwoFactorCodeException;
import ru.chousik.web.authservice.repository.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserRepository userRepository;
    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    @Value("${app.two-factor.issuer:Fixly}")
    private String issuer;

    @Transactional
    public TwoFactorSetupResponse initiateSetup(String username) {
        UserEntity user = getUser(username);
        String secret = secretGenerator.generate();
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(Boolean.FALSE);
        userRepository.save(user);
        return new TwoFactorSetupResponse(false, secret, buildOtpauthUrl(username, secret));
    }

    @Transactional(readOnly = true)
    public TwoFactorStatusResponse getStatus(String username) {
        UserEntity user = getUser(username);
        return new TwoFactorStatusResponse(Boolean.TRUE.equals(user.getTwoFactorEnabled()));
    }

    @Transactional
    public void enable(String username, String code) {
        UserEntity user = getUser(username);
        if (!isCodeValid(user, code)) {
            throw new InvalidTwoFactorCodeException();
        }
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disable(String username, String code) {
        UserEntity user = getUser(username);
        if (!isCodeValid(user, code)) {
            throw new InvalidTwoFactorCodeException();
        }
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
    }

    public void requireValidCodeForAction(UserEntity user, String code) {
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return;
        }
        if (!StringUtils.hasText(code)) {
            throw new MissingTwoFactorCodeException();
        }
        if (!isCodeValid(user, code)) {
            throw new InvalidTwoFactorCodeException();
        }
    }

    public void requireCodeForLogin(UserEntity user, String code) {
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return;
        }
        if (!StringUtils.hasText(code)) {
            throw new BadCredentialsException("OTP_REQUIRED");
        }
        if (!isCodeValid(user, code)) {
            throw new BadCredentialsException("INVALID_OTP");
        }
    }

    private boolean isCodeValid(UserEntity user, String code) {
        if (user == null || !StringUtils.hasText(user.getTwoFactorSecret())) {
            return false;
        }
        if (!StringUtils.hasText(code)) {
            return false;
        }
        return codeVerifier.isValidCode(user.getTwoFactorSecret(), code.trim());
    }

    private UserEntity getUser(String username) {
        return userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));
    }

    private String buildOtpauthUrl(String username, String secret) {
        String label = issuer + ":" + username;
        return "otpauth://totp/" + encode(label) + "?secret=" + secret + "&issuer=" + encode(issuer);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
