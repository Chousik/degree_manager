package ru.chousik.web.taskservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.net.URL;
import java.util.UUID;

@Table(name = "works")
@Entity
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID uuid;

    @Size(max = 50)
    @NotNull
    String title;

    @NotNull
    Integer completion;

    @NotNull
    Integer year;

    @Column(name = "teacher_id")
    @NotNull
    UUID teacherId;

    @Column(name = "student_id")
    @NotNull
    UUID studentId;

    @NotNull
    String key;
}
