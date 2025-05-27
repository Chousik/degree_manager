package ru.chousik.web.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Table(name = "students")
@Entity
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID uuid;

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

    public StudentEntity(String name,
                         String surname,
                         String middleName){
        this.name = name;
        this.surname = surname;
        this.middleName = middleName;
    }
}
