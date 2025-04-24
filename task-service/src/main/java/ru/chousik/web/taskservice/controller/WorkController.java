package ru.chousik.web.taskservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.taskservice.entity.TeacherEntity;
import ru.chousik.web.taskservice.dto.WorkDto;
import ru.chousik.web.taskservice.entity.StudentEntity;
import ru.chousik.web.taskservice.entity.WorkEntity;
import ru.chousik.web.taskservice.repository.WorkRepository;

import java.util.List;

@RestController
@RequestMapping("/work")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkController {
    WorkRepository workRepository;

    @GetMapping()
    public List<WorkDto> getWorks(){
        List<WorkEntity> workEntityList = workRepository.findAll();
        return workEntityList.stream()
                .map(this::makeDto).toList();
    }

    //#todo нормальный маппер
    private WorkDto makeDto(WorkEntity work){
        WorkDto dto = new WorkDto();
        dto.setYear(work.getYear());
        dto.setCompletion(work.getCompletion());
        dto.setTitle(work.getTitle());
        StudentEntity student = work.getStudentEntity();
        TeacherEntity teacher = work.getTeacher();
        dto.setAuthor(student.getSurname() +
                student.getName() + student.getMiddleName());
        dto.setSupervisor(teacher.getSurname() +
                teacher.getName() + teacher.getMiddleName());
        return dto;
    }
}
