package ru.chousik.web.authservice.controller;

import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.chousik.web.authservice.dto.AuthResponse;
import ru.chousik.web.authservice.dto.LoginRequest;
import ru.chousik.web.authservice.dto.SignupRequest;
import ru.chousik.web.authservice.service.AuthService;

import static lombok.AccessLevel.PRIVATE;

@Controller
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
    public class AuthController {
        AuthService authService;
        @PostMapping("/login")
        public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
            return authService.login(loginRequest);
        }

        @PostMapping("/signup")
        @ResponseStatus(HttpStatus.CREATED)
        public void signup(@Valid @RequestBody SignupRequest req) {
            authService.signup(req);
        }
    }
