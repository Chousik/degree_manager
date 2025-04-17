package ru.chousik.web.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.dto.AuthResponse;
import ru.chousik.web.authservice.dto.LoginRequest;
import ru.chousik.web.authservice.dto.SignupRequest;
import ru.chousik.web.authservice.entity.Role;
import ru.chousik.web.authservice.entity.User;
import ru.chousik.web.authservice.repository.UserRepository;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Service
@RequiredArgsConstructor
public class AuthService {
    final AuthenticationManager authenticationManager;
    final JwtService jwt;
    private final PasswordEncoder encoder;
    private final UserRepository userRepo;


    public AuthResponse login(LoginRequest req) throws Exception {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getUsername(), req.getPassword()));
        String token = jwt.generateToken((UserDetails) auth.getPrincipal());
        return new AuthResponse(token, "Bearer");
    }
    public void signup(SignupRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent())
            throw new IllegalStateException("Username already taken");

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRoles(Set.of());
        userRepo.save(user);
    }
}
