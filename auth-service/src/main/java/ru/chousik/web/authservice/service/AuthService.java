package ru.chousik.web.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.chousik.web.authservice.repository.UserRepository;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Service
@RequiredArgsConstructor
public class AuthService {
    UserRepository userRepository;
}
