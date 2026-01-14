package ru.chousik.web.authservice.dto;

public record TwoFactorSetupResponse(boolean enabled, String secret, String otpauthUrl) {
}
