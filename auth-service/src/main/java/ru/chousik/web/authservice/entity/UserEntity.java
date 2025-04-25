package ru.chousik.web.authservice.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonTypeName("ru.chousik.web.authservice.entity.UserEntity")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public class UserEntity {
    @Id
    String username;

    @NotNull
    String password;

    @NotNull
    Boolean enabled;

    @OneToOne
    @JoinColumn(name = "teacher_id", unique = true)
    TeacherEntity teacher;
}
