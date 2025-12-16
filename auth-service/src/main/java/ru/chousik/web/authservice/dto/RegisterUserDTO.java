package ru.chousik.web.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterUserDTO {
    @NotBlank(message = "Юзернейм требуется.")
    String username;
    @NotBlank(message = "Пароль требуется.")
    @Size(min = 8, message = "Длина пароля минимум 8 символов.")
    String password;
}
