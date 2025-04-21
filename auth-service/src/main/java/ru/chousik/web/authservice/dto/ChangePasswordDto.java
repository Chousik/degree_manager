package ru.chousik.web.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class ChangePasswordDto {
    @NotBlank(message = "Old password is required")
    String oldPassword;
    @NotBlank(message = "New password is required")
    String newPassword;
}
