package ru.chousik.web.user_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.dto.TeacherDTO;
import ru.chousik.web.user_service.repository.TeacherRepository;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TeacherController {
    TeacherRepository teacherRepository;

    @GetMapping
    List<TeacherDTO> getTeacher(){
        return teacherRepository.findAll();
    }
}
