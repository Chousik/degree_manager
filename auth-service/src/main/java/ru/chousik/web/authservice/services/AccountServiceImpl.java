package ru.chousik.web.authservice.services;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.chousik.web.authservice.dto.AdminChangePasswordDTO;
import ru.chousik.web.authservice.dto.ChangePasswordDTO;
import ru.chousik.web.authservice.dto.RegisterUserDTO;
import ru.chousik.web.authservice.dto.UserDTO;
import ru.chousik.web.authservice.entity.AuthoritiesEntity;
import ru.chousik.web.authservice.entity.EmailVerificationToken;
import ru.chousik.web.authservice.entity.UserEntity;
import ru.chousik.web.authservice.entity.UserProfileEntity;
import ru.chousik.web.authservice.exception.*;
import ru.chousik.web.authservice.repository.AuthoritiesRepository;
import ru.chousik.web.authservice.repository.EmailVerificationTokenRepository;
import ru.chousik.web.authservice.repository.UserProfileRepository;
import ru.chousik.web.authservice.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    final AuthoritiesRepository authoritiesRepository;
    final UserRepository userRepository;
    final UserProfileRepository userProfileRepository;
    final EmailVerificationTokenRepository emailVerificationTokenRepository;
    final PasswordEncoder passwordEncoder;
    final EmailService emailService;

    @Value("${app.email-verification.expiration-hours:24}")
    long verificationExpirationHours;

    @Override
    @Transactional
    public void register(RegisterUserDTO dto){
        if (userRepository.existsByUsername(dto.getUsername())){
            throw new UsernameExistsException(dto.getUsername());
        }
        if (userProfileRepository.existsByEmail(dto.getEmail())) {
            throw new EmailExistsException(dto.getEmail());
        }

        UserEntity user = new UserEntity(dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                false);
        user = userRepository.save(user);

        authoritiesRepository.save(new AuthoritiesEntity(user, "ROLE_USER"));

        UserProfileEntity profile = new UserProfileEntity();
        profile.setUsername(dto.getUsername());
        profile.setEmail(dto.getEmail());
        profile.setName(dto.getName());
        profile.setSurname(dto.getSurname());
        profile.setLastName(dto.getLastName());
        profile.setPhone(dto.getPhone());
        profile.setStatus("PENDING_VERIFICATION");
        profile.setCreatedAt(OffsetDateTime.now());
        userProfileRepository.save(profile);

        emailVerificationTokenRepository.deleteByUsername(user.getUsername());
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUsername(user.getUsername());
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(OffsetDateTime.now().plusHours(verificationExpirationHours));
        token.setUsed(false);
        emailVerificationTokenRepository.save(token);

        emailService.sendVerificationEmail(dto.getEmail(), token.getToken());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken tokenEntity = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (Boolean.TRUE.equals(tokenEntity.getUsed())) {
            throw new InvalidVerificationTokenException();
        }
        if (tokenEntity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ExpiredVerificationTokenException();
        }

        UserEntity user = userRepository.getUserEntitiesByUsername(tokenEntity.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(tokenEntity.getUsername()));
        if (Boolean.FALSE.equals(user.getEnabled())) {
            user.setEnabled(true);
            userRepository.save(user);
        }

        userProfileRepository.findByUsername(user.getUsername())
                .ifPresent(profile -> {
                    profile.setStatus("ACTIVE");
                    userProfileRepository.save(profile);
                });

        tokenEntity.setUsed(true);
        emailVerificationTokenRepository.save(tokenEntity);
    }

    @Override
    @Transactional
    public void ensureOAuthUser(String email, String name) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required for OAuth login");
        }

        UserEntity user = userRepository.getUserEntitiesByUsername(email).orElse(null);
        if (Objects.isNull(user)) {
            user = new UserEntity(email, passwordEncoder.encode(UUID.randomUUID().toString()), true);
            userRepository.saveAndFlush(user);
            authoritiesRepository.save(new AuthoritiesEntity(user, "ROLE_USER"));

            UserProfileEntity profile = new UserProfileEntity();
            profile.setUsername(email);
            profile.setEmail(email);
            profile.setName(resolveName(name, email));
            profile.setSurname("");
            profile.setStatus("ACTIVE");
            profile.setCreatedAt(OffsetDateTime.now());
            userProfileRepository.save(profile);
        } else if (Boolean.FALSE.equals(user.getEnabled())) {
            user.setEnabled(true);
            userRepository.saveAndFlush(user);
        }

        emailVerificationTokenRepository.deleteByUsername(email);
    }

    @Override
    public void deleteUser(String username){
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        authoritiesRepository.removeByUser(user);
        emailVerificationTokenRepository.deleteByUsername(username);
        userRepository.delete(user);
    }

    @Override
    public void changeOwnPassword(String username, ChangePasswordDTO dto){
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new IncorrectOldPasswordException();
        }

        changePassword(username,
                passwordEncoder.encode(dto.getNewPassword()));
    }

    @Override
    public void changeUserPassword(String username, AdminChangePasswordDTO dto){
        if (!userRepository.existsByUsername(username)){
            throw new UserNotFoundException(username);
        }

        changePassword(username,
                passwordEncoder.encode(dto.getPassword()));
    }

    private void changePassword(String username, String password){
        if (!(passwordEncoder.matches(userRepository.getPasswordByUsername(username),
                password))){
            throw new PasswordReuseException();
        }

        userRepository.updatePasswordByUsername(passwordEncoder.encode(password),
                username);
    }

    @Override
    public void addAdminRole(String username) {
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (!authoritiesRepository.getAuthoritiesEntityByUser(user).
                stream()
                .filter(r -> r.getAuthority().equals("ROLE_ADMIN"))
                .toList()
                .isEmpty()){
            throw new AdminRoleAlreadyAssignedException(username);
        }

        authoritiesRepository.save(new AuthoritiesEntity(user, "ROLE_ADMIN"));
    }

    @Override
    public void removeAdminRole(String username){
        UserEntity user = userRepository.getUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (authoritiesRepository.getAuthoritiesEntityByUser(user).
                stream()
                .filter(r -> r.getAuthority().equals("ROLE_ADMIN"))
                .toList()
                .isEmpty()){
            throw new AdminRoleNotAssignedException(username);
        }

        authoritiesRepository.removeByAuthorityAndUser("ROLE_ADMIN", user);
    }

    @Override
    public List<UserDTO> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private UserDTO mapToDTO(UserEntity user){
        List<String> authorities = authoritiesRepository.getAuthoritiesEntityByUser(user)
                .stream()
                .map(Objects::toString)
                .toList();
        return new UserDTO(user.getUsername(),
                authorities);
    }

    private String resolveName(String displayName, String email) {
        if (StringUtils.hasText(displayName)) {
            return displayName;
        }
        if (StringUtils.hasText(email)) {
            return email.split("@")[0];
        }
        return "user";
    }
}
