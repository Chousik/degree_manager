package ru.chousik.web.authservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import ru.chousik.web.authservice.service.AuthService;

import static lombok.AccessLevel.PRIVATE;

@Controller
@FieldDefaults(level = PRIVATE)
@RequiredArgsConstructor
public class AuthController {
    AuthService authService;
}
