package ru.chousik.web.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    String username;

    @NotBlank(message = "password is required")
    String password;
}
