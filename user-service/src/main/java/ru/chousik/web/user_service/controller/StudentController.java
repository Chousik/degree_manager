package ru.chousik.web.user_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.user_service.entity.StudentEntity;
import ru.chousik.web.user_service.repository.StudentRepository;

import java.util.List;

@RestController
@RequestMapping("/student")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StudentController {
    StudentRepository studentRepository;

    @GetMapping
    List<StudentEntity> getStudents(){
        return studentRepository.findAll();
    }
}
