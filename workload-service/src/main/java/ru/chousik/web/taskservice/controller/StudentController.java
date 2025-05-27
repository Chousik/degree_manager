package ru.chousik.web.taskservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.taskservice.entity.StudentEntity;
import ru.chousik.web.taskservice.repository.StudentRepository;

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
