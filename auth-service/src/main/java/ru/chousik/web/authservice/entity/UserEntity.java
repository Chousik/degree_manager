package ru.chousik.web.authservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserEntity {
    @Id
    String username;

    @NotNull
    String password;

    @NotNull
    Boolean enabled;

    @OneToOne
    @JoinColumn(name = "id", unique = true)
    TeacherEntity teacher;
}
