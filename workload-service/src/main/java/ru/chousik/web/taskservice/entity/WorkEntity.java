package ru.chousik.web.taskservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

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

    @Size(max = 500)
    @NotNull
    String title;

    @Min(0) @Max(100)
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

    @Column(unique=true)
    @NotNull
    String key;

    @Min(0) @Max(100)
    @NotNull
    Integer uniqueCount;
}
