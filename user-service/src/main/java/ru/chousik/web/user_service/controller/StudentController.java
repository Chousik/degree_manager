package ru.chousik.web.user_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.dto.StudentDTO;
import ru.chousik.web.user_service.entity.StudentEntity;
import ru.chousik.web.user_service.services.StudentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/student")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StudentController {
    StudentService studentService;
    ModelMapper modelMapper;

    @GetMapping
    List<StudentDTO> getStudents(){
        return studentService.getStudents()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public StudentDTO getStudent(@PathVariable UUID id) {
        return this.convertToDTO(studentService.getStudent(id));
    }

    private StudentDTO convertToDTO(StudentEntity student){
        return modelMapper.map(student, StudentDTO.class);
    }
}
