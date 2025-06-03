package ru.chousik.web.taskservice.services;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.chousik.web.dto.StudentDTO;
import ru.chousik.web.dto.TeacherDTO;
import ru.chousik.web.taskservice.clients.StudentClient;
import ru.chousik.web.taskservice.dto.SaveWorkDTO;
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
    public void saveWork(SaveWorkDTO saveWorkDTO,
                         String key){
        WorkEntity work = fromDTO(saveWorkDTO);
        work.setKey(key);
        Integer unique = workRepository.findFirstUniqueCountByTitle(saveWorkDTO.getTitle())
                        .orElse((int) (Math.random() * 23) + 70);
        work.setUniqueCount(unique);
        workRepository.save(work);
    }

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
        TeacherDTO teacher = studentClient.getTeacherById(work.getTeacherId());
        WorkDTO workDTO = modelMapper.map(work, WorkDTO.class);
        workDTO.setAuthor(String.join(" ",
                List.of(
                        student.getName(),
                        student.getMiddleName(),
                        student.getSurname())));
        workDTO.setSupervisor(String.join(" ",
                List.of(
                        teacher.getName(),
                        teacher.getMiddleName(),
                        teacher.getSurname())));
        return workDTO;
    }

    private WorkEntity fromDTO(SaveWorkDTO saveWorkDTO){
        return modelMapper.map(saveWorkDTO, WorkEntity.class);
    }
}
