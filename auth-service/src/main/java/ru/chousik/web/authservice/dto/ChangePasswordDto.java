package ru.chousik.web.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class ChangePasswordDto {
    @NotBlank(message = "Old password is required")
    String oldPassword;
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password need minimum 8 character")
    String newPassword;
}
