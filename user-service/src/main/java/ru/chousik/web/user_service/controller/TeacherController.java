package ru.chousik.web.user_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.dto.TeacherDTO;
import ru.chousik.web.user_service.entity.TeacherEntity;
import ru.chousik.web.user_service.services.TeacherService;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TeacherController {
    TeacherService teacherService;
    ModelMapper modelMapper;

    @GetMapping
    List<TeacherDTO> getTeacher(){
        return teacherService.getTeachers()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    private TeacherDTO convertToDTO(TeacherEntity teacher){
        return modelMapper.map(teacher, TeacherDTO.class);
    }
}
