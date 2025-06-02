package ru.chousik.web.user_service.services;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.chousik.web.user_service.entity.TeacherEntity;
import ru.chousik.web.user_service.repository.TeacherRepository;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    TeacherRepository teacherRepository;

    @Override
    public List<TeacherEntity> getTeachers() {
        return teacherRepository.findAll();
    }

    @Override
    public TeacherEntity getTeacher(UUID uuid) {
        return teacherRepository.findById(uuid)
                .orElseThrow(IllegalArgumentException::new);
    }
}
