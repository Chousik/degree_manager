package ru.chousik.web.user_service.services;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.chousik.web.user_service.entity.StudentEntity;
import ru.chousik.web.user_service.repository.StudentRepository;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {
    StudentRepository studentRepository;

    @Override
    public List<StudentEntity> getStudents() {
        return studentRepository.findAll();
    }

    @Override
    public StudentEntity getStudent(UUID uuid) {
        return studentRepository.getStudentEntityByUuid(uuid);
    }
}
