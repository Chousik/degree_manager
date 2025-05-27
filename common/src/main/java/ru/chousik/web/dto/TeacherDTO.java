package ru.chousik.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDTO {
    private UUID id;
    private String name;
    private String surname;
    private String middleName;
    private String academicStatus;
}