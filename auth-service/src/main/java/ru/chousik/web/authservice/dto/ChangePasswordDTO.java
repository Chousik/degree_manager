package ru.chousik.web.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class ChangePasswordDTO {
    @NotBlank(message = "Необходим старый пароль")
    String oldPassword;
    @NotBlank(message = "Необходим новый пароль.")
    @Size(min = 8, message = "Минимальная нового длина пароля 8 символов.")
    String newPassword;
}
