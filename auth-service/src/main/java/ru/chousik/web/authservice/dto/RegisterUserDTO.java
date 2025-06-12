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
    @NotBlank(message = "Имя требуется.")
    @Size(min = 1, message = "Имя должно содержать держать минимум 1 символ.")
    String name;
    @NotBlank(message = "Требуется фамилия.")
    @Size(min = 1, message = "Фамилия должна содержать держать минимум 1 символ.")
    String surname;
    @NotBlank(message = "Требуется отчество.")
    @Size(min = 1, message = "Отчество должно содержать держать минимум 1 символ.")
    @JsonProperty("middle_name")
    String middleName;
}
