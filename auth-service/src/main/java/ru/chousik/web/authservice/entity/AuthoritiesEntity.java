package ru.chousik.web.authservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "authorities")
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthoritiesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @OneToOne
    @JoinColumn(name = "username")
    UserEntity user;

    @Size(max = 50)
    @NotNull
    String authority;

    public AuthoritiesEntity(UserEntity user, String authority) {
        this.user = user;
        this.authority = authority;
    }
}