package ru.chousik.web.authservice.dto;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(level = PRIVATE)
public class AuthResponse {
    String password;
    String accessToken;
    String tokenType;
}
