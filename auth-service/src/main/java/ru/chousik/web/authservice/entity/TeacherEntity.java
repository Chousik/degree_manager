package ru.chousik.web.authservice.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "teachers")
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonTypeName("ru.chousik.web.authservice.security.TeacherEntity")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@NoArgsConstructor
@Getter
@Setter
public class TeacherEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Size(max = 50)
    @NotNull
    String name;

    @Size(max = 50)
    @NotNull
    String surname;

    @Column(name = "middle_name")
    @Size(max = 50)
    @NotNull
    String middleName;

    @Column(name = "academic_status")
    @Size(max = 50)
    @NotNull
    String academicStatus;

    public TeacherEntity(String name,
                         String surname,
                         String middleName,
                         String academicStatus){
        this.name = name;
        this.surname = surname;
        this.middleName = middleName;
        this.academicStatus = academicStatus;
    }
}
