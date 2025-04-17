package ru.chousik.web.authservice.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(level = PRIVATE)
@RequiredArgsConstructor
public class AuthResponse {
    final String accessToken;
    final String tokenType;
}
