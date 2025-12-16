package ru.chousik.web.authservice.dto;

import jakarta.validation.constraints.Email;
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
    @NotBlank(message = "Email требуется.")
    @Email(message = "Некорректный email.")
    String email;
    @NotBlank(message = "Имя требуется.")
    @Size(max = 20, message = "Имя не должно превышать 20 символов.")
    String name;
    @NotBlank(message = "Фамилия требуется.")
    @Size(max = 60, message = "Фамилия не должна превышать 60 символов.")
    String surname;
    @Size(max = 20, message = "Отчество не должно превышать 20 символов.")
    String lastName;
    @Size(max = 12, message = "Номер телефона не должен превышать 12 символов.")
    String phone;
}
