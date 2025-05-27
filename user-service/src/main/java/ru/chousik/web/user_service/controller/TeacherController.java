package ru.chousik.web.user_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.taskservice.entity.TeacherEntity;
import ru.chousik.web.taskservice.repository.TeacherRepository;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TeacherController {
    TeacherRepository teacherRepository;

    @GetMapping
    List<TeacherEntity> getTeacher(){
        return teacherRepository.findAll();
    }
}
