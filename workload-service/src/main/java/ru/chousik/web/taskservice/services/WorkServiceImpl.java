package ru.chousik.web.taskservice.services;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.chousik.web.dto.StudentDTO;
import ru.chousik.web.taskservice.clients.StudentClient;
import ru.chousik.web.taskservice.dto.WorkDTO;
import ru.chousik.web.taskservice.entity.WorkEntity;
import ru.chousik.web.taskservice.repository.WorkRepository;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {
    WorkRepository workRepository;
    StudentClient studentClient;
    ModelMapper modelMapper;

    @Override
    public List<WorkDTO> getWorks() {
        return workRepository
                .findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private WorkDTO mapToDTO(WorkEntity work){
        StudentDTO student = studentClient.getStudentById(work.getStudentId());
        WorkDTO workDTO = modelMapper.map(work, WorkDTO.class);
        workDTO.setAuthor(String.join(" ",
                List.of(
                        student.getName(),
                        student.getMiddleName(),
                        student.getSurname())));
        return workDTO;
    }
}
