package ru.chousik.web.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    UUID id;

    @NotNull
    @Column(name = "email", nullable = false)
    String email;

    @NotNull
    @Column(name = "username", nullable = false, unique = true, length = 50)
    String username;

    @Size(max = 20)
    @NotNull
    @Column(name = "name", nullable = false, length = 20)
    String name;

    @Size(max = 60)
    @NotNull
    @Column(name = "surname", nullable = false, length = 60)
    String surname;

    @Size(max = 20)
    @Column(name = "last_name", length = 20)
    String lastName;

    @Size(max = 12)
    @Column(name = "phone", length = 12)
    String phone;

    @Size(max = 60)
    @NotNull
    @Column(name = "city", nullable = false, length = 60)
    String city;

    @Column(name = "rating", precision = 2, scale = 1)
    BigDecimal rating;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    String status;

    @Column(name = "last_ban")
    UUID lastBan;

    @NotNull
    @Column(name = "created_at", nullable = false)
    OffsetDateTime createdAt;
}
