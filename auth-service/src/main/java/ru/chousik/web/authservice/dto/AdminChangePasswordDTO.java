package ru.chousik.web.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminChangePasswordDTO {
    @NotBlank(message = "Необходим новый пароль.")
    @Size(min = 8, message = "Минимальная длина нового пароля 8 символов.")
    String password;
}
