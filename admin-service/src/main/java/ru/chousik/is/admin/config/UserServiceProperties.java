package ru.chousik.is.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.user-service")
public record UserServiceProperties(String baseUrl) {
}
