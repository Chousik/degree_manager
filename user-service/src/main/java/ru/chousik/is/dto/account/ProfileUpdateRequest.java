package ru.chousik.is.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProfileUpdateRequest(
        @NotBlank String name,
        @NotBlank String surname,
        String lastName,
        @Pattern(regexp = "^$|^\\d{10,12}$", message = "Phone must contain 10-12 digits") String phone,
        String city
) {
}
