package ru.chousik.web.user_service.services;

import ru.chousik.web.user_service.entity.TeacherEntity;

import java.util.List;
import java.util.UUID;

public interface TeacherService {
    List<TeacherEntity> getTeachers();
    TeacherEntity getTeacher(UUID uuid);
}
