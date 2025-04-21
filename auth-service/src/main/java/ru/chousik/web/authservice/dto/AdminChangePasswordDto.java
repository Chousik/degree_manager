package ru.chousik.web.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminChangePasswordDto {
    @NotBlank(message = "New password is required")
    String password;
}
