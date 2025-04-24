package ru.chousik.web.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterUserDto {
    @NotBlank(message = "Username is required")
    String username;
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password need minimum 8 character")
    String password;
    @NotBlank(message = "name is required")
    @Size(min = 1, message = "name need minimum 8 character")
    String name;
    @NotBlank(message = "surname is required")
    @Size(min = 1, message = "surname need minimum 8 character")
    String surname;
    @NotBlank(message = "middleName is required")
    @Size(min = 1, message = "middleName need minimum 8 character")
    @JsonProperty("middle_name")
    String middleName;
}
