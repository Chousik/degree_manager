package ru.chousik.web.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotNull
    @Column(name = "username", nullable = false)
    String username;

    @NotNull
    @Column(name = "token", nullable = false, unique = true, length = 180)
    String token;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    OffsetDateTime expiresAt;

    @NotNull
    @Column(name = "used", nullable = false)
    Boolean used = false;
}
