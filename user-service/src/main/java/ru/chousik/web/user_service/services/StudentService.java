package ru.chousik.web.user_service.services;

import ru.chousik.web.user_service.entity.StudentEntity;

import java.util.List;
import java.util.UUID;

public interface StudentService {
    List<StudentEntity> getStudents();
    StudentEntity getStudent(UUID uuid);
}
